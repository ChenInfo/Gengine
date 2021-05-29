package org.gengine.health.heartbeat;

/**
 * Defines the object responsible for producing {@link Heartbeat}s
 * which let services monitor health of other services in the system.
 *
 */
public interface Heart
{
    /**
     * Starts the repeated sending of {@link Heartbeat} messages
     * for health monitoring
     */
    public void start();

    /**
     * Stops the repeated sending of {@link Heartbeat} messages
     * for health monitoring
     */
    public void stop();

    /**
     * Returns a single {@link Heartbeat} object without sending
     * via messaging
     *
     * @return the heartbeat object
     */
    public Heartbeat beat();

}
