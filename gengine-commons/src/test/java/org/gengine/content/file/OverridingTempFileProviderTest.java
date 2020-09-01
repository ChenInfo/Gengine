package org.gengine.content.file;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.*;

public class OverridingTempFileProviderTest
{

    @Test
    public void testDir()
    {
        File tempFile = OverridingTempFileProvider.getTempDir();
        assertTrue(tempFile.getPath().contains(OverridingTempFileProvider.APPLICATION_TEMP_FILE_DIR));
    }

}
