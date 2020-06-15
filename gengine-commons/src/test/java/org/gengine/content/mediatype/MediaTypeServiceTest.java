package org.gengine.content.mediatype;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;

public class MediaTypeServiceTest
{

    private FileMediaTypeService mediaTypeService;

    @Before
    public void setUp()
    {
        mediaTypeService = new FileMediaTypeServiceImpl(null);
        ((FileMediaTypeServiceImpl) mediaTypeService).init();
    }

    @Test
    public void testGetMediaTypeFromExtension()
    {
        assertEquals("text/plain", mediaTypeService.getMediaType("txt"));
        assertEquals("application/pdf", mediaTypeService.getMediaType("pdf"));
    }

    @Test
    public void testGetExtensionFromMediaType()
    {
        assertEquals(".txt", mediaTypeService.getExtension("text/plain"));
        assertEquals(".pdf", mediaTypeService.getExtension("application/pdf"));
    }

    @Test
    public void testFileMediaType()
    {
        assertEquals("text/plain", FileMediaType.MEDIATYPE_TEXT_PLAIN.getMediaType());
        assertEquals("application/pdf", FileMediaType.MEDIATYPE_PDF.getMediaType());
    }

}
