package org.gengine.content.transform.options;

import static junit.framework.Assert.*;

import java.util.Arrays;

import org.junit.Test;


public class TransformationOptionsTest
{

    @Test
    public void testImageOptions()
    {
        CropSourceOptions cropSourceOptions = new CropSourceOptions();
        cropSourceOptions.setApplicableMediaTypes(Arrays.asList("type1", "type2"));
        cropSourceOptions.setWidth(640);
        cropSourceOptions.setHeight(480);
        cropSourceOptions.setXOffset(10);
        cropSourceOptions.setYOffset(20);
        cropSourceOptions.setGravity("up");

        ImageTransformationOptions options = new ImageTransformationOptions();
        options.addSourceOptions(cropSourceOptions);

        ImageTransformationOptions copy = new ImageTransformationOptions(options);

        cropSourceOptions.setWidth(11);
        cropSourceOptions.setHeight(22);

        CropSourceOptions copyCropSourceOptions = copy.getSourceOptions(CropSourceOptions.class);
        assertEquals("[type1, type2]", copyCropSourceOptions.getApplicableMediaTypes().toString());
        assertEquals(640, copyCropSourceOptions.getWidth());
        assertEquals(480, copyCropSourceOptions.getHeight());
        assertEquals(10, copyCropSourceOptions.getXOffset());
        assertEquals(20, copyCropSourceOptions.getYOffset());
        assertEquals("up", copyCropSourceOptions.getGravity());
    }

    @Test
    public void testVideoOptions()
    {
        TemporalSourceOptions temporalSourceOptions = new TemporalSourceOptions();
        temporalSourceOptions.setApplicableMediaTypes(Arrays.asList("type1", "type2"));
        temporalSourceOptions.setElementIntervalSeconds(5);
        temporalSourceOptions.setMaxElements(30);

        VideoTransformationOptions options = new VideoTransformationOptions();
        options.addSourceOptions(temporalSourceOptions);

        VideoTransformationOptions copy = new VideoTransformationOptions(options);

        temporalSourceOptions.setElementIntervalSeconds(55);
        temporalSourceOptions.setMaxElements(44);

        TemporalSourceOptions copyTemporalSourceOptions = copy.getSourceOptions(TemporalSourceOptions.class);
        assertEquals("[type1, type2]", copyTemporalSourceOptions.getApplicableMediaTypes().toString());
        assertEquals(new Integer(5), copyTemporalSourceOptions.getElementIntervalSeconds());
        assertEquals(new Integer(30), copyTemporalSourceOptions.getMaxElements());

        copyTemporalSourceOptions.setElementIntervalSeconds(null);
        copyTemporalSourceOptions.setMaxElements(null);
        copyTemporalSourceOptions.setDuration("00:00:55");
        copyTemporalSourceOptions.setOffset("00:00:02");

        assertEquals(new Integer(55), options.getSourceOptions(TemporalSourceOptions.class).getElementIntervalSeconds());
        assertEquals(new Integer(44), options.getSourceOptions(TemporalSourceOptions.class).getMaxElements());
        assertNull(options.getSourceOptions(TemporalSourceOptions.class).getDuration());
        assertNull(options.getSourceOptions(TemporalSourceOptions.class).getOffset());
    }

}
