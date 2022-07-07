package org.tdar.configuration;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.tdar.struts.action.WebConfig;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class TdarConfigurationTest {
    transient Logger logger = LoggerFactory.getLogger(getClass());


    @Test
    /** A stupid test to mostly confirm that map config is wired up correctly **/
    public void testMapConfigurationValues() {
        WebConfig config = new WebConfig();
        assertNotNull(config);
        assertNotNull(config.getLeafletApiKey());
        assertNotNull(config.getLeafletStaticApiKey());
        assertEquals("osm", config.getLeafletProvider());
    }



    @Test
    public void testAccessionPricingEnabled() {
        TdarConfiguration tdc = TdarConfiguration.getInstance();
        assertFalse("accession fee feature should be disabled", tdc.isAccessionFeesEnabled());
    }
    



}
