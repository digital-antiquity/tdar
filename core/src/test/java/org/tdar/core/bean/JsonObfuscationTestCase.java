package org.tdar.core.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.SerializationServiceImpl;
import org.tdar.utils.json.JsonLookupFilter;
import org.tdar.utils.json.JsonProjectLookupFilter;

public class JsonObfuscationTestCase {
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * these tests are around to help prevent cases of double escaping,
     * JS and HTML escaping need to be handled outside of this in the autocomplete or the tool
     * that's calling them.
     */

    SerializationService serializationService;

    public JsonObfuscationTestCase() throws ClassNotFoundException {
        // TODO Auto-generated constructor stub
        serializationService = new SerializationServiceImpl();
    }

    @Test
    public void testHtmlEmbedded() throws IOException {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("<");
        String json = serializationService.convertToFilteredJson(p, JsonLookupFilter.class);
        logger.debug(json);
        assertFalse(json.contains("&lt;"));
        assertTrue(json.contains("test <"));
    }

    @Test
    public void testNullProject() throws IOException {
        String json = serializationService.convertToFilteredJson(Project.NULL, JsonProjectLookupFilter.class);
        logger.debug(json);
        Assert.assertNotNull(json);
    }

    @Test
    public void testJsEmbedded() throws IOException {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("O'Donnell");
        String json = serializationService.convertToFilteredJson(p, JsonLookupFilter.class);
        logger.debug(json);
        assertFalse(json.contains("\\'"));
        assertTrue(json.contains("\"O'"));
    }

    @Test
    public void testQuoteEmbedded() throws IOException {
        Person p = new Person();
        p.setFirstName("test");
        p.setLastName("O\"Donnell");
        String json = serializationService.convertToFilteredJson(p, JsonLookupFilter.class);
        logger.debug(json);
        assertTrue(json.contains("\""));
        assertFalse(json.contains("&quot;"));
        assertTrue(json.contains("O\\\"D"));
    }
}
