
package org.gengine.content.handler.s3;

import com.amazonaws.ClientConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.ContentReference;
import org.gengine.content.file.TempFileProvider;
import org.gengine.content.mediatype.FileMediaType;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the s3 content reference handler
 *
 * @see {@link S3ContentReferenceHandlerImpl}
 */
public class S3ContentReferenceHandlerImplTest
{
    private static final Log logger = LogFactory.getLog(S3ContentReferenceHandlerImplTest.class);

    private static S3ContentReferenceHandlerImpl handler;

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
    private static String s3AccessKey = null;
    private static String s3SecretKey = null;

    private static String s3BucketName = "alf-gengine-s3-test-"+UUID.randomUUID().toString();

    private static String s3BucketRegion = null;

    private static boolean cleanup = true;

    @BeforeClass
    public static void setUp()
    {
        handler = new S3ContentReferenceHandlerImpl();

        handler.setS3AccessKey(s3AccessKey);
        handler.setS3SecretKey(s3SecretKey);

        handler.setS3BucketName(s3BucketName);
        handler.setS3BucketRegion(s3BucketRegion);

        // note: will attempt to create bucket if it does not exist
        handler.init();

        assertTrue("S3 not available !", handler.isAvailable());
    }

    @AfterClass
    public static void tearDown()
    {
    	if (cleanup)
    	{
	        try
	        {
	        	AmazonS3 s3 = S3ContentReferenceHandlerImpl.initClient(s3AccessKey, s3SecretKey, s3BucketRegion);
	            s3.deleteBucket(s3BucketName);
	        }
	        catch (AmazonClientException e)
	        {
	            throw e;
	        }
    	}
    }

    protected void checkReference(String fileName, String mediaType)
    {
        ContentReference reference = handler.createContentReference(fileName, mediaType);
        assertEquals(mediaType, reference.getMediaType());

        String uri = reference.getUri();
        String createdFileName = uri.split("\\/")[uri.split("\\/").length-1];

        String origPrefix = fileName.substring(0, StringUtils.lastIndexOf(fileName, "."));
        String origSuffix = fileName.substring(StringUtils.lastIndexOf(fileName, "."), fileName.length());
        assertTrue("ContentReference file name '" + createdFileName +
                "' did not contain original file name prefix '" + origPrefix + "'",
                createdFileName.contains(origPrefix));
        assertTrue("ContentReference file name '" + createdFileName +
                "' did not contain original file name suffix '" + origPrefix + "'",
                createdFileName.contains(origSuffix));
    }

    @Test
    public void testSimpleReference()
    {
        checkReference("myfile.txt", "text/plain");
    }

    @Test
    public void testPathReference()
    {
        checkReference("my.file.txt", "text/plain");
    }

    /**
     * Tests: putInputStream, getInputStream, delete
     */
    @Test
    public void testPutGetDelete()
    {
        int LOOP_COUNT = ClientConfiguration.DEFAULT_MAX_CONNECTIONS + 5; // MM-682

        int TEST_SIZE_IN_BYTES = 1024; // 1 KiB

        for (int i = 0; i < LOOP_COUNT; i++)
        {
            testPutGetDeleteImpl(TEST_SIZE_IN_BYTES);
        }
    }

    private void testPutGetDeleteImpl(int testContentSizeInBytes)
    {
    	String uuid = UUID.randomUUID().toString();
        String fileName = "test-" + uuid + ".bin";

        ContentReference reference = handler.createContentReference(fileName, "application/octet-stream");

        assertFalse(handler.isContentReferenceExists(reference));

        byte[] dataIn = new byte[testContentSizeInBytes];
        new Random().nextBytes(dataIn);

        int contentLen = dataIn.length;
        try
        {
	    	ByteArrayInputStream bais = new ByteArrayInputStream(dataIn);

            // S3 putObject
            reference.setSize((long)contentLen); // prevents AmazonS3Client warn
            handler.putInputStream(bais, reference);

	        bais.close();
        }
        catch (IOException ioe)
        {
        	logger.error(ioe.getMessage());
        }

        assertTrue(handler.isContentReferenceExists(reference));

        try
        {
            // S3 getObject
            InputStream is = handler.getInputStream(reference, false);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1)
            {
                baos.write(buffer, 0, length);
            }

			baos.close();
			is.close();

            byte[] dataOut = baos.toByteArray();

			// check bytes
			assertTrue(Arrays.equals(dataOut, dataIn));
        }
        catch (IOException ioe)
        {
        	logger.error(ioe.getMessage());
        }

