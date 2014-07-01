package org.tdar.core.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.service.XmlService;
import org.tdar.utils.jaxb.JsonProjectLookupFilter;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class JsonObfuscationTestCase {
    private Logger logger = Logger.getLogger(getClass());

    /*
     * these tests are around to help prevent cases of double escaping,
     * JS and HTML escaping need to be handled outside of this in the autocomplete or the tool
     * that's calling them.
     */

    XmlService xmlService;

    public JsonObfuscationTestCase() throws ClassNotFoundException {
        // TODO Auto-generated constructor stub
        xmlService = new XmlService();
    }

    @Test
    public void testHtmlEmbedded() throws IOException {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("<");
        String json = xmlService.convertToFilteredJson(p, JsonLookupFilter.class);
        logger.debug(json);
        assertFalse(json.contains("&lt;"));
        assertTrue(json.contains("test <"));
    }

    @Test
    public void testNullProject() throws IOException {
        String json = xmlService.convertToFilteredJson(Project.NULL, JsonProjectLookupFilter.class);
        logger.debug(json);
        Assert.assertNotNull(json);
    }

    @Test
    public void testJsEmbedded() throws IOException {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("O'Donnell");
        String json = xmlService.convertToFilteredJson(p, JsonLookupFilter.class);
        logger.debug(json);
        assertFalse(json.contains("\\'"));
        assertTrue(json.contains("\"O'"));
    }

    @Test
    public void testQuoteEmbedded() throws IOException {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("O\"Donnell");
        String json = xmlService.convertToFilteredJson(p, JsonLookupFilter.class);
        logger.debug(json);
        assertTrue(json.contains("\""));
        assertFalse(json.contains("&quot;"));
        assertTrue(json.contains("O\\\"D"));
    }
}
