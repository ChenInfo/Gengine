package org.gengine.content.transform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.gengine.content.ContentReference;
import org.gengine.content.mediatype.FileMediaTypeService;
import org.gengine.content.mediatype.FileMediaTypeServiceImpl;

/**
 * Base content transformer worker class
 *
 */
public abstract class AbstractContentTransformerWorkerTest
{
    protected FileMediaTypeService mediaTypeService = new FileMediaTypeServiceImpl(null);

    /**
     * For the given mime type, returns one or more quick*
     *  files to be tested.
     * By default this is just quick + the default extension.
     * However, you can override this if you need special
     *  rules, eg quickOld.foo, quickMid.foo and quickNew.foo
     *  for differing versions of the file format.
     */
    protected String[] getQuickFilenames(String sourceMimetype) {
       String sourceExtension = mediaTypeService.getExtension(sourceMimetype);
       return new String[] {
             "quick." + sourceExtension
       };
    }

    /**
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     *
     * @param the file required, eg <b>quick.txt</b>
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     * @throws IOException
     */
    public static ContentReference getNamedQuickTestFileReference(String quickname, String mediaType) throws IOException
    {
        URL url = AbstractContentTransformerWorkerTest.class.getClassLoader().getResource("quick/" + quickname);
        if (url == null)
        {
            throw new FileNotFoundException(quickname + " not found");
        }
        // TODO Consider introduction of Spring core dependency for better resolution in jars, etc.
        return new ContentReference(url.toString(), mediaType);
    }

    /**
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     *
     * @param the file required, eg <b>quick.txt</b>
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     * @throws IOException
     */
    public static File loadNamedQuickTestFile(String quickname) throws IOException
    {
        URL url = AbstractContentTransformerWorkerTest.class.getClassLoader().getResource("quick/" + quickname);
        if (url == null)
        {
            return null;
        }
        try
        {
            return new File(new URI(url.toString()));
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
        // TODO Consider introduction of Spring core dependency
        // return ResourceUtils.getFile(url);
    }

    /**
     * Helper method to load one of the "The quick brown fox" files from the
     * classpath.
     *
     * @param the file extension required, eg <b>txt</b> for the file quick.txt
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     * @throws IOException
     */
    public static File loadQuickTestFile(String extension) throws IOException
    {
       return loadNamedQuickTestFile("quick."+extension);
    }

}
