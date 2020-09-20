package org.gengine.content.dropwizard.configuration;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for known content reference handlers
 *
 */
public class ContentReferenceHandlerConfiguration
{
    @NotNull
    private String contentReferenceHandlerClass;

    // Must support all known content reference handler values here
    private String directoryPath;
    private String remoteBaseUrl;
    private String username;
    private String password;
    private String s3AccessKey;
    private String s3SecretKey;
    private String s3BucketName;
    private String s3BucketRegion;

    @JsonProperty
    public String getContentReferenceHandlerClass()
    {
        return contentReferenceHandlerClass;
    }

    @JsonProperty
    public void setContentReferenceHandlerClass(String contentReferenceHandlerClass)
    {
        this.contentReferenceHandlerClass = contentReferenceHandlerClass;
    }

    @JsonProperty
    public String getDirectoryPath()
    {
        return directoryPath;
    }

    @JsonProperty
    public void setDirectoryPath(String directoryPath)
    {
        this.directoryPath = directoryPath;
    }

    @JsonProperty
    public String getRemoteBaseUrl()
    {
        return remoteBaseUrl;
    }

    @JsonProperty
    public void setRemoteBaseUrl(String remoteBaseUrl)
    {
        this.remoteBaseUrl = remoteBaseUrl;
    }

    @JsonProperty
    public String getUsername()
    {
        return username;
    }

    @JsonProperty
    public void setUsername(String username)
    {
        this.username = username;
    }

    @JsonProperty
    public String getPassword()
    {
        return password;
    }

    @JsonProperty
    public void setPassword(String password)
    {
        this.password = password;
    }

    @JsonProperty
    public String getS3AccessKey()
    {
        return s3AccessKey;
    }

    @JsonProperty
    public void setS3AccessKey(String s3AccessKey)
    {
        this.s3AccessKey = s3AccessKey;
    }

    @JsonProperty
    public String getS3SecretKey()
    {
        return s3SecretKey;
    }

    @JsonProperty
    public void setS3SecretKey(String s3SecretKey)
    {
        this.s3SecretKey = s3SecretKey;
    }

    @JsonProperty
    public String getS3BucketName()
    {
        return s3BucketName;
    }

    @JsonProperty
    public void setS3BucketName(String s3BucketName)
    {
        this.s3BucketName = s3BucketName;
    }

    @JsonProperty
    public String getS3BucketRegion()
    {
        return s3BucketRegion;
    }

    @JsonProperty
    public void setS3BucketRegion(String s3BucketRegion)
    {
        this.s3BucketRegion = s3BucketRegion;
    }

}
