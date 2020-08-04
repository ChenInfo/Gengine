package org.gengine.content.hash;

import java.util.List;
import java.util.Map;

import org.gengine.content.ContentReference;
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

    /**
     * Generates a hash value for the given content reference using the given algorithm
     *
     * @param sources
     * @param hashAlgorithm
     * @return the map of hex encoded hash values
     * @throws Exception
     */
    public Map<ContentReference, String> generateHashes(
            List<ContentReference> sources,
            String hashAlgorithm) throws Exception;

}
