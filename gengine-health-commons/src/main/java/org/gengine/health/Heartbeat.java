package org.gengine.health;

/**
 * An object describing a properly behaving resource at a particular
 * point in time.
 *
 */
public class Heartbeat
{

    private String componentId;
    private String instanceId;
    private Long time;

    public Heartbeat()
    {
    }

    public Heartbeat(String componentId, String instanceId)
    {
        this.componentId = componentId;
        this.instanceId = instanceId;
        this.time = System.currentTimeMillis();
    }

    /**
     * Gets the component identifier.
     *
     * @return the component ID
     */
    public String getComponentId()
    {
        return componentId;
    }

    /**
     * Sets the component identifier.
     *
     * @param component
     */
    public void setComponentId(String component)
    {
        this.componentId = component;
    }

    /**
     * Gets the identifier for a particular instance of a component.
     *
     * @return the component instance ID
     */
    public String getInstanceId()
    {
        return instanceId;
    }

    /**
     * Sets the identifier for a particular instance of a component.
     *
     * @param instanceId
     */
    public void setInstanceId(String instanceId)
    {
        this.instanceId = instanceId;
    }

    /**
     * Gets the epoch time of the health check.
     *
     * @return the epoch time of the heartbeat
     */
    public Long getTime()
    {
        return time;
    }

    /**
     * Sets the epoch time of the health check.
     *
     * @param time
     */
    public void setTime(Long time)
    {
        this.time = time;
    }

}
