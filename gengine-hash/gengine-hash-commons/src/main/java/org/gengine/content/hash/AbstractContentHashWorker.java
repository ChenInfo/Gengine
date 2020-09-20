package org.gengine.content.hash;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gengine.content.AbstractContentWorker;
import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;
import org.gengine.content.ContentWorkResult;

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
        super.initialize();
        if (sourceContentReferenceHandler != null && sourceContentReferenceHandler.isAvailable())
        {
            setIsAvailable(true);
        }
    }

    @Override
    public List<ContentWorkResult> generateHashes(
            List<ContentReference> sources,
            String hashAlgorithm) throws ContentIOException, InterruptedException, ContentHashException
    {
        List<ContentWorkResult> results = new ArrayList<ContentWorkResult>();
        if (sources == null || sources.size() == 0)
        {
            return results;
        }
        for (ContentReference source : sources)
        {
            String value = generateHashInternal(
                        sourceContentReferenceHandler.getInputStream(source, true),
                        hashAlgorithm);
            Map<String, Object> resultDetails = new HashMap<String, Object>();
            resultDetails.put(ContentHashWorker.RESULT_DETAIL_HEX_ENCODED_VALUE, value);
            ContentWorkResult result = new ContentWorkResult(source, resultDetails);
            results.add(result);
        }
        return results;
    }

    /**
     * Computes the hash value for the given input stream using the given algorithm
     *
     * @param sourceFile
     * @param hashAlgorithm
     * @return the hex encoded hash value
     * @throws ContentIOException
     * @throws InterruptedException
     * @throws ContentHashException
     */
    public abstract String generateHashInternal(
            InputStream sourceFile,
            String hashAlgorithm) throws ContentIOException, InterruptedException, ContentHashException;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName() + "[");
        builder.append("contentReferenceHandler: " + sourceContentReferenceHandler.toString());
        builder.append("]");
        return builder.toString();
    }

}
