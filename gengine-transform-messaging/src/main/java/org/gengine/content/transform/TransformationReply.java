package org.gengine.content.transform;

import java.util.List;

import org.gengine.content.ContentReference;
import org.gengine.messaging.Reply;

/**
 * Represents a reply from a content transformer on the status of a transformation request.
 */
public class TransformationReply implements Reply
{
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_IN_PROGRESS = "in-progress";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_ERROR = "error";

    private String requestId;
    private List<ContentReference> targetContentReferences;
    private String status;
    private String jobId;
    private Float progress;
    private String detail;

    public TransformationReply() {
        super();
    }

    public TransformationReply(TransformationRequest request)
    {
        super();
        this.requestId = request.getRequestId();
        this.targetContentReferences = request.getTargetContentReferences();
    }

    /**
     * Gets the UUID for the original transformation request
     *
     * @return the transformation request ID
     */
    public String getRequestId()
    {
        return requestId;
    }

    /**
     * Sets the UUID for the original transformation request
     *
     * @param requestId
     */
    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    /**
     * Gets the target content reference objects.
     * <p>
     * This is usually the same as the content references provided in the original
     * transformation request.
     *
     * @return the target content references
     */
    public List<ContentReference> getTargetContentReferences()
    {
        return targetContentReferences;
    }

    /**
     * Sets the target content reference objects.
     * <p>
     * This is usually the same as the content references provided in the original
     * transformation request.
     *
     * @param targetContentReferences
     */
    public void setTargetContentReferences(List<ContentReference> targetContentReferences)
    {
        this.targetContentReferences = targetContentReferences;
    }

    /**
     * Gets the status of the transformation
     *
     * @return the status
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * Sets the status of the transformation
     *
     * @param status
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * Gets the implementation's job ID for the transformation, separation from the
     * Gengine transformation request ID
     *
     * @return the implementation job ID
     */
    public String getJobId()
    {
        return jobId;
    }

    /**
     * Sets the implementation's job ID for the transformation, separation from the
     * Gengine transformation request ID
     *
     * @param jobId
     */
    public void setJobId(String jobId)
    {
        this.jobId = jobId;
    }

    /**
     * Gets the percentage progress of the transformation
     *
     * @return the transformation progress
     */
    public Float getProgress()
    {
        return progress;
    }

    /**
     * Sets the percentage progress of the transformation
     *
     * @param progress
     */
    public void setProgress(Float progress)
    {
        this.progress = progress;
    }

    /**
     * Gets further detail on the status of the transformation, possibly messages relating to an error
     *
     * @return the transformation status detail
     */
    public String getDetail()
    {
        return detail;
    }

    /**
     * Sets further detail on the status of the transformation, possibly messages relating to an error
     *
     * @param detail
     */
    public void setDetail(String detail)
    {
        this.detail = detail;
    }

}
