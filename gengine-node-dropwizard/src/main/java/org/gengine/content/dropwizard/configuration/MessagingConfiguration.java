package org.gengine.content.dropwizard.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for messaging
 *
 */
public class MessagingConfiguration
{
    @NotNull
    private BrokerConfiguration broker;

    @JsonProperty
    public BrokerConfiguration getBroker()
    {
        return broker;
    }

    @JsonProperty
    public void setBrokerConfiguration(BrokerConfiguration broker)
    {
        this.broker = broker;
    }

}
