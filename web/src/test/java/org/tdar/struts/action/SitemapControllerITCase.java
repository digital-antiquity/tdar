package org.tdar.struts.action;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.processes.daily.SitemapGeneratorProcess;
import org.tdar.struts.action.sitemap.SitemapController;


public class SitemapControllerITCase extends AbstractControllerITCase {

    @Autowired
    SitemapGeneratorProcess sitemapGeneratorProcess;

    @Test
    // note this will fail on second run because the following test creates the file
    public void testSitemapControllerWithoutSitemaps() {
        SitemapController controller = generateNewInitializedController(SitemapController.class);
        String execute = controller.execute();
        Assert.assertEquals(TdarActionSupport.NOT_FOUND, execute);
    }

    @Test
    public void testSitemapController() throws IOException {
        sitemapGeneratorProcess.execute();
        SitemapController controller = generateNewInitializedController(SitemapController.class);
        String execute = controller.execute();
        Assert.assertEquals(TdarActionSupport.SUCCESS, execute);
        String contents = IOUtils.toString(controller.getInputStream());
        logger.debug(contents);

    }
}
