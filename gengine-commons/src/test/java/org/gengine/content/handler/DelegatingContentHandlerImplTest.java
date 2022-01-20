package org.gengine.content.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.gengine.content.ContentIOException;
import org.gengine.content.ContentReference;
import org.gengine.content.file.FileProvider;
import org.gengine.content.file.FileProviderImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DelegatingContentHandlerImplTest
{
    private ContentReferenceHandler handlerA;
    private ContentReferenceHandler handlerB;
    private FileContentReferenceHandler delegatingHandler;

    private ContentReference contentReferenceFile1a;
    private ContentReference contentReferenceFile1b;
    private ContentReference contentReferenceFile1c;

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Before
    public void setUp() throws Exception
    {
        FileProvider fileProviderA = new FileProviderImpl("content-handler-a");
        handlerA = new MockFileContentReferenceHandlerImpl("content-handler-a");
        ((FileContentReferenceHandlerImpl) handlerA).setFileProvider(fileProviderA);

        FileProvider fileProviderB = new FileProviderImpl("content-handler-b");
        handlerB = new MockFileContentReferenceHandlerImpl("content-handler-b");
        ((FileContentReferenceHandlerImpl) handlerB).setFileProvider(fileProviderB);

        ArrayList<ContentReferenceHandler> delegates = new ArrayList<ContentReferenceHandler>();
        delegates.add(handlerA);
        delegates.add(handlerB);
        delegatingHandler = new DelegatingContentHandlerImpl();
        ((DelegatingContentHandlerImpl) delegatingHandler).setDelegates(delegates);

        contentReferenceFile1a = getTextReference("/content-handler-a/file-a-1.txt");
        contentReferenceFile1b = getTextReference("/content-handler-b/file-b-1.txt");
        contentReferenceFile1c = getTextReference("/content-handler-c/file-c-1.txt");
    }

    private ContentReference getTextReference(String testPath) throws URISyntaxException
    {
        return getContentReference(testPath, "text/plain");
    }

    private ContentReference getContentReference(String testPath, String mediaType) throws URISyntaxException
    {
        return new ContentReference(this.getClass().getResource(testPath).toURI().toString(), mediaType);
    }

    @Test
    public void testIsAvailable()
    {
        assertTrue(delegatingHandler.isAvailable());

        ((FileContentReferenceHandlerImpl) handlerA).setFileProvider(null);
        assertFalse(delegatingHandler.isAvailable());
    }

    @Test
    public void testIsContentReferenceSupported()
    {
        assertTrue(delegatingHandler.isContentReferenceSupported(contentReferenceFile1a));
        assertTrue(delegatingHandler.isContentReferenceSupported(contentReferenceFile1b));
        assertFalse(delegatingHandler.isContentReferenceSupported(contentReferenceFile1c));
    }

    @Test
    public void testIsContentReferenceExists()
    {
        assertTrue(delegatingHandler.isContentReferenceExists(contentReferenceFile1a));
        assertTrue(delegatingHandler.isContentReferenceExists(contentReferenceFile1b));

        thrown.expect(UnsupportedOperationException.class);
        delegatingHandler.isContentReferenceExists(contentReferenceFile1c);
    }

    @Test
    public void testCreateContentReference()
    {
        thrown.expect(UnsupportedOperationException.class);
        delegatingHandler.createContentReference("testCreate.txt", "text/plain");
    }

    @Test
    public void testGetFile() throws Exception
    {
        File sourceFile = delegatingHandler.getFile(contentReferenceFile1a, false);
        assertTrue(sourceFile.exists());
    }

    @Test
    public void testNonFileDelegate() throws Exception
    {
        MockEmptyContentReferenceHandlerImpl handlerC = new MockEmptyContentReferenceHandlerImpl();
        ArrayList<ContentReferenceHandler> delegates = new ArrayList<ContentReferenceHandler>();
        delegates.add(handlerA);
        delegates.add(handlerB);
        delegates.add(handlerC);

        ContentReference contentReference = new ContentReference("mock://empty.txt", "text/plain");

        thrown.expect(UnsupportedOperationException.class);
        delegatingHandler.getFile(contentReference, false);
    }

    @Test
    public void testGetInputStream() throws Exception
    {
        InputStream sourceInputStream = delegatingHandler.getInputStream(contentReferenceFile1a, false);
        assertEquals(15, sourceInputStream.available());
    }

    @Test
    public void testPutFile() throws Exception
    {
        File sourceFile = new File(this.getClass().getResource(
                "/content-handler-a/file-a-1.txt").toURI());

        String targetFolder = "/content-handler-a";
        ContentReference targetContentReference = getTextReference(targetFolder);
        targetContentReference.setUri(targetContentReference.getUri() + "/file-a-3.txt");

        delegatingHandler.putFile(sourceFile, targetContentReference);

        File targetFile = new File(new URI(targetContentReference.getUri()));
        assertTrue(targetFile.exists());
    }

    @Test
    public void testPutInputStream() throws Exception
    {
        InputStream sourceInputStream = (this.getClass().getResourceAsStream(
                "/content-handler-a/file-a-1.txt"));

        String targetFolder = "/content-handler-a";
        ContentReference targetContentReference = getTextReference(targetFolder);
        targetContentReference.setUri(targetContentReference.getUri() + "/file-a-4.txt");

        delegatingHandler.putInputStream(sourceInputStream, targetContentReference);

        File targetFile = new File(new URI(targetContentReference.getUri()));
        assertTrue(targetFile.exists());
    }

    @Test
    public void testDelete() throws Exception
    {
        File sourceFile = new File(this.getClass().getResource(
                    "/content-handler-b/file-b-1.txt").toURI());

        ContentReference targetContentReferenceToDelete = getTextReference("/content-handler-b");
        targetContentReferenceToDelete.setUri(targetContentReferenceToDelete.getUri() + "/file-b-3.txt");

        delegatingHandler.putFile(sourceFile, targetContentReferenceToDelete);
        File fileToDelete = new File(new URI(targetContentReferenceToDelete.getUri()));
        assertTrue(fileToDelete.exists());

        delegatingHandler.delete(targetContentReferenceToDelete);
        assertFalse(fileToDelete.exists());
    }

    /**
     * Test class that allows checking for specific paths
     */
    public class MockFileContentReferenceHandlerImpl extends FileContentReferenceHandlerImpl
    {
        private String supportedUriPath;

        public MockFileContentReferenceHandlerImpl(String supportedUriPath)
        {
            this.supportedUriPath = supportedUriPath;
        }

        @Override
        public boolean isContentReferenceSupported(ContentReference contentReference)
        {
            if (contentReference == null)
            {
                return false;
            }
            return contentReference.getUri().contains(supportedUriPath);
        }
    }

    /**
     * Test class for non-file content references
     */
    public class MockEmptyContentReferenceHandlerImpl implements ContentReferenceHandler
    {

        @Override
        public boolean isContentReferenceSupported(ContentReference contentReference)
        {
            return contentReference.getUri().startsWith("mock:/");
        }

        @Override
        public boolean isContentReferenceExists(ContentReference contentReference)
        {
            return true;
        }

        @Override
        public ContentReference createContentReference(String fileName, String mediaType) throws ContentIOException
        {
            return null;
        }

        @Override
        public InputStream getInputStream(ContentReference contentReference, boolean waitForAvailability)
                throws ContentIOException, InterruptedException
        {
            return null;
        }

        @Override
        public long putInputStream(InputStream sourceInputStream, ContentReference targetContentReference)
                throws ContentIOException
        {
            return 0;
        }

        @Override
        public long putFile(File sourceFile, ContentReference targetContentReference) throws ContentIOException
        {
            return 0;
        }

        @Override
        public void delete(ContentReference contentReference) throws ContentIOException
        {
        }

        @Override
        public boolean isAvailable()
        {
            return true;
        }
    }
}
