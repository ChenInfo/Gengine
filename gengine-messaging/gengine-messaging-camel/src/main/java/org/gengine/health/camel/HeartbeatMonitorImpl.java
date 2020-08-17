package org.gengine.health.camel;

import org.apache.camel.Handler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gengine.health.Heartbeat;
import org.gengine.health.HeartbeatDao;
import org.gengine.health.HeartbeatMonitor;

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
           logger.info("Heartbeat message expected but received: " + message.toString());
           message = new Heartbeat("bogus: " + message.toString(), null);
           //return;
        }
        heartbeatDao.record((Heartbeat) message);
    }

}
