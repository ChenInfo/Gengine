package org.gengine.content.file;

/**
 * Class which overrides TempFileProvider's temp file dir for testing
 *
 */
public class OverridingTempFileProvider extends TempFileProvider
{
    public static final String OVERRIDE_TEMP_FILE_DIR = "GengineTest";

    protected static String getApplicationTempFileDir()
    {
        return OVERRIDE_TEMP_FILE_DIR;
    }

}
