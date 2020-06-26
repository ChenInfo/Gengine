package org.gengine.content.transform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.gengine.messaging.MessageConsumer;

/**
 * Boostrap for transformer nodes.
 *
 */
public class TransformNodeBootstrap extends AbstractSimpleAmqpNodeBootstrap<AbstractContentTransformerWorker>
{
    /**
     * Logger for this class
     */
    private static final Log logger = LogFactory.getLog(TransformNodeBootstrap.class);

    protected static final String PROP_WORKER_DIR_TARGET = "gengine.worker.dir.target";

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        TransformNodeBootstrap bootstrap = new TransformNodeBootstrap();
        bootstrap.run(args);
    }

    @Override
    protected MessageConsumer getMessageConsumer()
    {
        AbstractContentTransformerWorker worker = createWorker();
        worker.setSourceContentReferenceHandler(
                createFileContentReferenceHandler(PROP_WORKER_DIR_SOURCE));
        worker.setTargetContentReferenceHandler(
                createFileContentReferenceHandler(PROP_WORKER_DIR_TARGET));
        worker.init();
        logger.debug("Initialized " + worker.toString());

        BaseContentTransformerNode node = new BaseContentTransformerNode();
        node.setWorker(worker);

        return node;
    }

}
