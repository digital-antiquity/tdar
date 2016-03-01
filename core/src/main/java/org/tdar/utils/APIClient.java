package org.tdar.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.SerializationService;
import org.tdar.utils.jaxb.JaxbResultContainer;

public class APIClient {

    private static final String ID = "id";
    private static final String ACCOUNT_ID = "accountId";
    private static final String RECORD = "record";
    private static final String API_INGEST_UPDATE_FILES = "/api/ingest/updateFiles";
    private static final String API_INGEST_UPLOAD = "/api/ingest/upload";
    private static final String API_LOGOUT = "/api/logout";
    private static final String USER_LOGIN_LOGIN_PASSWORD = "userLogin.loginPassword";
    private static final String USER_LOGIN_LOGIN_USERNAME = "userLogin.loginUsername";
    private static final String API_LOGIN = "/api/login";
    private static final String UPLOAD_FILE = "uploadFile";
    private String baseUrl;
    private CloseableHttpClient httpClient = SimpleHttpUtils.createClient();
    private SerializationService serializationService;

    public APIClient(String baseSecureUrl, SerializationService serializationService) {
        this.baseUrl = baseSecureUrl;
        this.serializationService = serializationService;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    public Pair<Integer, JaxbResultContainer> apiLogin(String username, String password) throws IllegalStateException, Exception {
        HttpPost post = new HttpPost(baseUrl + API_LOGIN);
        List<NameValuePair> postNameValuePairs = new ArrayList<>();
        postNameValuePairs.add(new BasicNameValuePair(USER_LOGIN_LOGIN_USERNAME, username));
        postNameValuePairs.add(new BasicNameValuePair(USER_LOGIN_LOGIN_PASSWORD, password));
        post.setEntity(new UrlEncodedFormEntity(postNameValuePairs, HTTP.UTF_8));
        CloseableHttpResponse response = getHttpClient().execute(post);
        logger.debug("status {}", response.getStatusLine());
        HttpEntity entity = response.getEntity();
        String result = IOUtils.toString(entity.getContent());
        logger.debug(result);
        return Pair.create(response.getStatusLine().getStatusCode(), (JaxbResultContainer) serializationService.parseXml(new StringReader(result)));
    }

    public void apiLogout() throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(baseUrl + API_LOGOUT);
        CloseableHttpResponse response = getHttpClient().execute(post);
        logger.debug("status {}", response.getStatusLine());
        Assert.assertTrue(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_MOVED_TEMPORARILY).contains(response.getStatusLine().getStatusCode()));

    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpResponse uploadRecord(String docXml, Long tdarId, Long accountId, File... files) throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(baseUrl + API_INGEST_UPLOAD);

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody(RECORD, docXml);
        processIds(tdarId, accountId, builder);

        addFiles(builder, files);

        post.setEntity(builder.build());
        CloseableHttpResponse response = getHttpClient().execute(post);
        return response;

    }

    private void addFiles(MultipartEntityBuilder builder, File... files) {
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            builder.addPart(UPLOAD_FILE, new FileBody(file));
        }
    }

    private void processIds(Long tdarId, Long accountId, MultipartEntityBuilder builder) {
        if (accountId != null) {
            builder.addTextBody(ACCOUNT_ID, accountId.toString());
        }
        if (tdarId != null) {
            builder.addTextBody(ID, tdarId.toString());
        }
    }

    public CloseableHttpResponse updateFiles(String text, Long tdarId, Long accountId, File... files) throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(baseUrl + API_INGEST_UPDATE_FILES);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody(RECORD, text);

        processIds(tdarId, accountId, builder);
        addFiles(builder, files);
        post.setEntity(builder.build());
        CloseableHttpResponse response = getHttpClient().execute(post);
        return response;
    }

}
