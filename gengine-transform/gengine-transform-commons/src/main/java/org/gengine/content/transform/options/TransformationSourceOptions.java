package org.gengine.content.transform.options;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.gengine.util.Mergable;

/**
 * Defines options and demarcations needed to describe the details of how
 * the source should be transformed, independent of the target requirements.
 * <p>
 * See {@link PagedSourceOptions} for an example implementation that
 * describes the page number that should be used from the source content.
 *
 */
public interface TransformationSourceOptions extends Serializable, Mergable<TransformationSourceOptions>
{

    /**
     * Gets the list of applicable media types (mimetypes)
     *
     * @return the applicable media types
     */
    public List<String> getApplicableMediaTypes();

    /**
     * Gets whether or not these transformation source options apply for the
     * given media type (mimetype)
     *
     * @param mediaType the media type of the source
     * @return if these transformation source options apply
     */
    public boolean isApplicableForMediaType(String mediaType);

    /**
     * Gets the serializer for the source options.
     *
     * @return the serializer
     */
    public TransformationSourceOptionsSerializer getSerializer();

    /**
     * Defines methods for serializing the source options into a parameter map and
     * deserializing from a serialized options accessor.
     * <p>
     * This is primarily used when interacting with the {@link RenditionService}
     * with {@link AbstractRenderingEngine}'s RenderContext being an implementer
     * of this interface.
     */
    public interface TransformationSourceOptionsSerializer
    {

        /**
         * Serializes the given transformation source options into the given parameter map.
         *
         * @param transformationSourceOptions
         * @param parameters
         */
        public void serialize(TransformationSourceOptions transformationSourceOptions, Map<String, Serializable> parameters);

        /**
         * Gets the parameters from the serialized options accessor and builds a source options object.
         *
         * @param serializedOptions
         * @return the deserialized source options
         */
        public TransformationSourceOptions deserialize(SerializedTransformationOptionsAccessor serializedOptions);

    }

}


