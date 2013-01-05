package org.tdar.web;

import static org.tdar.TestConstants.ADMIN_PASSWORD;
import static org.tdar.TestConstants.ADMIN_USERNAME;
import static org.tdar.TestConstants.PASSWORD;
import static org.tdar.TestConstants.USERNAME;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.http.HttpStatus;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.FormEncodingType;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.KeyDataPair;
import com.gargoylesoftware.htmlunit.util.NameValuePair;

/**
 * @author Adam Brin
 * 
 */
public abstract class AbstractAuthenticatedWebTestCase extends AbstractWebTestCase {

    @Before
    public void setUp() {
        // Manual invocation of super.prepare() is unnecessary, see the multiple @Befores section in
        // http://ibiblio.org/java/slides/sdbestpractices2005/junit4/What's_New_in_JUnit_4.html
        // the relevant part is that for multiple @Before annotations, order is
        // unspecified within a class, but superclass @Befores are guaranteed to
        // always execute in front of subtype @Befores
        // super.prepare();
        login();
    }

    public void login() {
        login(USERNAME, PASSWORD);
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    public void loginAdmin() {
        login(getAdminUsername(), getAdminPassword());
    }

    public static String getAdminUsername() {
        return ADMIN_USERNAME;
    }

    public static String getAdminPassword() {
        return ADMIN_PASSWORD;
    }

    public void login(String user, String pass) {
        login(user, pass, false);
    }

    public int login(String user, String pass, boolean expectingErrors) {
        gotoPage("/");
        clickLinkOnPage("Log In");
        setMainForm("loginForm");
        user = System.getProperty("tdar.user", user);
        pass = System.getProperty("tdar.pass", pass);
        // logger.info(user + ":" + pass);
        setInput("loginUsername", user);
        setInput("loginPassword", pass);
        if (expectingErrors) {
            webClient.setThrowExceptionOnFailingStatusCode(false);
            submitFormWithoutErrorCheck("Login");
            webClient.setThrowExceptionOnFailingStatusCode(true);
        } else {
            clickElementWithId("btnLogin");
        }
        return internalPage.getWebResponse().getStatusCode();
    }

    @After
    public void logout() {
        webClient.setJavaScriptEnabled(false);
        gotoPage("/logout");
    }

    public String getPersonalFilestoreTicketId() {
        gotoPageWithoutErrorCheck("/upload/grab-ticket");
        TextPage textPage = (TextPage) internalPage;
        String json = textPage.getContent();
        logger.debug("ticket json::" + json);
        JSONObject jsonObject = JSONObject.fromObject(json);
        String ticketId = jsonObject.getString("id");
        logger.debug("ticket id::" + ticketId);
        return ticketId;
    }

    /**
     * upload the specified file to the personal filestore. Note this will change the current page of the webclient
     * 
     * @param ticketId
     * @param path
     */
    public void uploadFileToPersonalFilestore(String ticketId, String path) {
        uploadFileToPersonalFilestore(ticketId, path, true);
    }

    public void addFileProxyFields(int rowNum, FileAccessRestriction restriction, String filename) {
        createInput("hidden", "fileProxies[" + rowNum + "].restriction", restriction.name());
        createInput("hidden", "fileProxies[" + rowNum + "].action", FileAction.ADD.name());
        createInput("hidden", "fileProxies[" + rowNum + "].fileId", "-1");
        createInput("hidden", "fileProxies[" + rowNum + "].filename", filename);
        createInput("hidden", "fileProxies[" + rowNum + "].sequenceNumber", Integer.toString(rowNum));
    }

    public int uploadFileToPersonalFilestoreWithoutErrorCheck(String ticketId, String path) {
        return uploadFileToPersonalFilestore(ticketId, path, false);
    }

    private int uploadFileToPersonalFilestore(String ticketId, String path, boolean assertNoErrors) {
        int code = 0;
        WebClient client = getWebClient();
        String url = getBaseUrl() + "/upload/upload";
        try {
            WebRequest webRequest = new WebRequest(new URL(url), HttpMethod.POST);
            List<NameValuePair> parms = new ArrayList<NameValuePair>();
            parms.add(nameValuePair("ticketId", ticketId));
            File file = null;
            if (path != null) {
                file = new File(path);
                parms.add(nameValuePair("uploadFile", file));
            }
            webRequest.setRequestParameters(parms);
            webRequest.setEncodingType(FormEncodingType.MULTIPART);
            Page page = client.getPage(webRequest);
            code = page.getWebResponse().getStatusCode();
            Assert.assertTrue(assertNoErrors && code == HttpStatus.OK.value());
            if (file != null) {
                assertFileSizes(page, Arrays.asList(new File[] { file }));
            }
        } catch (MalformedURLException e) {
            Assert.fail("mailformed URL: are you sure you specified the right page in your test?");
        } catch (IOException iox) {
            Assert.fail("IO exception occured during test");
        } catch (FailingHttpStatusCodeException httpEx) {
            if (assertNoErrors) {
                Assert.fail("upload request returned code" + httpEx.getStatusCode());
            }
            code = httpEx.getStatusCode();
        }
        return code;
    }

    protected void assertFileSizes(Page page, List<File> files) {
        JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON(page.getWebResponse().getContentAsString());
        for (int i = 0; i < files.size(); i++) {
            Assert.assertEquals("file size reported from server should be same as original", files.get(i).length(), jsonArray.getJSONObject(i).getLong("size"));
        }
    }

    public NameValuePair nameValuePair(String name, String value) {
        return new NameValuePair(name, value);
    }

    private NameValuePair nameValuePair(String name, File file) {
        // FIXME:is it safe to specify text/plain even when we know it isn't?? It happens to 'work' for these tests, not sure of potential side effects...
        return nameValuePair(name, file, "text/plain");
    }

    private NameValuePair nameValuePair(String name, File file, String contentType) {
        KeyDataPair keyDataPair = new KeyDataPair(name, file, contentType, "utf8");
        return keyDataPair;
    }

}
