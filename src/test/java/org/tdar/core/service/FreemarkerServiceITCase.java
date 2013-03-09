package org.tdar.core.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;

public class FreemarkerServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private FreemarkerService freemarkerService;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testFreemarkerRendering() throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        String heir = "Hieronymous";
        map.put("foo", heir);
        String boash = "Boash";
        map.put("bar", boash);
        String output = freemarkerService.render("test-email.ftl", map);
        logger.debug("output: {}", output);
        assertTrue(output.contains(heir));
        assertTrue(output.contains(boash));
    }

}
