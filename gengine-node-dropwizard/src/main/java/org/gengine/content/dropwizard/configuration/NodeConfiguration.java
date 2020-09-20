package org.gengine.content.dropwizard.configuration;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

/**
 * Configuration for the Gengine node
 *
 */
public class NodeConfiguration extends Configuration
{

    @Valid
    @NotNull
    private MessagingConfiguration messagingConfig;

    @Valid
    @NotNull
    private ContentReferenceHandlersConfiguration contentReferenceHandlersConfig;

    @Valid
    private List<ComponentConfiguration> components;

    private String version;

    @JsonProperty("messaging")
    public MessagingConfiguration getMessagingConfig()
    {
        return messagingConfig;
    }

    @JsonProperty("messaging")
    public void setMessagingConfig(MessagingConfiguration messagingConfig)
    {
        this.messagingConfig = messagingConfig;
    }

    @JsonProperty("contentReferenceHandlers")
    public ContentReferenceHandlersConfiguration getContentReferenceHandlersConfig()
    {
        return contentReferenceHandlersConfig;
    }

    @JsonProperty("contentReferenceHandlers")
    public void setContentReferenceHandlersConfig(ContentReferenceHandlersConfiguration contentReferenceHandlersConfig)
    {
        this.contentReferenceHandlersConfig = contentReferenceHandlersConfig;
    }

    @JsonProperty()
    public List<ComponentConfiguration> getComponents()
    {
        return components;
    }

    @JsonProperty()
    public void setComponents(List<ComponentConfiguration> components)
    {
        this.components = components;
    }

    @JsonProperty
    public String getVersion()
    {
        return version;
    }

    @JsonProperty
    public void setVersion(String version)
    {
        this.version = version;
    }

}
