package org.gengine.content.dropwizard.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for source and target content reference handlers
 *
 */
public class ContentReferenceHandlersConfiguration
{
    @NotNull
    private ContentReferenceHandlerConfiguration source;

    @NotNull
    private ContentReferenceHandlerConfiguration target;

    @JsonProperty
    public ContentReferenceHandlerConfiguration getSource()
    {
        return source;
    }

    @JsonProperty
    public void setSource(ContentReferenceHandlerConfiguration source)
    {
        this.source = source;
    }

    @JsonProperty
    public ContentReferenceHandlerConfiguration getTarget()
    {
        return target;
    }

    @JsonProperty
    public void setTarget(ContentReferenceHandlerConfiguration target)
    {
        this.target = target;
    }

}
