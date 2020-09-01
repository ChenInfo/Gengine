package org.gengine.messaging.camel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.gengine.content.ContentIOException;
import org.gengine.messaging.Reply;
import org.gengine.messaging.Request;
import org.gengine.messaging.RequestReplyMessageProducer;

/**
 * An Apache Camel implementation of a request-reply message producer.
 * <p>
 * A pending requests map is maintained and the {@link ReplyCallable} periodically polls
 * that map to determine if a correlated reply has been received.
 * <p>
 * Note that the built-in Camel asynchronous processing was not used for a few reasons:
 * <ul>
 *    <li>Routing of the temp queues back to the appropriate location and custom unmarshalling proved problematic</li>
 *    <li>Several references stated that there could be additional overhead in constructing the temp queues even with caching</li>
 *    <li>Clustering of pending requests would have likely been much more difficult</li>
 * </ul>
 *
 * @param <RQ> the request type
 * @param <RP> the reply type
 */
public class CamelRequestReplyMessageProducer<RQ extends Request<RP>, RP extends Reply>
        extends CamelMessageProducer
        implements RequestReplyMessageProducer<RQ, RP>
{
    private static final Log logger = LogFactory.getLog(CamelRequestReplyMessageProducer.class);

    private static final long DEFAULT_PENDING_REQUEST_POLLING_INTERVAL_MS = 500;
    private static final long DEFAULT_PENDING_REQUEST_TIMEOUT_MS = 20000;

    // TODO: In a clustered env this would have to be distributed (Hazelcast) or persisted
    protected Map<String, RP> pendingRequests = new HashMap<String, RP>();

    protected long pollingIntervalMs = DEFAULT_PENDING_REQUEST_POLLING_INTERVAL_MS;
    protected long timeoutMs = DEFAULT_PENDING_REQUEST_TIMEOUT_MS;

    protected ExecutorService executorService;

    /**
     * The pending request polling interval in milliseconds
     *
     * @param pollingIntervalMs
     */
    public void setPollingIntervalMs(long pollingIntervalMs)
    {
        this.pollingIntervalMs = pollingIntervalMs;
    }

    /**
     * The pending request timeout in milliseconds
     *
     * @param timeoutMs
     */
    public void setTimeoutMs(long timeoutMs)
    {
        this.timeoutMs = timeoutMs;
    }

    /**
     * The executor service to be used for executing the polling callable
     *
     * @param executorService
     */
    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    public void init()
    {
        if (executorService == null)
        {
            executorService = Executors.newCachedThreadPool();
        }
    }

    @Override
    public Future<RP> asyncRequest(RQ request)
    {
        send(request);
        pendingRequests.put(request.getRequestId(), null);

        FutureTask<RP> future = new FutureTask<>(new ReplyCallable(request.getRequestId()));
        executorService.execute(future);
        return future;
    }

    @SuppressWarnings("unchecked")
    public void onReceive(Object message)
    {
        // TODO Better handling of checking object type and casting
        RP reply = (RP) message;

        if (logger.isDebugEnabled())
        {
            logger.debug("Received reply for request " + reply.getRequestId());
        }

        if (!pendingRequests.containsKey(reply.getRequestId()))
        {
         // TODO Need to better handle errors here, send an error message?
            logger.error("Unknown pending request: " +
                    reply.getRequestId());
            return;
        }

        pendingRequests.put(reply.getRequestId(), reply);
    }

    /**
     * Class which polls the pending requests map to determine if a
     * correlated reply has been received.
     */
    public class ReplyCallable implements Callable<RP>
    {
        private String requestId;

        /**
         * Constructor which takes a correlating request ID
         *
         * @param requestId
         */
        public ReplyCallable(String requestId)
        {
            this.requestId = requestId;
        }

        @Override
        public RP call() throws Exception
        {
            long startTime = (new Date()).getTime();
            try {
                while (((new Date()).getTime() - startTime <= timeoutMs) || timeoutMs == -1)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Polling for pending request " + requestId +
                                " completion in " + pollingIntervalMs + "ms...");
                    }
                    Thread.sleep(pollingIntervalMs);
                    RP reply = pendingRequests.get(
                            requestId);

                    if (reply == null)
                    {
                        // reply hasn't come back yet
                        continue;
                    }

                    pendingRequests.remove(requestId);
                    return reply;
                }
                // We must have timed out
                throw new ContentIOException("Timeout while waiting for reply");
            }
            catch (InterruptedException e)
            {
                // We were asked to stop
                return null;
            }
        }
    }

}
