package org.tdar.core.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.tdar.core.bean.entity.Person;

public class JsonObfuscationTestCase {
    protected Logger logger = Logger.getLogger(getClass());

    /*
     * these tests are around to help prevent cases of double escaping, 
     * JS and HTML escaping need to be handled outside of this in the autocomplete or the tool
     *  that's calling them.
     */
    
    @Test
    public void testHtmlEmbedded() {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("<");
        logger.debug(p.toJSON());
        assertFalse(p.toJSON().toString().contains("&lt;"));
        assertTrue(p.toJSON().toString().contains("test <"));
    }

    @Test
    public void testJsEmbedded() {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("O'Donnell");
        logger.debug(p.toJSON());
        assertFalse(p.toJSON().toString().contains("\\'"));
        assertTrue(p.toJSON().toString().contains("\"O'"));
    }

    @Test
    public void testQuoteEmbedded() {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("O\"Donnell");
        logger.debug(p.toJSON());
        assertTrue(p.toJSON().toString().contains("\""));
        assertFalse(p.toJSON().toString().contains("&quot;"));
        assertTrue(p.toJSON().toString().contains("O\\\"D"));
    }
}
