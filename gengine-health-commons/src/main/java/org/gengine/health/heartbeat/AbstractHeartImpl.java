package org.gengine.health.heartbeat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Abstract heart implementation for sending {@link Heartbeat} messages.
 *
 */
public abstract class AbstractHeartImpl implements Heart
{
    private static final Log logger = LogFactory.getLog(AbstractHeartImpl.class);

    public static final Object SERVICE_INSTANCE_ID_FLAG_GENERATE_UUID = "--GENERATE-UUID--";

    protected static final long DEFAULT_TIMER_DELAY_MS = 2000;
    protected static final long DEFAULT_TIMER_PERIOD_MS = 2000;

    protected String serviceType;
    protected String serviceInstanceId;
    protected Timer timer;
    protected long timerPeriodMs = DEFAULT_TIMER_PERIOD_MS;

    /**
     * Sets the service type to be used for {@link Heartbeat} messages.
     *
     * @param serviceType
     */
    public void setServiceType(String serviceType)
    {
        this.serviceType = serviceType;
    }

    /**
     * Sets the service instance ID to be used for {@link Heartbeat} messages.
     *
     * @param serviceInstanceId
     */
    public void setServiceInstanceId(String serviceInstanceId)
    {
        if (SERVICE_INSTANCE_ID_FLAG_GENERATE_UUID.equals(serviceInstanceId))
        {
            this.serviceInstanceId = UUID.randomUUID().toString();
        }
        else
        {
            this.serviceInstanceId = serviceInstanceId;
        }
    }

    /**
     * The period of heartbeat messages
     *
     * @param timerPeriodMs
     */
    public void setTimerPeriodMs(long timerPeriodMs)
    {
        this.timerPeriodMs = timerPeriodMs;
    }

    @Override
    public void start()
    {
        if (serviceInstanceId == null)
        {
            serviceInstanceId = UUID.randomUUID().toString();
        }
        logger.info("Starting heartbeat for " + serviceType + " id=" + serviceInstanceId);
        timer = new Timer();
        timer.schedule(new HeartbeatTask(), DEFAULT_TIMER_DELAY_MS, timerPeriodMs);
    }

    @Override
    public void stop()
    {
        timer.cancel();
    }

    /**
     * Gathers any useful health metrics for the details section of
     * each heartbeat messages.
     *
     * @return the status details
     */
    protected abstract Map<String, String> gatherDetails();

    /**
     * Sends the given heartbeat message
     *
     * @param heartbeat
     */
    protected abstract void send(Heartbeat heartbeat);

    protected class HeartbeatTask extends TimerTask
    {
        @Override
        public void run()
        {
            try
            {
                Map<String, String> details = gatherDetails();
                Heartbeat heartbeat =
                        new Heartbeat(serviceType, serviceInstanceId, true, details);
                send(heartbeat);
            }
            catch (Exception e)
            {
                logger.error(e.getMessage(), e);
                timer.cancel();
            }
        }

    }
}
