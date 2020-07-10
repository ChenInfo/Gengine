package org.gengine.content.hash.javase;

import org.gengine.content.hash.BaseContentHashNode;
import org.gengine.content.node.AbstractSimpleAmqpNodeBootstrap;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;

/**
 * Starts up an AMQP Java SE hash node via command line arguments
 *
 */
public class JavaSeAmqpContentHashNodeBootstrap
        extends AbstractSimpleAmqpNodeBootstrap<JavaSeContentHashNodeWorker, BaseContentHashNode>
{
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        JavaSeAmqpContentHashNodeBootstrap bootstrap = new JavaSeAmqpContentHashNodeBootstrap();
        bootstrap.run(args);
    }

    @Override
    protected BaseContentHashNode createNode(JavaSeContentHashNodeWorker worker)
    {
        BaseContentHashNode node = new BaseContentHashNode();
        node.setWorker(worker);
        return node;
    }

    @Override
    protected void initWorker(JavaSeContentHashNodeWorker worker)
    {
        worker.setContentReferenceHandler(
                createFileContentReferenceHandler(AbstractSimpleAmqpNodeBootstrap.PROP_WORKER_DIR_SOURCE));
    }

    @Override
    protected void initNode(BaseContentHashNode node, AmqpDirectEndpoint endpoint)
    {
        if (node != null)
        {
            node.setMessageProducer(endpoint);
        }
    }

}
