package org.gengine.content.hash;

import org.gengine.content.ContentReference;
import org.gengine.content.ContentWorker;

/**
 * Defines the methods responsible for doing the work of hash computation of a content reference
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
     * @param source
     * @param hashAlgorithm
     * @return the hex encoded hash value
     * @throws Exception
     */
    public String generateHash(
            ContentReference source,
            String hashAlgorithm) throws Exception;

}
