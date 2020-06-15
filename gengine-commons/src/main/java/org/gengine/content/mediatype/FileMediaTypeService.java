package org.gengine.content.mediatype;

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

}
