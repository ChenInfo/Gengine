package org.gengine.content.hash;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public Map<ContentReference, String> generateHashes(
            List<ContentReference> sources,
            String hashAlgorithm) throws Exception
    {
        Map<ContentReference, String> values = new HashMap<ContentReference, String>();
        if (sources == null || sources.size() == 0)
        {
            return values;
        }
        for (ContentReference source : sources)
        {
            String value = generateHashInternal(
                    sourceContentReferenceHandler.getInputStream(source, true),
                    hashAlgorithm);
            values.put(source, value);
        }
        return values;
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
