package org.gengine.content.transform;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.gengine.content.mediatype.FileMediaType;
import org.gengine.content.transform.options.CropSourceOptions;
import org.gengine.content.transform.options.ImageResizeOptions;
import org.gengine.content.transform.options.ImageTransformationOptions;
import org.gengine.content.transform.options.PagedSourceOptions;
import org.gengine.content.transform.options.TemporalSourceOptions;
import org.gengine.messaging.jackson.ObjectMapperFactory;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TransformationRequestMarshallingTest
{

    private TransformationRequest transformationRequest;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        ImageTransformationOptions options = new ImageTransformationOptions();
        options.setAutoOrient(true);
        options.setCommandOptions("command options");
        options.setIncludeEmbedded(true);
        options.setPageLimit(50);
        options.setTimeoutMs(1000);

        ImageResizeOptions imageResizeOptions = new ImageResizeOptions();
        imageResizeOptions.setAllowEnlargement(true);
        imageResizeOptions.setHeight(55);
        imageResizeOptions.setMaintainAspectRatio(true);
        imageResizeOptions.setPercentResize(true);
        imageResizeOptions.setResizeToThumbnail(true);
        imageResizeOptions.setWidth(56);
        options.setResizeOptions(imageResizeOptions);

        CropSourceOptions cropSourceOptions = new CropSourceOptions();
        cropSourceOptions.setApplicableMediaTypes(Arrays.asList("image media types"));
        cropSourceOptions.setGravity("gravity");
        cropSourceOptions.setHeight(40);
        cropSourceOptions.setPercentageCrop(true);
        cropSourceOptions.setWidth(41);
        cropSourceOptions.setXOffset(5);
        cropSourceOptions.setYOffset(6);
        options.addSourceOptions(cropSourceOptions);

        PagedSourceOptions pagedSourceOptions = new PagedSourceOptions();
        pagedSourceOptions.setApplicableMediaTypes(Arrays.asList("document media types"));
        pagedSourceOptions.setStartPageNumber(7);
        pagedSourceOptions.setEndPageNumber(8);
        options.addSourceOptions(pagedSourceOptions);

        TemporalSourceOptions temporalSourceOptions = new TemporalSourceOptions();
        temporalSourceOptions.setApplicableMediaTypes(Arrays.asList("a/v media types"));
        temporalSourceOptions.setDuration("00:00:00.5");
        temporalSourceOptions.setOffset("00:00:00.2");
        options.addSourceOptions(temporalSourceOptions);

        transformationRequest = new TransformationRequest();
        transformationRequest.setOptions(options);
        transformationRequest.setTargetMediaType(FileMediaType.VIDEO_MP4.getMediaType());

        mapper = ObjectMapperFactory.createInstance();
    }

    @Test
    public void testMarshalling() throws IOException
    {
        String json = mapper.writeValueAsString(transformationRequest);

        System.out.println("json=" + json);

        TransformationRequest unmarshalledRequest = mapper.readValue(json, TransformationRequest.class);

        assertEquals(
                "00:00:00.5",
                unmarshalledRequest.getOptions().getSourceOptions(TemporalSourceOptions.class).getDuration());
    }

}
