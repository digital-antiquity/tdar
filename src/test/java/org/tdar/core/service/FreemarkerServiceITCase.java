package org.tdar.core.service;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.struts.action.search.SearchParameters;

public class FreemarkerServiceITCase extends AbstractIntegrationTestCase{
    
    @Autowired
    private FreemarkerService freemarkerService;
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Test
    public void testNothing() {
        Assert.assertNotNull(freemarkerService);
        logger.debug("yay it wired up!");
    }
   
    public void testRender() {
        String path = "/search/results-description.ftl";
        Object root = new SearchParameters();
        String renderedTemplate = freemarkerService.render(path, root);
        Assert.assertNotNull(renderedTemplate);
        Assert.assertTrue("not empty string", StringUtils.isNotEmpty(renderedTemplate));
    }
    
    @Test
    public void testFreemarkerRendering() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "Hieronymous");
        map.put("bar", "Basho");
        String output = freemarkerService.render("test-email.ftl", map);
        logger.debug("output: {}", output);
        assertTrue(output.contains("Hieronymous"));
        assertTrue(output.contains("Basho"));
    }
   
    

}
