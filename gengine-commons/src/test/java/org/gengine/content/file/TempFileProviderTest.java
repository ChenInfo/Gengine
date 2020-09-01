package org.gengine.content.file;

import java.io.File;

import junit.framework.TestCase;

/**
 * Unit test for TempFileProvider
 *
 * @see org.gengine.content.file.TempFileProvider
 *
 */
public class TempFileProviderTest extends TestCase
{
    /**
     * test of getTempDir
     *
     * @throws Exception
     */
    public void testTempDir() throws Exception
    {
        File tempDir = TempFileProvider.getTempDir();
        assertTrue("Not a directory", tempDir.isDirectory());
        File tempDirParent = tempDir.getParentFile();

        // create a temp file
        File tempFile = File.createTempFile("AAAA", ".tmp");
        File tempFileParent = tempFile.getParentFile();

        // they should be equal
        assertEquals("Our temp dir not subdirectory system temp directory",
                tempFileParent, tempDirParent);
    }

    /**
     * test create a temporary file
     *
     * create another file with the same prefix and suffix.
     * @throws Exception
     */
    public void testTempFile() throws Exception
    {
        File tempFile = TempFileProvider.createTempFile("AAAA", ".tmp");
        File tempFileParent = tempFile.getParentFile();
        File tempDir = TempFileProvider.getTempDir();
        assertEquals("Temp file not located in our temp directory",
                tempDir, tempFileParent);

        /**
         * Create another temp file and then delete it.
         */
        File tempFile2 = TempFileProvider.createTempFile("AAAA", ".tmp");
        tempFile2.delete();
    }

    /**
     * test create a temporary file with a directory
     *
     * create another file with the same prefix and suffix.
     * @throws Exception
     */
    public void testTempFileWithDir() throws Exception
    {
        File tempDir = TempFileProvider.getTempDir();
        File tempFile = TempFileProvider.createTempFile("AAAA", ".tmp", tempDir);
        File tempFileParent = tempFile.getParentFile();
        assertEquals("Temp file not located in our temp directory",
                tempDir, tempFileParent);

        /**
         * Create another temp file and then delete it.
         */
        File tempFile2 = TempFileProvider.createTempFile("AAAA", ".tmp", tempDir);
        tempFile2.delete();
    }
}
