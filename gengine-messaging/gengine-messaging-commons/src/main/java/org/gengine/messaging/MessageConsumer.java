package org.gengine.messaging;

/**
 * Defines methods for handling messages.  A separate message listener is
 * responsible for pulling messages off a queue and passing them to the consumer.
 *
 */
public interface MessageConsumer
{

    /**
     * Performs any processing required upon receiving the given POJO message
     *
     * @param message
     */
    public void onReceive(Object message);

    /**
     * The class of POJO messages expected
     *
     * @return the POJO message class
     */
    public Class<?> getConsumingMessageBodyClass();

}
