package org.tdar.dataone.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;
import org.tdar.web.AbstractWebTestCase;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.vividsolutions.jts.util.Assert;

public class DataOneWebITCase extends AbstractWebTestCase {

    @Test
    public void ping() {
        Assert.equals(gotoPage("/dataone/monitor/ping"), 200);
    }

    @Test
    public void systemInfo() {
        Assert.equals(gotoPage("/dataone/node/"), 200);
        Assert.equals(gotoPage("/dataone/"), 200);
        logger.debug(getPageCode());
    }

    @Test
    public void testObject() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        String path = "/dataone/object/doi:10.6067:XCV8SN0B29";
        Page page = webClient.getPage(new WebRequest(new URL(pathToUrl(path)), HttpMethod.HEAD));

        Assert.equals(gotoPage(path), 200);
        logger.debug(getPageCode());
    }

    @Test
    public void testMeta() {
        Assert.equals(gotoPage("/dataone/meta/doi:10.6067:XCV8SN0B29"), 200);
        logger.debug(getPageCode());
    }
}
