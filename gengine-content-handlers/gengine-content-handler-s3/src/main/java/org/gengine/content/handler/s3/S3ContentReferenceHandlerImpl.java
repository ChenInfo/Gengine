package org.gengine.content.handler.s3;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;
import org.gengine.content.handler.AbstractUrlContentReferenceHandler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

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

    private String s3AccessKey;
    private String s3SecretKey;
    private String s3BucketName;
    private String s3BucketRegion;

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

    public void init()
    {
        // Instantiate S3 Service and get or create necessary bucket.
        try
        {
            s3 = new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey));

            if (s3BucketRegion != null)
            {
                if (!s3.doesBucketExist(s3BucketName))
                {
                    s3.createBucket(s3BucketName, s3BucketRegion);
                }
            }
            else
            {
                s3.createBucket(s3BucketName); // default region
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("S3 content transport initialization complete: " +
                        "{ bucketName: '"+s3BucketName+"', bucketLocation: '"+s3BucketRegion + "' }");
            }
            this.isAvailable = true;
        }
        catch (AmazonClientException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("S3 content transport failed to initialize bucket " +
                        "'" + s3BucketName + "': " + e.getMessage());
            }

            this.isAvailable = false;
        }
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
            return object.getObjectContent();
        }
        catch (Throwable t)
        {
            throw new ContentIOException("Failed to read content", t);
        }
    }

    @Override
    public long putInputStream(InputStream sourceInputStream, ContentReference targetContentReference)
            throws ContentIOException
    {
        if (!isContentReferenceSupported(targetContentReference))
        {
            throw new ContentIOException("ContentReference not supported");
        }

        String remotePath = getRelativePath(targetContentReference.getUri());

        try
        {
            s3.putObject(new PutObjectRequest(
                    s3BucketName, remotePath, sourceInputStream, new ObjectMetadata()));
            ObjectMetadata metadata = s3.getObjectMetadata(
                    new GetObjectMetadataRequest(s3BucketName, remotePath));
            return metadata.getContentLength();
        } catch (AmazonClientException e)
        {
            throw new ContentIOException("Failed to write content", e);
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
        } catch (AmazonClientException e)
        {
            throw new ContentIOException("Failed to delete content", e);
        }

    }

}
