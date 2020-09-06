package org.gengine.content.handler.tempfile;

import org.gengine.content.file.FileProviderImpl;
import org.gengine.content.file.CleaningTempFileProvider;
import org.gengine.content.handler.FileContentReferenceHandlerImpl;

/**
 * A convenience FileContentReferenceHandlerImpl extension which creates a file
 * provider with a directory path of the {@link CleaningTempFileProvider}'s temp dir.
 *
 */
public class TempFileContentReferenceHandlerImpl extends FileContentReferenceHandlerImpl
{
    public TempFileContentReferenceHandlerImpl()
    {
        super();
        FileProviderImpl fileProvider = new FileProviderImpl();
        fileProvider.setDirectoryPath(CleaningTempFileProvider.getTempDir().getPath());
        setFileProvider(fileProvider);
    }
}
