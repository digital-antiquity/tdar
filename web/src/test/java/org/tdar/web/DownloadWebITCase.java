package org.tdar.web;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;

public class DownloadWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String DOWNLOAD_REGISTRATION = "downloadRegistration";
    private String url;

    @Before
    public void uploadFile() {
        createDocumentAndUploadFile("my first document", null);
        url = getCurrentUrlPath();
        logout();
    }

    @Test
    public void testDownloadLoginFailed() {
        gotoPage(url);
        DomNodeList<DomNode> all = htmlPage.getDocumentElement().querySelectorAll(".download-file");
        clickLinkWithText(all.get(0).getTextContent().trim());

        setInput("downloadUserLogin.loginUsername", getAdminUsername());
        // wrong on purpose
        setInput("downloadUserLogin.loginPassword", getAdminUsername());
        submitFormWithoutErrorCheck("submit");
        assertTextPresent("Please check that your username and password were entered correctly");
        checkForFreemarkerExceptions();

        setInput("h.timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        // try successfully
        setInput("downloadUserLogin.loginUsername", getAdminUsername());
        setInput("downloadUserLogin.loginPassword", getAdminPassword());
        submitForm("submit");
        assertCurrentUrlContains("filestore/confirm");
        DomNodeList<DomNode> manualLink = htmlPage.getDocumentElement().querySelectorAll("#manual-download");
        String url = manualLink.get(0).getAttributes().getNamedItem("href").getTextContent();
        logger.debug("DOWNLOAD URL: {}", url);
        gotoPage(url);
    }

    @Test
    public void testDownloadRegistrationFailed() {
        gotoPage(url);
        DomNodeList<DomNode> all = htmlPage.getDocumentElement().querySelectorAll(".download-file");
        clickLinkWithText(all.get(0).getTextContent().trim());

        setInput(DOWNLOAD_REGISTRATION + ".person.username", getAdminUsername());
        // wrong on purpose
        setInput(DOWNLOAD_REGISTRATION + ".password", getAdminUsername());
        setInput("h.timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        submitFormWithoutErrorCheck("submitAction");
        checkForFreemarkerExceptions();
        assertTextPresent("find a user with this email");
        assertTextPresent("You must accept the Terms of Service");
        assertTextNotPresent("Could not authenticate at this time");

        Map<String, String> personmap = new HashMap<>();
        setupBasicUser(personmap, "downloadwebtest", DOWNLOAD_REGISTRATION);
        personmap.put(DOWNLOAD_REGISTRATION + ".acceptTermsOfUse", "true");
//        deleteUser(personmap.get(DOWNLOAD_REGISTRATION + ".person.username"));
        personmap.remove("downloadRegistration.contributorReason");
        personmap.remove("downloadRegistration.person.phone");
        for (String key : personmap.keySet()) {
            setInput(key, personmap.get(key));
        }
        submitForm("submitAction");

        // complete

        assertCurrentUrlContains("filestore/confirm");
        // ("#manual-download");
    }

}
