package org.tdar.struts.action.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AbstractControllerITCase;
import org.tdar.struts.action.api.geo.GeoLookupAction;

public class GeoAPIITCase extends AbstractControllerITCase {

    @Test
    public void testGeoAPI() throws Exception {
        GeoLookupAction gla = generateNewInitializedController(GeoLookupAction.class);
        gla.setCountries(Arrays.asList("England","Scotland","Wales","Northern Ireland"));
        gla.prepare();

        String body = serializationService.convertToJson(gla.getResultObject());
        logger.debug(body);
        assertTrue(body.contains("no countries"));
    }

    @Autowired
    private SerializationService serializationService;
    @Test
    public void testGeoAPIEmpty() throws Exception {
        GeoLookupAction gla = generateNewInitializedController(GeoLookupAction.class);
        gla.prepare();
        String body = serializationService.convertToJson(gla.getResultObject());
        logger.debug(body);
        assertTrue(body.contains("no countries"));
    }

    @Test
    public void testGeoAPIValid() throws Exception {
        GeoLookupAction gla = generateNewInitializedController(GeoLookupAction.class);
        gla.setCountries(Arrays.asList("Georgia"));
        gla.prepare();

        String body = serializationService.convertToJson(gla.getResultObject());
        logger.debug(body);
        assertFalse(body.contains("no countries"));
    }
}
