package org.gengine.content.dropwizard.configuration;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

/**
 * Configuration for the Gengine node
 */
public class NodeConfiguration extends Configuration
{
    @NotEmpty
    private String sourceDirectory;

    @NotEmpty
    private String targetDirectory;

    @Valid
    @NotNull
    private MessagingConfiguration messagingConfig;

    @Valid
    private List<ComponentConfiguration> components;

    private String version;

    @JsonProperty
    public String getSourceDirectory()
    {
        return sourceDirectory;
    }

    @JsonProperty
    public void setSourceDirectory(String sourceDirectory)
    {
        this.sourceDirectory = sourceDirectory;
    }

    @JsonProperty
    public String getTargetDirectory()
    {
        return targetDirectory;
    }

    @JsonProperty
    public void setTargetDirectory(String targetDirectory)
    {
        this.targetDirectory = targetDirectory;
    }

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
