package org.tdar.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.XmlService;
import org.tdar.struts.action.APIControllerITCase;
import org.tdar.struts.action.api.APIController;
import org.tdar.utils.Pair;
import org.tdar.utils.SimpleHttpUtils;
import org.tdar.utils.TestConfiguration;
import org.tdar.utils.jaxb.JaxbResultContainer;

import com.opensymphony.xwork2.Action;
import com.sun.media.rtsp.protocol.StatusCode;

public class APIControllerWebITCase extends AbstractWebTestCase {

    @Autowired
    XmlService xmlService;

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    private static Logger logger = LoggerFactory.getLogger(SimpleHttpUtils.class);
    CloseableHttpClient httpClient = SimpleHttpUtils.createClient();

    @Test
    public void testValidLogin() throws IllegalStateException, Exception {
        setupValidLogin();
        apiLogout();
    }

    private JaxbResultContainer setupValidLogin() {
        try {
            Pair<Integer, JaxbResultContainer> apiLogin = apiLogin(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
            assertEquals(StatusCode.OK, apiLogin.getFirst().intValue());
            JaxbResultContainer result = apiLogin.getSecond();
            assertNotNull(result.getApiToken());
            assertEquals(TdarConfiguration.getInstance().getRequestTokenName(), result.getSessionKeyName());
            assertNotNull(result.getUsername());
            return result;
        } catch (Exception e) {
            logger.debug("exception", e);
        }
        return null;
    }

    @Test
    public void testValidRequestWithoutCookie() throws IllegalStateException, Exception {
        JaxbResultContainer login = setupValidLogin();
        CloseableHttpClient client2 = SimpleHttpUtils.createClient();
        HttpGet get = new HttpGet(CONFIG.getBaseSecureUrl()  + "/api/view?id=4231");
        CloseableHttpResponse execute = client2.execute(get);
        int statusCode = execute.getStatusLine().getStatusCode();
        logger.debug("status:{}", statusCode);
        assertEquals(HttpStatus.SC_FORBIDDEN, statusCode);
        logger.debug(IOUtils.toString(execute.getEntity().getContent()));
        get = new HttpGet(String.format("%s/api/view?id=4231&%s=%s",CONFIG.getBaseSecureUrl() , login.getSessionKeyName(), login.getApiToken()));
        execute = client2.execute(get);
        logger.debug("status:{}", statusCode);
        statusCode = execute.getStatusLine().getStatusCode();
        assertEquals(HttpStatus.SC_OK, statusCode);
        logger.debug(IOUtils.toString(execute.getEntity().getContent()));
        apiLogout();
    }

    @Test
    @Rollback
    public void testCreate() throws Exception {
        JaxbResultContainer login = setupValidLogin();
        
        Document doc = genericService.findAll(Document.class, 1).get(0);
        genericService.markReadOnly(doc);
        doc.setId(null);
        doc.getInformationResourceFiles().clear();
        doc.setMappedDataKeyColumn(null);
        APIControllerITCase.removeInvalidFields(doc);
        String docXml = xmlService.convertToXML(doc);
        logger.info(docXml);
        HttpPost post = new HttpPost(CONFIG.getBaseSecureUrl()  + "/api/upload");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("record", docXml);
        post.setEntity(builder.build());
        CloseableHttpResponse response = httpClient.execute(post);        
        logger.debug("status:{} ", response.getStatusLine());
        logger.debug("response: {}", IOUtils.toString(response.getEntity().getContent()));
        assertEquals(StatusCode.CREATED, response.getStatusLine().getStatusCode());

        
    }
    
    @Test
    public void testInvalidLogin() throws IllegalStateException, Exception {
        Pair<Integer, JaxbResultContainer> apiLogin = apiLogin(CONFIG.getUsername(), CONFIG.getPassword());
        assertEquals(StatusCode.BAD_REQUEST, apiLogin.getFirst().intValue());
        JaxbResultContainer result = apiLogin.getSecond();
        assertNull(result.getApiToken());
        assertNull(result.getUsername());
        assertEquals(getText("apiAuthenticationController.invalid_user"), result.getErrors().get(0));
    }

    public Pair<Integer, JaxbResultContainer> apiLogin(String username, String password) throws IllegalStateException, Exception {
        HttpPost post = new HttpPost(CONFIG.getBaseSecureUrl() + "/api/login");
        List<NameValuePair> postNameValuePairs = new ArrayList<>();
        postNameValuePairs.add(new BasicNameValuePair("userLogin.loginUsername", username));
        postNameValuePairs.add(new BasicNameValuePair("userLogin.loginPassword", password));
        post.setEntity(new UrlEncodedFormEntity(postNameValuePairs, HTTP.UTF_8));
        CloseableHttpResponse response = httpClient.execute(post);
        logger.debug("status {}", response.getStatusLine());
        HttpEntity entity = response.getEntity();
        String result = IOUtils.toString(entity.getContent());
        logger.debug(result);
        return Pair.create(response.getStatusLine().getStatusCode(), (JaxbResultContainer) xmlService.parseXml(new StringReader(result)));
    }

    public void apiLogout() throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(CONFIG.getBaseSecureUrl() + "/api/logout");
        CloseableHttpResponse response = httpClient.execute(post);
        logger.debug("status {}", response.getStatusLine());
        Assert.assertTrue(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_MOVED_TEMPORARILY).contains(response.getStatusLine().getStatusCode()));

    }
}
