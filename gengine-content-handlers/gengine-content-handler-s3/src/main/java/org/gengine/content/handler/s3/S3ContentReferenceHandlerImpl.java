package org.gengine.content.handler.s3;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;
import org.gengine.content.handler.AbstractUrlContentReferenceHandler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

/**
 * AWS S3 content handler implementation
 *
 */
public class S3ContentReferenceHandlerImpl extends AbstractUrlContentReferenceHandler
{
    private static final Log logger = LogFactory.getLog(S3ContentReferenceHandlerImpl.class);

    /** store protocol that is used as prefix in contentUrls */
    public static final String S3_STORE_PROTOCOL = "s3";
    public static final String S3_PROTOCOL_DELIMITER = "://";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";

    private AmazonS3 s3;
    private TransferManager tm;

    //
    // If s3AccessKey / s3SecretKey are not overridden
    // then use DefaultAWSCredentialsProviderChain which searches for credentials in this order:
    //
    // - Environment Variables - AWS_ACCESS_KEY_ID and AWS_SECRET_KEY
    // - Java System Properties - aws.accessKeyId and aws.secretKey
    // - Credential profiles file at the default location (~/.aws/credentials) shared by all AWS SDKs and the AWS CLI
    // - Credentials delivered through the Amazon EC2 container service if AWS_CONTAINER_CREDENTIALS_RELATIVE_URI env var is set
    //   and security manager has permission to access the var,
    // - Instance profile credentials delivered through the Amazon EC2 metadata service
    //
    private String s3AccessKey;
    private String s3SecretKey;

    private String s3BucketName;
    private String s3BucketRegion;

    private static final String DEFAULT_BUCKET_REGION = "us-east-1";

    public void setS3AccessKey(String s3AccessKey)
    {
        this.s3AccessKey = s3AccessKey;
    }

    public void setS3SecretKey(String s3SecretKey)
    {
        this.s3SecretKey = s3SecretKey;
    }

    public String getS3BucketName()
    {
        return s3BucketName;
    }

    public void setS3BucketName(String s3BucketName)
    {
        this.s3BucketName = s3BucketName;
    }

    public void setS3BucketRegion(String s3BucketLocation)
    {
        this.s3BucketRegion = s3BucketLocation;
    }

