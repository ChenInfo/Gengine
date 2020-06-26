package org.gengine.content.hash;

import java.io.FileInputStream;
import java.io.InputStream;

import org.gengine.content.ContentReference;
import org.gengine.content.handler.ContentReferenceHandler;

/**
 * Abstract hash node worker which uses a content reference handler to convert the
 * content reference into a usable input stream for the actual implementation.
 *
 */
public abstract class AbstractContentHashWorker implements ContentHashWorker
{

    protected ContentReferenceHandler contentReferenceHandler;

    public void setContentReferenceHandler(ContentReferenceHandler contentReferenceFileHandler)
    {
        this.contentReferenceHandler = contentReferenceFileHandler;
    }

    public String generateHash(
            ContentReference source,
            String hashAlgorithm) throws Exception
    {
        return generateHashInternal(
                new FileInputStream(contentReferenceHandler.getFile(source)),
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
        builder.append("contentReferenceHandler: " + contentReferenceHandler.toString());
        builder.append("]");
        return builder.toString();
    }

}
