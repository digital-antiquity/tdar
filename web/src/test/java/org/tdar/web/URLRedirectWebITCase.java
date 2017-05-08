package org.tdar.web;

import org.junit.Test;
import org.tdar.utils.TestConfiguration;

public class URLRedirectWebITCase extends AbstractWebTestCase {

    public String getSecureUrl(String path) {
        return TestConfiguration.getInstance().getBaseSecureUrl() + path;
    }

    @Test
    public void testBrowseSecureToInsecureWithPage() {
        String part = "browse/creators/4?startRecord=4&recordsPerPage=2";
        gotoPage(getSecureUrl(part));
        assertCurrentUrlEquals(getBaseUrl() + part);
    }

    @Test
    public void testBrowseSecureToInsecure() {
        String part = "browse/creators/4";
        gotoPage(getSecureUrl(part));
        assertCurrentUrlEquals(getBaseUrl() + part);
    }

    @Test
    public void testProjectSecureToInsecureWithPage() {
        String part = "project/3805/new-philadelphia-archaeology-project?startRecord=2&recordsPerPage=2";
        gotoPage(getSecureUrl(part));
        assertCurrentUrlEquals(getBaseUrl() + part);
    }

    @Test
    public void testProjectSecureToInsecure() {
        String part = "project/3805/new-philadelphia-archaeology-project";
        gotoPage(getSecureUrl(part));
        assertCurrentUrlEquals(getBaseUrl() + part);
    }
}