    // Instantiate S3 Service and get or create necessary bucket
    public void init()
    {
        try
        {
            s3 = initClient(s3AccessKey, s3SecretKey, s3BucketRegion);

            if (!s3.doesBucketExist(s3BucketName))
            {
                // relies on s3ClientBuilder regional endpoint (or else default region)
                s3.createBucket(s3BucketName);
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("S3 content transport initialization complete: " +
                        "{ bucketName: '" + s3BucketName + "', bucketLocation: '" + s3BucketRegion + "' }");
            }

            tm = TransferManagerBuilder.standard().withS3Client(s3).build();

            this.isAvailable = true;
        }
        catch (AmazonClientException e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("S3 content transport failed to initialize bucket " +
                        "'" + s3BucketName + "': " + e.getMessage());
            }

            this.isAvailable = false;
        }
    }

    // helper
    public static AmazonS3 initClient(String s3AccessKey, String s3SecretKey, String s3BucketRegion)
    {
        // equivalent to "defaultClient" (unless credentials &/or region are overridden)
        AmazonS3ClientBuilder s3ClientBuilder = AmazonS3ClientBuilder.standard();

        if ((s3AccessKey != null) || (s3SecretKey != null))
        {
            // override default credentials
            s3ClientBuilder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(s3AccessKey, s3SecretKey)));
        }

        if (s3BucketRegion != null)
        {
            // override default region
            s3ClientBuilder.withRegion(s3BucketRegion);
        }

        AmazonS3 s3 = null;

        try
        {
            s3 = s3ClientBuilder.build();
        }
        catch (IllegalArgumentException e)
        {
            if (e.getCause() != null &&
                e.getCause() instanceof URISyntaxException &&
                e.getCause().getMessage().startsWith("Illegal character in authority at index 8: https://s3."))
            {
                s3ClientBuilder.withRegion(DEFAULT_BUCKET_REGION);
                s3 = s3ClientBuilder.build();
            }
        }
        catch (AmazonClientException e)
        {
            if (e.getMessage().contains("Unable to find a region via the region provider chain"))
            {
                s3ClientBuilder.withRegion(DEFAULT_BUCKET_REGION);
                s3 = s3ClientBuilder.build();
            }
        }

        return s3;
    }

    @Override
    protected String getRemoteBaseUrl()
    {
        return S3_STORE_PROTOCOL + S3_PROTOCOL_DELIMITER + s3BucketName + "/";
    }

    protected String getS3UrlFromHttpUrl(String url)
    {
        if (url == null)
        {
            return null;
        }
        if (url.startsWith(S3_STORE_PROTOCOL))
        {
            return url;
        }
        url = url.replaceFirst(HTTPS_PROTOCOL + ":\\/\\/", S3_STORE_PROTOCOL + ":\\/\\/");
        url = url.replaceFirst(HTTP_PROTOCOL + ":\\/\\/", S3_STORE_PROTOCOL + ":\\/\\/");
        url = url.replaceFirst("\\.s3\\.amazonaws\\.com", "");
        return url;
    }

    @Override
    public boolean isContentReferenceSupported(ContentReference contentReference)
    {
        if (contentReference == null)
        {
            return false;
        }
        String uri = contentReference.getUri();
        if (uri == null)
        {
            return false;
        }
        if (uri.startsWith(S3_STORE_PROTOCOL + S3_PROTOCOL_DELIMITER))
        {
            return true;
        }
        if ((uri.startsWith(HTTPS_PROTOCOL) || uri.startsWith(HTTP_PROTOCOL)) &&
             uri.contains("s3.amazonaws.com/"))
        {
            return true;
        }
        return false;
    }

    @Override
    public boolean isContentReferenceExists(ContentReference contentReference)
    {
        if (!isContentReferenceSupported(contentReference))
        {
            return false;
        }

        try
        {
            String s3Url = getS3UrlFromHttpUrl(contentReference.getUri());

            if (logger.isDebugEnabled())
            {
                logger.debug("Checking existence of reference: " + s3Url);
            }

            return s3.doesObjectExist(s3BucketName, getRelativePath(s3Url));
        }
        catch (AmazonServiceException e)
        {
        	if (e.getStatusCode() == 404)
        	{
        		return false;
        	}
            throw new ContentIOException("Failed to check existence of content: " + e.getMessage(), e);
        }
        catch (Throwable t)
        {
            // Otherwise don't really care why, just that it doesn't exist

            if (logger.isWarnEnabled())
            {
                logger.warn("Ignoring failure to check existence of content: " + t.getMessage());
            }

            return false;
        }
    }

    @Override
    public InputStream getInputStream(ContentReference contentReference, boolean waitForAvailability) throws ContentIOException
    {
        if (!isContentReferenceSupported(contentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }
        try
        {
            String s3Url = getS3UrlFromHttpUrl(contentReference.getUri());

            if (logger.isDebugEnabled())
            {
                logger.debug("Getting remote input stream for reference: " + s3Url);
            }

            // Get the object and retrieve the input stream
            S3Object object = s3.getObject(new GetObjectRequest(s3BucketName, getRelativePath(s3Url)));

            if (logger.isDebugEnabled())
            {
                logger.debug("Get object: " + s3Url);
            }

            return object.getObjectContent();
        }
        catch (Throwable t)
        {
            throw new ContentIOException("Failed to read content", t);
        }
    }

    @Override
    public long putFile(File sourceFile, ContentReference targetContentReference)
            throws ContentIOException
    {
        if (! isContentReferenceSupported(targetContentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }

        long startTime = System.currentTimeMillis();

        String remotePath = getRelativePath(targetContentReference.getUri());

        try
        {
            long contentLength = sourceFile.length();

            try
            {
                Upload xfer = tm.upload(s3BucketName, remotePath, sourceFile);
                xfer.waitForCompletion();
            }
            catch (InterruptedException e)
            {
                logger.error("Upload was interrupted: "+e.getMessage());

                // Be a good citizen  and set interrupt flag
                Thread.currentThread().interrupt();

                throw new ContentIOException("Failed to write content", e);
            }

            ObjectMetadata metadata = s3.getObjectMetadata(
                    new GetObjectMetadataRequest(s3BucketName, remotePath));

            long storedContentLength = metadata.getContentLength();

            if (logger.isWarnEnabled())
            {
                if (storedContentLength != contentLength)
                {
                    logger.warn("Metadata length differs - expected " + contentLength + ", actual "+ storedContentLength);
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Upload file: " + s3BucketName + ":" + remotePath + " (in "+(System.currentTimeMillis()-startTime)+" msecs)");
            }

            return storedContentLength;
        }
        catch (AmazonClientException e)
        {
            throw new ContentIOException("Failed to write content", e);
        }
    }

    @Override
    public long putInputStream(InputStream sourceInputStream, ContentReference targetContentReference)
            throws ContentIOException
    {
        if (! isContentReferenceSupported(targetContentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }

        long startTime = System.currentTimeMillis();

        String remotePath = getRelativePath(targetContentReference.getUri());

        try
        {
            Long contentLength = targetContentReference.getSize();
            ObjectMetadata omd = new ObjectMetadata();
            if (contentLength != null)
            {
                omd.setContentLength(contentLength);
            }

            s3.putObject(new PutObjectRequest(s3BucketName, remotePath, sourceInputStream, omd));

            ObjectMetadata metadata = s3.getObjectMetadata(
                    new GetObjectMetadataRequest(s3BucketName, remotePath));

            long storedContentLength = metadata.getContentLength();

            if (logger.isWarnEnabled())
            {
                if ((contentLength != null) && (storedContentLength != contentLength))
                {
                    logger.warn("Metadata length differs - expected " + contentLength + ", actual "+ storedContentLength);
                }
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Put object: " + s3BucketName + ":" + remotePath + " (in "+(System.currentTimeMillis()-startTime)+" msecs)");
            }

            return storedContentLength;
        }
        catch (AmazonClientException e)
        {
            throw new ContentIOException("Failed to write content", e);
        }
    }

    @Override
    public File getFile(ContentReference contentReference, boolean waitForTransfer) throws ContentIOException, InterruptedException {
        if (!isContentReferenceSupported(contentReference)) {
            throw new ContentIOException("ContentReference not supported");
        }

        try {
            String s3Url = getS3UrlFromHttpUrl(contentReference.getUri());

            if (logger.isDebugEnabled()) {
                logger.debug("Getting file for reference: " + s3Url);
            }

            // Create a temporary file
            File tempFile = File.createTempFile("s3content", null);

            // Get the object and retrieve the input stream
            S3Object object = s3.getObject(new GetObjectRequest(s3BucketName, getRelativePath(s3Url)));
            InputStream inputStream = object.getObjectContent();

            // Write the input stream to the temporary file
            try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new ContentIOException("Failed to write content to file", e);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("File written to: " + tempFile.getAbsolutePath());
            }

            return tempFile;
        } catch (Throwable t) {
            throw new ContentIOException("Failed to get content", t);
        }
    }
    
    @Override
    public void delete(ContentReference contentReference) throws ContentIOException
    {
        if (!isContentReferenceSupported(contentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }

        String remotePath = getRelativePath(contentReference.getUri());

        try
        {
            s3.deleteObject(new DeleteObjectRequest(s3BucketName, remotePath));

            if (logger.isDebugEnabled())
            {
                logger.debug("Deleted object: " + s3BucketName + ":" + remotePath);
            }
        }
        catch (AmazonClientException e)
        {
            throw new ContentIOException("Failed to delete content", e);
        }
    }
}
