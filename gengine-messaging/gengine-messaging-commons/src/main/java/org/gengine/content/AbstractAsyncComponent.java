package org.gengine.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.gengine.messaging.Reply;
import org.gengine.messaging.Request;

/**
 * Base component which pulls a message off the queue before performing the work, useful
 * when progress reporting is needed as some messaging endpoints will lock the session
 * until the message consumption is complete and progress replies can not be sent.
 *
 * @param <W>
 * @param <RQ>
 * @param <RP>
 */
public abstract class AbstractAsyncComponent<W extends ContentWorker, RQ extends Request<RP>, RP extends Reply> extends AbstractComponent<W>
{
    /**
     * Logger for this class
     */
    private static final Log logger = LogFactory.getLog(AbstractAsyncComponent.class);

    private final BlockingQueue<RQ> localQueue =  new SynchronousQueue<RQ>();

    @SuppressWarnings("unchecked")
    protected void onReceiveImpl(Object message)
    {
        RQ request = (RQ) message;
        try
        {
            localQueue.put(request);
        }
        catch (InterruptedException e)
        {
        }
    }

    /**
     * Performs the actual work for the request.
     *
     * @param request
     */
    protected abstract void processRequest(RQ request);

    /**
     * Takes from the local concurrent queue and hands them off to be processed
     *
     */
    protected class LocalQueueProcessor implements Runnable
    {
        private final BlockingQueue<RQ> localProcessorQueue;

        public LocalQueueProcessor(BlockingQueue<RQ> localProcessorQueue)
        {
            this.localProcessorQueue = localProcessorQueue;
        }

        @Override
        public void run()
        {
            logger.debug("Starting local queue processing");
            while(true)
            {
                try
                {
                    RQ request = localProcessorQueue.take();
                    logger.debug("Processing local queue message");
                    processRequest(request);
                    logger.debug("Processing local queue message complete");
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }

    public void init()
    {
        super.init();
        executorService.execute(new LocalQueueProcessor(localQueue));
    }

}
