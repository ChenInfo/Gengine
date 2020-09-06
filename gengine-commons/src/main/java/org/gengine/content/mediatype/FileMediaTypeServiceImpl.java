package org.gengine.content.mediatype;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.gengine.error.GengineRuntimeException;

/**
 * Implementation of FileMediaTypeService which delegates to Apache Tika
 * to do the actual work.
 *
 */
public class FileMediaTypeServiceImpl implements FileMediaTypeService
{

    private static final Log logger = LogFactory.getLog(FileMediaTypeServiceImpl.class);

    protected TikaConfig tikaConfig;
    protected Tika tika;

    public FileMediaTypeServiceImpl(TikaConfig tikaConfig)
    {
        this.tikaConfig = tikaConfig;
        init();
    }

    public void init()
    {
        if (tikaConfig == null)
        {
            logger.debug("Initializing with default Tika config");
            tikaConfig = TikaConfig.getDefaultConfig();
        }
        if (tika == null)
        {
            tika = new Tika(tikaConfig);
        }
    }

    @Override
    public String getExtension(String mimetype)
    {
        try
        {
            MimeType tikaMimeType = tikaConfig.getMimeRepository().forName(mimetype);
            if (tikaMimeType != null)
            {
                String tikaExtension = tikaMimeType.getExtension();
                if (tikaExtension.startsWith("."))
                {
                    tikaExtension = tikaExtension.substring(1, tikaExtension.length());
                }
                return tikaExtension;
            }
        }
        catch (MimeTypeException e)
        {
            throw new GengineRuntimeException("Could not get extension for mimetype", e);
        }

        return null;
    }

    @Override
    public String getMediaType(String extension)
    {
        return tika.detect("*." + extension);
    }

    @Override
    public String getMediaTypeByName(File file)
    {
        return tika.detect(file.getName());
    }

    public MimeType getTikaMimeType(String mimetype) throws MimeTypeException
    {
        return tikaConfig.getMimeRepository().forName(mimetype);
    }

}