        if (cleanup)
        {
            // S3 deleteObject
            handler.delete(reference);

            assertFalse(handler.isContentReferenceExists(reference));
        }
    }

    /**
     * Tests: putFile (+ delete for cleanup)
     */
    @Test
    public void testPutFile() throws IOException
    {
        Long pf_start_size = getVar("pf_start_size", new Long(1024 * 1024 * 1L), Long.class); // default 1 MiB
        Integer pf_multiplier = getVar("pf_multiplier", new Integer(2), Integer.class);
        Integer pf_count = getVar("pf_count", new Integer(3), Integer.class);
        Integer pf_repeat = getVar("pf_repeat", new Integer(1), Integer.class);

        if (logger.isInfoEnabled())
        {
            logger.info("testPutFile: pf_start_size="+pf_start_size+", pf_multiplier="+pf_multiplier
                    +", pf_count="+pf_count+", pf_repeat="+pf_repeat);
        }

        long testContentSizeInBytes = pf_start_size;

        for (int i = 0; i < pf_count; i++)
        {
            File testFile = createTestFile(testContentSizeInBytes);

            for (int j = 0; j < pf_repeat; j++)
            {
                // via S3 Transfer Manger Upload
                testPutFileImpl(testFile, true);

                // via S3 PutObject
                testPutFileImpl(testFile, false);
            }

            if (cleanup)
            {
                testFile.delete();
            }

            // increase test file size for next loop
            testContentSizeInBytes = testContentSizeInBytes * pf_multiplier;
        }
    }

    private void testPutFileImpl(File sourceTestFile, boolean usePutFile) throws FileNotFoundException
    {
        String fileName = sourceTestFile.getName();
        String fileMediaType = FileMediaType.SERVICE.getMediaTypeByName(sourceTestFile);

        ContentReference targetContentReference = handler.createContentReference(fileName, fileMediaType);

        assertFalse(handler.isContentReferenceExists(targetContentReference));

        if (usePutFile)
        {
            // S3 upload
            handler.putFile(sourceTestFile, targetContentReference);
        }
        else
        {
            FileInputStream targetInputStream = new FileInputStream(sourceTestFile);

            // S3 putObject
            targetContentReference.setSize(sourceTestFile.length()); // prevents AmazonS3Client warn
            handler.putInputStream(targetInputStream, targetContentReference);
        }

        assertTrue(handler.isContentReferenceExists(targetContentReference));

        if (cleanup)
        {
            // S3 deleteObject
            handler.delete(targetContentReference);

            assertFalse(handler.isContentReferenceExists(targetContentReference));
        }
    }

    private <T> T getVar(String varName, T defaultValue, Class<T> type)
    {
        String value = System.getProperty(varName);
        if (value == null)
        {
            value = System.getenv(varName);
        }
        if (value == null)
        {
            return defaultValue;
        }
        else if (type == Long.class)
        {
            return type.cast(Long.parseLong(value));
        }
        else if (type == Integer.class)
        {
            return type.cast(Integer.parseInt(value));
        }
        else if (type == String.class)
        {
            return type.cast(value);
        }
        else
        {
            throw new UnsupportedOperationException("Unsupported type: "+ type);
        }
    }

    private File createTestFile(long testContentSizeInBytes) throws IOException
    {
        long startTime = System.currentTimeMillis();

        String fileSuffix = ".tmp";
        String fileBase = "test-" + UUID.randomUUID().toString();

        File testFile = TempFileProvider.createTempFile(fileBase, fileSuffix);

        RandomAccessFile raf = new RandomAccessFile(testFile.getAbsoluteFile(), "rw");
        raf.setLength(testContentSizeInBytes);
        raf.close();

        if (logger.isDebugEnabled())
        {
            logger.debug("Create (random access) test file [" + testFile.getAbsolutePath() + " , " + testContentSizeInBytes + "] (in "+(System.currentTimeMillis()-startTime)+" msecs)");
        }

        return testFile;
    }
}
