package org.gengine.content.hash;

import java.io.InputStream;

import org.gengine.content.AbstractContentWorker;
import org.gengine.content.ContentReference;

/**
 * Abstract hash node worker which uses a content reference handler to convert the
 * content reference into a usable input stream for the actual implementation.
 *
 */
public abstract class AbstractContentHashWorker extends AbstractContentWorker implements ContentHashWorker
{

    @Override
    public void initialize()
    {
    }

    @Override
    public String generateHash(
            ContentReference source,
            String hashAlgorithm) throws Exception
    {
        return generateHashInternal(
                sourceContentReferenceHandler.getInputStream(source, true),
                hashAlgorithm);
    }

    /**
     * Computes the hash value for the given input stream using the given algorithm
     *
     * @param sourceFile
     * @param hashAlgorithm
     * @return the hex encoded hash value
     * @throws Exception
     */
    public abstract String generateHashInternal(
            InputStream sourceFile,
            String hashAlgorithm) throws Exception;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + "[");
        builder.append("contentReferenceHandler: " + sourceContentReferenceHandler.toString());
        builder.append("]");
        return builder.toString();
    }

}
