package org.gengine.health;

/**
 * Defines the persistence of a heartbeat.  Implementations might
 * be a log or database
 *
 */
public interface HeartbeatDao
{

    /**
     * Persists the heartbeat.
     *
     * @param heartbeat
     */
    public void record(Heartbeat heartbeat);

}
