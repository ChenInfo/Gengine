package org.gengine.content.mediatype;

import java.io.File;

/**
 * Defines a service for getting extensions for media types and vice versa.
 *
 */
public interface FileMediaTypeService
{

    /**
     * Get the extension for the specified internet media type
     *
     * @param mediaType a valid media type
     * @return Returns the default extension for the media type
     */
    public String getExtension(String mediaType);

    /**
     * Get the internet media type for the specified extension
     *
     * @param extension a valid file extension
     * @return Returns a valid media type if found, or null if does not exist
     */
    public String getMediaType(String extension);

    /**
     * Get the internet media type for the specified file using only its
     * file name, no inspection
     *
     * @param file
     * @return Returns a valid media type if found, or null if does not exist
     */
    public String getMediaTypeByName(File file);

}
