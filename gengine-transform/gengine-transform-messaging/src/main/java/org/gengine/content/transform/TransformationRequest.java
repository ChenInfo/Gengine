package org.gengine.content.transform;

import java.util.List;

import org.gengine.content.AbstractContentRequest;
import org.gengine.content.ContentReference;
import org.gengine.content.transform.options.TransformationOptions;
import org.gengine.messaging.Request;

/**
 * Represents a request for content transformation from source to target with transformation options
 *
 */
public class TransformationRequest extends AbstractContentRequest implements Request<TransformationReply>
{
    private List<ContentReference> targetContentReferences;
    private TransformationOptions options;
    private String targetMediaType; // Added field

    public TransformationRequest()
    {
        super();
    }

    public TransformationRequest(
            List<ContentReference> sourceContentReferences,
            List<ContentReference> targetContentReferences,
            TransformationOptions options)
    {
        super();
        setSourceContentReferences(sourceContentReferences);
        this.targetContentReferences = targetContentReferences;
        this.options = options;
    }

    /**
     * Gets the target content reference objects
     *
     * @return target content references
     */
    public List<ContentReference> getTargetContentReferences()
    {
        return targetContentReferences;
    }

    /**
     * Sets the target content reference objects
     *
     * @param targetContentReferences
     */
    public void setTargetContentReferences(List<ContentReference> targetContentReferences)
    {
        this.targetContentReferences = targetContentReferences;
    }

    /**
     * Gets the options for the requested transformation
     *
     * @return the transformation options
     */
    public TransformationOptions getOptions()
    {
        return options;
    }

    /**
     * Sets the options for the requested transformation
     *
     * @param options
     */
    public void setOptions(TransformationOptions options)
    {
        this.options = options;
    }

    /**
     * Gets the target media type
     * 
     * @return target media type
     */
    public String getTargetMediaType()
    {
        return targetMediaType;
    }

    /**
     * Sets the target media type
     * 
     * @param targetMediaType
     */
    public void setTargetMediaType(String targetMediaType)
    {
        this.targetMediaType = targetMediaType;
    }

    @Override
    public Class<TransformationReply> getReplyClass()
    {
        return TransformationReply.class;
    }

}
