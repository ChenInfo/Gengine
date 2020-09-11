package org.gengine.content.hash;

import java.util.List;

import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;
import org.gengine.content.ContentWorkResult;
import org.gengine.content.ContentWorker;

/**
 * Defines the methods responsible for doing the work of hash computation of content references
 *
 */
public interface ContentHashWorker extends ContentWorker
{
    public static final String HASH_ALGORITHM_MD5 = "MD5";
    public static final String HASH_ALGORITHM_SHA_256 = "SHA-256";
    public static final String HASH_ALGORITHM_SHA_512 = "SHA-512";

    public static final String RESULT_DETAIL_HEX_ENCODED_VALUE = "HEX_ENCODED_VALUE";

    /**
     * Generates a hash value for the given content reference using the given algorithm
     *
     * @param sources
     * @param hashAlgorithm
     * @return the results with hex encoded hash values
     * @throws Exception
     */
    public List<ContentWorkResult> generateHashes(
            List<ContentReference> sources,
            String hashAlgorithm) throws ContentIOException, InterruptedException, ContentHashException;

    /**
     * Determines whether or not the given hash algorithm is supported
     * by the implementation.
     *
     * @param hashAlgorithm
     * @return true if the algorithm is supported
     */
    public boolean isAlgorithmSupported(String hashAlgorithm);

}
