package org.tdar.web.resource;

import org.junit.Test;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.utils.TestConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;

public class ResourceAccessWebITCase extends AbstractAdminAuthenticatedWebTestCase {
    
    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();

    @Test
    public void testShareAccessSuccess() {
        gotoPage("/resource/request-access?resourceId=3088&requestorId=" + CONFIG.getUserId());
        setInput("permission", GeneralPermissions.MODIFY_METADATA.name());
        submitForm("submit");
        logger.info(getCurrentUrlPath());
        logger.info(getPageText());
        assertThat(getPageText(), containsString(" has been granted "));
        logger.info("we are now on page: {}", getWebClient().getCurrentWindow().getEnclosedPage().getUrl());
    }

    @Test
    public void testShareAccessFailureEmptyPermission() {
        gotoPage("/resource/request-access?resourceId=3088&requestorId=" + CONFIG.getUserId());
        setInput("permission", "");
        submitFormWithoutErrorCheck("submit");
        assertTrue(getPageText().contains("Please specify the level of rights"));
    }

    private void assertStatusCodeNotSuccess() {
        logger.debug("{} {}", internalPage.getWebResponse().getStatusCode(), getCurrentUrlPath());
        assertNotEquals(200, internalPage.getWebResponse().getStatusCode());
    }

    @Test
    public void testShareAccessFailureEmptyUser() {
        gotoPageWithoutErrorCheck("/resource/request-access?resourceId=3088");
        assertStatusCodeNotSuccess();
    }

    @Test
    public void testShareAccessFailureEmptyResource() {
        gotoPageWithoutErrorCheck("/resource/request-access");
        assertStatusCodeNotSuccess();
    }

    @Test
    public void testShareAccessFalure() {
        logout();
        login(CONFIG.getUsername(), CONFIG.getPassword());
        gotoPageWithoutErrorCheck("/resource/request-access?resourceId=3088&requestorId=" + CONFIG.getUserId());
        logger.debug(getPageText());
    }
}
