package org.gengine.content.hash.javase;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.gengine.content.ContentIOException;
import org.gengine.content.hash.AbstractContentHashWorker;
import org.gengine.content.hash.ContentHashException;

/**
 * A Java SE implementation of a content hash node worker
 *
 */
public class JavaSeContentHashWorker extends AbstractContentHashWorker
{
    private static final int BUFFER_SIZE = 8*1024;

    @Override
    public String generateHashInternal(InputStream source, String hashAlgorithm) throws ContentIOException, InterruptedException, ContentHashException
    {
        if (source == null || hashAlgorithm == null) {
            throw new IllegalArgumentException("source and hashAlgorithm must not be null");
        }
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlgorithm);

            byte[] buffer = new byte[BUFFER_SIZE];

            int bytesRead = 0;
            while( (bytesRead = source.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, bytesRead);
            }

            return encodeHex(messageDigest.digest());
        }
        catch (IOException e)
        {
            throw new ContentIOException("Could not read content for hashing", e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new ContentHashException(e);
        }
        finally
        {
            try
            {
                source.close();
            }
            catch (IOException e)
            {
            }
        }
    }

    /**
     * Performs a hex encoding of the given byte array
     *
     * @param byteArray
     * @return the hex encoded value
     */
    protected String encodeHex(byte[] byteArray)
    {
        BigInteger integer = new BigInteger(1, byteArray);
        return integer.toString(16);
    }

    @Override
    public boolean isAlgorithmSupported(String hashAlgorithm)
    {
        try
        {
            MessageDigest.getInstance(hashAlgorithm);
            return true;
        }
        catch (NoSuchAlgorithmException e)
        {
            return false;
        }
    }

}
