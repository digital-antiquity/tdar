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
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.XmlService;
import org.tdar.struts.action.api.ApiAuthenticationController;
import org.tdar.utils.Pair;
import org.tdar.utils.SimpleHttpUtils;
import org.tdar.utils.TestConfiguration;
import org.tdar.utils.jaxb.JaxbMapResultContainer;

import com.sun.media.rtsp.protocol.StatusCode;

public class APIControllerWebITCase extends AbstractWebTestCase {

    @Autowired
    XmlService xmlService;

    private static final TestConfiguration CONFIG = TestConfiguration.getInstance();
    private static Logger logger = LoggerFactory.getLogger(SimpleHttpUtils.class);
    CloseableHttpClient httpClient = SimpleHttpUtils.createClient();

    @Test
    public void testValidLogin() throws IllegalStateException, Exception {
        try {
        Pair<Integer, JaxbMapResultContainer> apiLogin = apiLogin(CONFIG.getAdminUsername(), CONFIG.getAdminPassword());
        assertEquals(StatusCode.OK, apiLogin.getFirst().intValue());
        JaxbMapResultContainer result = apiLogin.getSecond();
        assertNotNull(result.getResult().get(ApiAuthenticationController.API_TOKEN));
        assertEquals(TdarConfiguration.getInstance().getRequestTokenName(), result.getResult().get(ApiAuthenticationController.API_TOKEN_KEY_NAME));
        assertNotNull(result.getResult().get(ApiAuthenticationController.USERNAME));
        } catch (Exception e) {
            logger.debug("exception",e );
        }
    }

    @Test
    public void testInValidLogin() throws IllegalStateException, Exception {
        Pair<Integer, JaxbMapResultContainer> apiLogin = apiLogin(CONFIG.getUsername(), CONFIG.getPassword());
        assertEquals(StatusCode.OK, apiLogin.getFirst().intValue());
        JaxbMapResultContainer result = apiLogin.getSecond();
        assertNull(result.getResult().get(ApiAuthenticationController.API_TOKEN));
        assertNull(result.getResult().get(ApiAuthenticationController.USERNAME));
    }

    public Pair<Integer, JaxbMapResultContainer> apiLogin(String username, String password) throws IllegalStateException, Exception {
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
        return Pair.create(response.getStatusLine().getStatusCode(), (JaxbMapResultContainer) xmlService.parseXml(new StringReader(result)));
    }

    @After
    public void apiLogout() throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(CONFIG.getBaseSecureUrl() + "/api/logout");
        CloseableHttpResponse response = httpClient.execute(post);
        logger.debug("status {}", response.getStatusLine());
        Assert.assertTrue(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_MOVED_TEMPORARILY).contains(response.getStatusLine().getStatusCode()));

    }
}
