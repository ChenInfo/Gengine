package org.gengine.content.hash.javase;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import org.gengine.content.hash.AbstractContentHashWorker;

/**
 * A Java SE implementation of a content hash node worker
 *
 */
public class JavaSeContentHashNodeWorker extends AbstractContentHashWorker
{
    private static final int BUFFER_SIZE = 8*1024;

    @Override
    public String generateHashInternal(InputStream source, String hashAlgorithm) throws Exception
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
        finally
        {
            source.close();
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

}
