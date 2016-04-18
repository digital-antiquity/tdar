package org.tdar.web.resource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.utils.TestConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

import com.gargoylesoftware.htmlunit.html.DomElement;

public class ResourceAccessWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    private static final String DOCUMENT_URL = "/document/" + TestConstants.TEST_DOCUMENT_ID;
    private static final String REGISTRATION_PREFIX = "requestUserRegistration";

    @Test
    public void testShareAccessSuccess() {
        gotoPage("/resource/request/grant?resourceId=3088&requestorId=" + CONFIG.getUserId());
        setInput("permission", GeneralPermissions.MODIFY_METADATA.name());
        submitForm("submit");
        logger.info(getCurrentUrlPath());
        logger.info(getPageText());
        assertThat(getPageText(), containsString("has been granted"));
        logger.info("we are now on page: {}", getWebClient().getCurrentWindow().getEnclosedPage().getUrl());
    }

    @Test
    public void testShareAccessDenied() {
        gotoPage("/resource/request/grant?resourceId=3088&requestorId=" + CONFIG.getUserId());
        setInput("permission", GeneralPermissions.MODIFY_METADATA.name());
        setInput("reject", "true");
        setInput("comment","message!");
        submitForm("submit");
        logger.info(getCurrentUrlPath());
        logger.info(getPageText());
        assertThat(getPageText(), containsString("has been denied"));
        logger.info("we are now on page: {}", getWebClient().getCurrentWindow().getEnclosedPage().getUrl());
    }

    @Test
    public void testRequestAccess() throws IOException {
        logout();
        clickAccessLink();

        // login
        setInput("userLogin.loginUsername", getAdminUsername());
        setInput("userLogin.loginPassword", getAdminPassword());
        assertTrue(getPageText().contains("New Philadelphia Archaeology"));
        submitForm("submitLogin");
        
        // try and submit without comment
        assertTrue(getPageText().contains("New Philadelphia Archaeology"));
        assertFalse(getPageText().contains("SAA"));
        assertCurrentUrlContains("/resource/request");
        submitFormWithoutErrorCheck("send");
        assertFalse(getPageText().contains("Message Sent"));
        
        // add comment
        setInput("messageBody", "this is my comment");
        assertTrue(getPageText().contains("New Philadelphia Archaeology"));

        // submit and confirm message sent
        submitForm("send");
        assertTrue(getPageText().contains("New Philadelphia Archaeology"));
        assertTrue(getPageText().contains("Message Sent"));
    }

    @Test
    public void testShareAccessFailureEmptyPermission() throws IOException {
        logout();
        clickAccessLink();

        setInput("userLogin.loginUsername", getAdminUsername());
        setInput("userLogin.loginPassword", getAdminPassword());
        submitForm("submitLogin");
        assertCurrentUrlContains("/resource/request");
        gotoPage("/resource/request/grant?resourceId=3088&requestorId=" + CONFIG.getUserId());
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
        gotoPageWithoutErrorCheck("/resource/request/grant?resourceId=3088");
        assertStatusCodeNotSuccess();
    }

    @Test
    public void testShareAccessFailureEmptyResource() {
        gotoPageWithoutErrorCheck("/resource/request/grant");
        assertStatusCodeNotSuccess();
    }

    @Test
    public void testShareAccessFalure() {
        logout();
        login(CONFIG.getUsername(), CONFIG.getPassword());
        gotoPageWithoutErrorCheck("/resource/request/grant?resourceId=3088&requestorId=" + CONFIG.getUserId());
        logger.debug(getPageText());
    }

    @Test
    public void testRequestLoginFailed() throws IOException {
        logout();
        clickAccessLink();

        setInput("userLogin.loginUsername", getAdminUsername());
        // wrong on purpose
        setInput("userLogin.loginPassword", getAdminUsername());
        submitFormWithoutErrorCheck("submitLogin");
        assertTextPresent("Please check that your username and password were entered correctly");
        checkForFreemarkerExceptions();

        setInput("h.timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        // try successfully
        setInput("userLogin.loginUsername", getAdminUsername());
        setInput("userLogin.loginPassword", getAdminPassword());
        submitForm("submitLogin");
        assertCurrentUrlContains("/resource/request/");
    }

    @Test
    public void testRequestRegistrationFailed() throws IOException {
        logout();
        clickAccessLink();

        setInput(REGISTRATION_PREFIX + ".person.username", getAdminUsername());
        // wrong on purpose
        setInput(REGISTRATION_PREFIX + ".password", getAdminUsername());
        setInput("h.timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        submitFormWithoutErrorCheck("submitAction");
        checkForFreemarkerExceptions();
        assertTextPresent("find a user with this email");
        assertTextPresent("You must accept the Terms of Service");
        assertTextNotPresent("Could not authenticate at this time");
        logger.debug(getCurrentUrlPath());
        logger.debug(getPageText());
        Map<String, String> personmap = new HashMap<>();
        setupBasicUser(personmap, "contactwebtest", REGISTRATION_PREFIX);
        personmap.put(REGISTRATION_PREFIX + ".acceptTermsOfUseAndContributorAgreement", "true");
        // deleteUser(personmap.get(DOWNLOAD_REGISTRATION + ".person.username"));
        personmap.remove(REGISTRATION_PREFIX + ".contributorReason");
        personmap.remove(REGISTRATION_PREFIX + ".person.phone");
        for (String key : personmap.keySet()) {
            setInput(key, personmap.get(key));
        }
        submitForm("submitAction");

        // complete

        assertCurrentUrlContains("/resource/request/");
        // ("#manual-download");
    }

    private void clickAccessLink() throws IOException {
        gotoPage(DOCUMENT_URL);
        DomElement all = htmlPage.getElementById("requestAccess");
        logger.debug("requestAccess: {}", all);
        gotoPage(all.getAttribute("href"));
        assertCurrentUrlContains("/resource/request/");
    }

}
