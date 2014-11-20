package org.tdar.web;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.utils.TestConfiguration;

public class ResourceAccessWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();

    @Test
    public void testShareAccessSuccess() {
        gotoPage("/resource/request-access?resourceId=3088&requestorId=" + CONFIG.getUserId());
        setInput("permission", GeneralPermissions.MODIFY_METADATA.name());
        submitForm("submit");
        assertTrue(getPageText().contains(" has been granted "));
    }

    @Test
    public void testShareAccessFailureEmptyPermission() {
        gotoPage("/resource/request-access?resourceId=3088&requestorId=" + CONFIG.getUserId());
        setInput("permission", "");
        submitFormWithoutErrorCheck("submit");
        assertTrue(getPageText().contains("Please specify the level of rights"));
    }

    private void assertStatusCodeNotSuccess() {
        logger.debug("{} {}",internalPage.getWebResponse().getStatusCode(),  getCurrentUrlPath());
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
