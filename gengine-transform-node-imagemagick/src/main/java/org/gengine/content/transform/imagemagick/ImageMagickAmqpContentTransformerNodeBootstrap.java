package org.gengine.content.transform.imagemagick;

import org.gengine.content.handler.FileContentReferenceHandlerImpl;
import org.gengine.content.transform.BaseContentTransformerNode;
import org.gengine.content.transform.imagemagick.ImageMagickContentTransformerWorker;
import org.gengine.messaging.amqp.AmqpDirectEndpoint;
import org.gengine.messaging.amqp.AmqpNodeBootstrapUtils;

/**
 * Starts up an AMQP FFmpeg transformer node via command line arguments
 *
 */
public class ImageMagickAmqpContentTransformerNodeBootstrap
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        AmqpNodeBootstrapUtils.validateArguments(args);

        ImageMagickContentTransformerWorker worker = new ImageMagickContentTransformerWorker();
        worker.setContentReferenceHandler(new FileContentReferenceHandlerImpl());
        worker.init();

        BaseContentTransformerNode node = new BaseContentTransformerNode();
        node.setWorker(worker);

        AmqpDirectEndpoint endpoint = AmqpNodeBootstrapUtils.createEndpoint(node, args);
        node.setMessageProducer(endpoint);

        endpoint.startListener();
    }

}
