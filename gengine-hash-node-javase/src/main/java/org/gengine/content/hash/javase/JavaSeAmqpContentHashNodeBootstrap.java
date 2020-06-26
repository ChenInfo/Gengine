package org.gengine.content.hash.javase;

import org.gengine.content.hash.BaseContentHashNode;
import org.gengine.content.transform.AbstractSimpleAmqpNodeBootstrap;
import org.gengine.messaging.MessageConsumer;

/**
 * Starts up an AMQP Java SE hash node via command line arguments
 *
 */
public class JavaSeAmqpContentHashNodeBootstrap extends AbstractSimpleAmqpNodeBootstrap<JavaSeContentHashNodeWorker>
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
    protected MessageConsumer getMessageConsumer()
    {
        JavaSeContentHashNodeWorker worker = createWorker();
        worker.setContentReferenceHandler(
                createFileContentReferenceHandler(AbstractSimpleAmqpNodeBootstrap.PROP_WORKER_DIR_SOURCE));

        BaseContentHashNode node = new BaseContentHashNode();
        node.setWorker(createWorker());

        return node;
    }

}
