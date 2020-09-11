package org.gengine.content.transform;


import org.gengine.content.AbstractContentReply;
import org.gengine.messaging.Request;

/**
 * Represents a reply from a content transformer on the status of a transformation request.
 *
 */
public class TransformationReply extends AbstractContentReply
{
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_IN_PROGRESS = "in-progress";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_ERROR = "error";

    private String status;
    private String jobId;
    private Float progress;
    private String statusDetail;

    public TransformationReply()
    {
        super();
    }

    public TransformationReply(Request<?> request)
    {
        super(request);
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
    public String getStatusDetail()
    {
        return statusDetail;
    }

    /**
     * Sets further detail on the status of the transformation, possibly messages relating to an error
     *
     * @param statusDetail
     */
    public void setStatusDetail(String statusDetail)
    {
        this.statusDetail = statusDetail;
    }

}
