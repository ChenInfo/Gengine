package org.gengine.health.camel;

import org.apache.camel.Handler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.health.heartbeat.Heartbeat;
import org.gengine.health.heartbeat.HeartbeatDao;
import org.gengine.health.heartbeat.HeartbeatMonitor;

/**
 * HeartbeatMonitor implementation which uses Camel to route {@link Heartbeat}
 * messages.
 *
 */
public class HeartbeatMonitorImpl implements HeartbeatMonitor
{
    private static final Log logger = LogFactory.getLog(HeartbeatMonitorImpl.class);

    private HeartbeatDao heartbeatDao;

    /**
     * Sets the DAO used to persist heartbeats.
     *
     * @param heartbeatDao
     */
    public void setHeartbeatDao(HeartbeatDao heartbeatDao)
    {
        this.heartbeatDao = heartbeatDao;
    }

    @Override
    @Handler
    public void onReceive(Object message)
    {
        if (!(message instanceof Heartbeat))
        {
           logger.warn("Heartbeat message expected but received: " + message.toString());
           return;
        }
        heartbeatDao.record((Heartbeat) message);
    }

}
