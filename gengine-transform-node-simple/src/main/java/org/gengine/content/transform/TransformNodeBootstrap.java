package org.gengine.content.transform;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.content.node.AbstractSimpleAmqpNodeBootstrap;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;

/**
 * Boostrap for transformer nodes.
 *
 */
public class TransformNodeBootstrap
        extends AbstractSimpleAmqpNodeBootstrap<AbstractContentTransformerWorker, BaseContentTransformerNode>
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
    protected BaseContentTransformerNode createNode(AbstractContentTransformerWorker worker)
    {
        BaseContentTransformerNode node = new BaseContentTransformerNode();
        node.setWorker(worker);
        return node;
    }

    @Override
    protected void initWorker(AbstractContentTransformerWorker worker)
    {
        worker.setSourceContentReferenceHandler(
                createFileContentReferenceHandler(PROP_WORKER_DIR_SOURCE));
        worker.setTargetContentReferenceHandler(
                createFileContentReferenceHandler(PROP_WORKER_DIR_TARGET));
        worker.init();
        logger.debug("Initialized " + worker.toString());
    }

    @Override
    protected void initNode(BaseContentTransformerNode node, AmqpDirectEndpoint endpoint)
    {
        if (node != null)
        {
            node.setMessageProducer(endpoint);
        }
    }

}
