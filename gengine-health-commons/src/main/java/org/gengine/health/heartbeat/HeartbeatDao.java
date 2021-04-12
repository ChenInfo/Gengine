package org.gengine.health.heartbeat;

/**
 * Defines the persistence of a heartbeat.  Implementations might
 * be a log, database, or in-memory queue.
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
