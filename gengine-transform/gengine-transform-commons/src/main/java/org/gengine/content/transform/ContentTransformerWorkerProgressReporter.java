package org.gengine.content.transform;

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
     */
    public void onTransformationStarted();

    /**
     * Optionally called when some amount of progress has been made on
     * the transformation
     *
     * @param progress
     */
    public void onTransformationProgress(float progress);

    /**
     * Called when the transformation has completed
     */
    public void onTransformationComplete();
}
