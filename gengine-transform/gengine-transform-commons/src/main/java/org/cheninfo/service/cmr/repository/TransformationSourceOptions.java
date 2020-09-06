package org.cheninfo.service.cmr.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Defines options and demarcations needed to describe the details of how
 * the source should be transformed, independent of the target requirements.
 * <p>
 * See {@link PagedSourceOptions} for an example implementation that
 * describes the page number that should be used from the source content.
 *
 */
public interface TransformationSourceOptions
{

    /**
     * Gets the list of applicable mimetypes
     *
     * @return the applicable mimetypes
     */
    public List<String> getApplicableMimetypes();

    /**
     * Gets whether or not these transformation source options apply for the
     * given mimetype
     *
     * @param mimetype the mimetype of the source
     * @return if these transformation source options apply
     */
    public boolean isApplicableForMimetype(String mimetype);

    /**
     * Creates a new <code>TransformationSourceOptions</code> object from this
     * one, merging any non-null overriding fields in the given
     * <code>overridingOptions</code>
     *
     * @param overridingOptions
     * @return a merged <code>TransformationSourceOptions</code> object
     */
    public TransformationSourceOptions mergedOptions(TransformationSourceOptions overridingOptions);

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


