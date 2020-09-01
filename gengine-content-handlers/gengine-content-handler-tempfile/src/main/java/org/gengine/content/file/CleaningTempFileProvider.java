package org.gengine.content.file;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * A helper class that provides temporary files, providing a common point to clean
 * them up.
 *
 * <p>
 * The contents of CHENINFO_TEMP_FILE_DIR [%java.io.tmpdir%/ChenInfo] are managed by this
 * class.  Temporary files and directories are cleaned by TempFileCleanerJob so that
 * after a delay [default 1 hour] the contents of the cheninfo temp dir,
 * both files and directories are removed.
 *
 * <p>
 * Some temporary files may need to live longer than 1 hour.   The temp file provider allows special sub folders which
 * are cleaned less frequently.    By default, files in the long life folders will remain for 24 hours
 * unless cleaned by the application code earlier.
 *
 * <p>
 * The other contents of %java.io.tmpdir% are not touched by the cleaner job.
 *
 * <p>TempFileCleanerJob Job Data: protectHours, number of hours to keep temporary files, default 1 hour.
 *
 */
public class CleaningTempFileProvider extends TempFileProvider
{

    private static final Log logger = LogFactory.getLog(CleaningTempFileProvider.class);

    /**
     * Cleans up <b>all</b> ChenInfo temporary files that are older than the
     * given number of hours.  Subdirectories are emptied as well and all directories
     * below the primary temporary subdirectory are removed.
     * <p>
     * The job data must include a property <tt>protectHours</tt>, which is the
     * number of hours to protect a temporary file from deletion since its last
     * modification.
     *
     */
    public static class TempFileCleanerJob implements Job
    {
        public static final String KEY_PROTECT_HOURS = "protectHours";
        public static final String KEY_DIRECTORY_NAME = "directoryName";

        /**
         * Gets a list of all files in the {@link CleaningTempFileProvider#APPLICATION_TEMP_FILE_DIR temp directory}
         * and deletes all those that are older than the given number of hours.
         */
        public void execute(JobExecutionContext context) throws JobExecutionException
        {
            // get the number of hours to protect the temp files
            String strProtectHours = (String) context.getJobDetail().getJobDataMap().get(KEY_PROTECT_HOURS);
            if (strProtectHours == null)
            {
                throw new JobExecutionException("Missing job data: " + KEY_PROTECT_HOURS);
            }
            int protectHours = -1;
            try
            {
                protectHours = Integer.parseInt(strProtectHours);
            }
            catch (NumberFormatException e)
            {
                throw new JobExecutionException("Invalid job data " + KEY_PROTECT_HOURS + ": " + strProtectHours);
            }
            if (protectHours < 0 || protectHours > 8760)
            {
                throw new JobExecutionException("Hours to protect temp files must be 0 <= x <= 8760");
            }

            String directoryName = (String) context.getJobDetail().getJobDataMap().get(KEY_DIRECTORY_NAME);

            if (directoryName == null)
            {
                directoryName = CleaningTempFileProvider.getApplicationTempFileDir();
            }

            long now = System.currentTimeMillis();
            long aFewHoursBack = now - (3600L * 1000L * protectHours);

            long aLongTimeBack = now - (24 * 3600L * 1000L);

            File tempDir = CleaningTempFileProvider.getTempDir(directoryName);
            int count = removeFiles(tempDir, aFewHoursBack, aLongTimeBack, false);  // don't delete this directory
            // done
            if (logger.isDebugEnabled())
            {
                logger.debug("Removed " + count + " files from temp directory: " + tempDir);
            }
        }

        /**
         * Removes all temporary files created before the given time.
         * <p>
         * The delete will cascade down through directories as well.
         *
         * @param removeBefore only remove files created <b>before</b> this time
         * @return Returns the number of files removed
         */
        public static int removeFiles(long removeBefore)
        {
            File tempDir = CleaningTempFileProvider.getTempDir();
            return removeFiles(tempDir, removeBefore, removeBefore, false);
        }

        /**
         * @param directory the directory to clean out - the directory will optionally be removed
         * @param removeBefore only remove files created <b>before</b> this time
         * @param removeDir true if the directory must be removed as well, otherwise false
         * @return Returns the number of files removed
         */
        private static int removeFiles(File directory, long removeBefore, long longLifeBefore, boolean removeDir)
        {
            if (!directory.isDirectory())
            {
                throw new IllegalArgumentException("Expected a directory to clear: " + directory);
            }
            // check if there is anything to to
            if (!directory.exists())
            {
                return 0;
            }
            // list all files
            File[] files = directory.listFiles();
            int count = 0;
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    if(isLongLifeTempDir(file))
                    {
                        // long life for this folder and its children
                        int countRemoved = removeFiles(file, longLifeBefore, longLifeBefore, true);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Removed " + countRemoved + " files from temp directory: " + file);
                        }
                    }
                    else
                    {
                        // enter subdirectory and clean it out and remove itsynetics
                        int countRemoved = removeFiles(file, removeBefore, longLifeBefore, true);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Removed " + countRemoved + " files from directory: " + file);
                        }
                    }
                }
                else
                {
                    // it is a file - check the created time
                    if (file.lastModified() > removeBefore)
                    {
                        // file is not old enough
                        continue;
                    }
                    // it is a file - attempt a delete
                    try
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Deleting temp file: " + file);
                        }
                        file.delete();
                        count++;
                    }
                    catch (Throwable e)
                    {
                        logger.info("Failed to remove temp file: " + file);
                    }
                }
            }
            // must we delete the directory we are in?
            if (removeDir)
            {
                // the directory must be removed if empty
                try
                {
                    File[] listing = directory.listFiles();
                    if(listing != null && listing.length == 0)
                    {
                        // directory is empty
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("Deleting empty directory: " + directory);
                        }
                        directory.delete();
                    }
                }
                catch (Throwable e)
                {
                    logger.info("Failed to remove temp directory: " + directory, e);
                }
            }
            // done
            return count;
        }
    }
}
