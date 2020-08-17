package org.gengine.messaging;

import java.util.Map;

/**
 * Defines methods for sending message objects to a queue.
 *
 */
public interface MessageProducer
{

    /**
     * Send the given POJO message to the default queue for the producer
     *
     * @param message
     * @throws MessagingException
     */
    public void send(Object message) throws MessagingException;

    /**
     * Send the given POJO message to the default queue for the producer with the given headers
     *
     * @param message
     * @param headers
     * @throws MessagingException
     */
    public void send(Object message, Map<String, Object> headers) throws MessagingException;

    /**
     * Send the given POJO message to the given queue
     *
     * @param message
     * @param queueName
     * @throws MessagingException
     */
    public void send(Object message, String queueName) throws MessagingException;

    /**
     * Send the given POJO message to the given queue with the given headers
     *
     * @param message
     * @param queueName
     * @param headers
     * @throws MessagingException
     */
    public void send(Object message, String queueName, Map<String, Object> headers) throws MessagingException;

}
