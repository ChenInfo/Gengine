package org.gengine.content.transform.options;

import java.io.Serializable;

/**
 * Image resize options
 *
 */
public class ImageResizeOptions implements Serializable
{
    private static final long serialVersionUID = -6251308339825657974L;

    /** The width */
    private int width = -1;

    /** The height */
    private int height = -1;

    /** Indicates whether the aspect ratio of the image should be maintained */
    private boolean maintainAspectRatio = true;

    /** Indicates whether this is a percentage resize */
    private boolean percentResize = false;

    /** Indicates whether the resized image is a thumbnail */
    private boolean resizeToThumbnail = false;

    /**
     * Indicates that scaling operations should scale up or down to the specified dimensions, as requested.
     * If this argument is false, only resizings that scale the image down will be performed. Scaling up will result in
     * an unchanged image.
     * @since 4.0
     */
    private boolean allowEnlargement = true;

    /**
     * Default constructor
     */
    public ImageResizeOptions()
    {
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    @ToStringProperty
    public int getWidth()
    {
        return width;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    @ToStringProperty
    public int getHeight()
    {
        return height;
    }

    public void setMaintainAspectRatio(boolean maintainAspectRatio)
    {
        this.maintainAspectRatio = maintainAspectRatio;
    }

    @ToStringProperty
    public boolean isMaintainAspectRatio()
    {
        return maintainAspectRatio;
    }

    public void setPercentResize(boolean percentResize)
    {
        this.percentResize = percentResize;
    }

    @ToStringProperty
    public boolean isPercentResize()
    {
        return percentResize;
    }

    public void setResizeToThumbnail(boolean resizeToThumbnail)
    {
        this.resizeToThumbnail = resizeToThumbnail;
    }

    @ToStringProperty
    public boolean isResizeToThumbnail()
    {
        return resizeToThumbnail;
    }

    public void setAllowEnlargement(boolean allowEnlargement)
    {
        this.allowEnlargement = allowEnlargement;
    }

    @ToStringProperty
    public boolean getAllowEnlargement()
    {
        return allowEnlargement;
    }

    @Override
    public String toString()
    {
        return TransformationOptionsImpl.toString(this);
    }

}
