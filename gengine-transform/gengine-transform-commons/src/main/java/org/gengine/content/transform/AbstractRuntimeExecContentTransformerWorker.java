package org.gengine.content.transform;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.transform.AbstractFileContentTransformerWorker;
import org.gengine.util.exec.RuntimeExec;
import org.gengine.util.exec.RuntimeExec.ExecutionResult;

/**
 * Base class that uses a command line {@link RuntimeExec} to perform the transform
 *
 */
public abstract class AbstractRuntimeExecContentTransformerWorker extends AbstractFileContentTransformerWorker
{
    private static final Log logger = LogFactory.getLog(AbstractRuntimeExecContentTransformerWorker.class);

    /** source variable name */
    protected static final String VAR_SOURCE = "source";

    /** target variable name */
    protected static final String VAR_TARGET = "target";

    /** the system command executer */
    protected RuntimeExec executer;
    /** the check command executer */
    protected RuntimeExec versionDetailsExecuter;
    /** optional file details executer */
    protected RuntimeExec fileDetailsExecuter;

    /**
     * Default constructor
     */
    public AbstractRuntimeExecContentTransformerWorker()
    {
        // Intentionally empty
    }

    /**
     * Set the runtime command executer that must be executed.
     * Whether or not this is the full path to the convertCommand
     * or just the convertCommand itself depends the environment setup.
     * <p>
     *
     * @param executer the system command executer
     */
    public void setExecuter(RuntimeExec executer)
    {
        this.executer = executer;
    }

    /**
     * Sets the command that must be executed in order to retrieve version information from the converting executable
     * and thus test that the executable itself is present.
     *
     * @param versionDetailsExecuter
     *            command executer to retrieve version information
     */
    public void setVersionDetailsExecuter(RuntimeExec versionDetailsExecuter)
    {
        this.versionDetailsExecuter = versionDetailsExecuter;
    }

    /**
     * Perform any initialization needed for the primary executer including creating if null
     */
    protected abstract void initializeExecuter();

    /**
     * Perform any initialization needed for the version details executer including creating if null
     */
    protected abstract void initializeVersionDetailsExecuter();


    /**
     * Perform any initialization needed for the file details executer including creating if null
     */
    protected abstract void initializeFileDetailsExecuter();

    /**
     * Perform a transformation that verifies the executer is installed and configured correctly
     */
    protected abstract void initializationTest();

    /**
     * Checks for the JMagick and ImageMagick dependencies, using the common
     * {@link #transformInternal(File, File) transformation method} to check
     * that the sample image can be converted.
     */
    @Override
    public void initialize()
    {
        try
        {
            loadProperties();

            initializeExecuter();
            initializeVersionDetailsExecuter();
            initializeFileDetailsExecuter();

            initializeVersionString();
            initializeVersionDetailsString();

            initializationTest();
            setIsAvailable(true);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
            setIsAvailable(false);
        }
    }

    @Override
    protected void initializeVersionDetailsString()
    {
        if (this.versionDetailsExecuter == null)
        {
            return;
        }
        if (logger.isDebugEnabled())
        {
            logger.debug(this.getClass().getSimpleName() + " version details check...");
        }
        try
        {
            // On some platforms / versions / executables, the version command seems to
            // return an error code whilst still
            // returning output, so let's not worry about the exit code!
            ExecutionResult result = this.versionDetailsExecuter.execute();
            this.versionDetailsString = result.getStdOut().trim();
        }
        catch (Throwable e)
        {
            setIsAvailable(false);
            logger.error(getClass().getSimpleName() + " not available: "
                    + (e.getMessage() != null ? e.getMessage() : ""));
            // debug so that we can trace the issue if required
            logger.debug(e);
        }
    }

    /**
     * Optional method for implementations able to return details of a file which will
     * vary greatly depending on the file type and implementation.
     *
     * @param file
     * @return a simple string of file detail output
     * @throws Exception
     */
    public String getDetails(File file) throws Exception
    {
        if (fileDetailsExecuter == null)
        {
            return null;
        }
        Map<String, String> properties = new HashMap<String, String>(1);
        properties.put(VAR_SOURCE, file.getAbsolutePath());

        try
        {
            // On some platforms / versions / executables, the command seems to
            // return an error code whilst still
            // returning output or error, so let's not worry about the exit code!
            ExecutionResult result = this.fileDetailsExecuter.execute(properties);
            String out = result.getStdOut().trim();
            if (!out.equals(""))
            {
                return out;
            }
            return result.getStdErr().trim();
        }
        catch (Throwable e)
        {
            logger.info(getClass().getSimpleName() + " could not get details: "
                    + (e.getMessage() != null ? e.getMessage() : ""));
        }
        return null;
    }
}
