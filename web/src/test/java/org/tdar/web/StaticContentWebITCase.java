package org.tdar.web;

import java.io.File;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.configuration.TdarConfiguration;

public class StaticContentWebITCase extends AbstractAnonymousWebTestCase {

    TdarConfiguration tdarConfig = TdarConfiguration.getInstance();
    
    @Test
    public void testStaticRequestedFile() {
        StaticContentServlet servlet = new StaticContentServlet();
        File file = servlet.getRequestedFile("search-header.jpg", null);
        Assert.assertThat(file.getParent(), Matchers.is(tdarConfig.getHostedFileStoreLocation()));
    }

    @Test
    public void testStaticRequestedPairtreeFile() {
        StaticContentServlet servlet = new StaticContentServlet();
        File file = servlet.getRequestedFile("search-header.jpg", new String[] { "123456" });
        Assert.assertThat(file.getParent(), Matchers.endsWith("/12/34/56/rec"));

    }


}
