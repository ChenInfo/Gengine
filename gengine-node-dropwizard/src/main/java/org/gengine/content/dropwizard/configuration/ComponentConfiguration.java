package org.gengine.content.dropwizard.configuration;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration POJO for a component
 *
 */
public class ComponentConfiguration
{
    @NotNull
    private String name;

    @NotNull
    private String workerClass;

    @NotNull
    private boolean enabled;

    @NotEmpty
    private String requestQueue;

    @NotEmpty
    private String replyQueue;

    @JsonProperty
    public String getName()
    {
        return name;
    }

    @JsonProperty
    public void setName(String name)
    {
        this.name = name;
    }

    @JsonProperty
    public String getWorkerClass()
    {
        return workerClass;
    }

    @JsonProperty
    public void setWorkerClass(String workerClass)
    {
        this.workerClass = workerClass;
    }

    @JsonProperty
    public boolean getEnabled()
    {
        return enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @JsonProperty
    public String getRequestQueue()
    {
        return requestQueue;
    }

    @JsonProperty
    public void setRequestQueue(String requestQueue)
    {
        this.requestQueue = requestQueue;
    }

    @JsonProperty
    public String getReplyQueue()
    {
        return replyQueue;
    }

    @JsonProperty
    public void setReplyQueue(String replyQueue)
    {
        this.replyQueue = replyQueue;
    }

}
