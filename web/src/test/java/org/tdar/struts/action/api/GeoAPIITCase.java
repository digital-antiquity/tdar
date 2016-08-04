package org.tdar.struts.action.api;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.api.geo.GeoLookupAction;

public class GeoAPIITCase extends AbstractControllerITCase {

    @Test
    public void testGeoAPI() throws Exception {
        GeoLookupAction gla = generateNewInitializedController(GeoLookupAction.class);
        gla.setCountries(Arrays.asList("England","Scotland","Wales","Northern Ireland"));
        gla.prepare();

        String body = IOUtils.toString(gla.getJsonInputStream());
        logger.debug(body);
        assertTrue(body.contains("no countries"));
    }

    @Test
    public void testGeoAPIEmpty() throws Exception {
        GeoLookupAction gla = generateNewInitializedController(GeoLookupAction.class);
        gla.prepare();

        String body = IOUtils.toString(gla.getJsonInputStream());
        logger.debug(body);
        assertTrue(body.contains("no countries"));
    }

    @Test
    @Ignore
    public void testGeoAPIValid() throws Exception {
        GeoLookupAction gla = generateNewInitializedController(GeoLookupAction.class);
        gla.setCountries(Arrays.asList("Georgia"));
        gla.prepare();

        String body = IOUtils.toString(gla.getJsonInputStream());
        logger.debug(body);
        assertFalse(body.contains("no countries"));
    }
}
