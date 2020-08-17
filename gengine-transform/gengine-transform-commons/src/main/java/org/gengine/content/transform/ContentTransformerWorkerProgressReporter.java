package org.gengine.content.transform;

import org.gengine.content.transform.ContentTransformationException;

/**
 * Defines methods for reporting progress on a content transformation.
 * <p>
 * Implementations might send replies via messaging system or just log
 * progress.
 *
 */
public interface ContentTransformerWorkerProgressReporter
{
    /**
     * Called when the transformation has been started
     *
     * @throws ContentTransformationException
     */
    public void onTransformationStarted() throws ContentTransformationException;

    /**
     * Optionally called when some amount of progress has been made on
     * the transformation
     *
     * @param progress
     * @throws ContentTransformationException
     */
    public void onTransformationProgress(float progress) throws ContentTransformationException;

    /**
     * Called when the transformation has completed
     *
     * @throws ContentTransformationException
     */
    public void onTransformationComplete() throws ContentTransformationException;
}
