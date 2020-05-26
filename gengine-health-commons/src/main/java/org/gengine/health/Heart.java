package org.gengine.health;

/**
 * Defines the object responsible for producing {@link Heartbeat}s
 * which let components monitor health of other components in the system.
 *
 */
public interface Heart
{
    /**
     * Produces a {@link Heartbeat} for health monitoring
     */
    public void beat();

}
