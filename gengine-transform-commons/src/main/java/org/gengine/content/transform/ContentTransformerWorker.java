package org.gengine.content.transform;

import java.util.List;

import org.gengine.content.ContentReference;
import org.gengine.content.ContentWorker;
import org.gengine.content.transform.options.TransformationOptions;

/**
 * Defines the methods responsible for doing the work of transformation of a content reference
 *
 */
public interface ContentTransformerWorker extends ContentWorker
{
    /**
     * Transforms the given source content references into the given target content references
     * with the given options, reporting back via the given progress reporter.
     * <p>
     * Depending on the transformation being requests, multiple sources may be merged into a single target,
     * a single source may be split into multiple targets, multiple sources may be transformed to
     * multiple targets, etc.  It is up to the caller to know what type of targets to expect.
     * <p>
     * Additionally, some implementations may not be able to transform into the targets specified or
     * the number of targets may not be known before hand so implementations must return the
     * final target references.
     *
     * @param sources the list of source content references
     * @param targets the list of target content references which may be null indicating targets should be created
     * @param options
     * @param progressReporter
     * @return the final target references
     * @throws Exception
     */
    public List<ContentReference> transform(
            List<ContentReference> sources,
            List<ContentReference> targets,
            TransformationOptions options,
            ContentTransformerWorkerProgressReporter progressReporter) throws Exception;
}
