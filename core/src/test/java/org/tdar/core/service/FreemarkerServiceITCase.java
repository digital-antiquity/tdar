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

    // @Autowired
    // private RebuildHomepageCache homepageCache;

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

    // @Test
    // public void testFreemarkerRenderingToFile() throws IOException {
    // homepageCache.execute();
    // Map<String, Object> map = new HashMap<String, Object>();
    // String heir = "Hieronymous";
    // map.put("foo", heir);
    // String boash = "Boash";
    // map.put("bar", boash);
    // map.put("themeDir", "includes/themes/tdar");
    // map.put("siteAcronym", "tDAR");
    // map.put("geographicKeywordCache", genericService.findAll(HomepageGeographicKeywordCache.class));
    // File output = freemarkerService.renderWithCache("test-map.html", "WEB-INF/content/map.ftl", map);
    // String outTxt = FileUtils.readFileToString(output);
    // logger.debug("output: {}", output);
    // assertTrue(outTxt.contains(heir));
    // assertTrue(outTxt.contains(boash));
    // }

}
