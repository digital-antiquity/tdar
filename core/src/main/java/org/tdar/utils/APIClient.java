package org.tdar.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class APIClient {

    private static final String UTF_8 = "UTF-8";
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
    private CloseableHttpClient httpClient = SimpleHttpUtils.createClient(3500);

    public APIClient(String baseSecureUrl) {
        this.baseUrl = baseSecureUrl;
    }

    private Logger logger = LoggerFactory.getLogger(getClass());

    public ApiClientResponse apiLogin(String username, String password) throws IllegalStateException, Exception {
        HttpPost post = new HttpPost(baseUrl + API_LOGIN);
        List<NameValuePair> postNameValuePairs = new ArrayList<>();
        postNameValuePairs.add(new BasicNameValuePair(USER_LOGIN_LOGIN_USERNAME, username));
        postNameValuePairs.add(new BasicNameValuePair(USER_LOGIN_LOGIN_PASSWORD, password));
        post.setEntity(new UrlEncodedFormEntity(postNameValuePairs, UTF_8));
        CloseableHttpResponse response = getHttpClient().execute(post);
        ApiClientResponse response_ = new ApiClientResponse(response);
        response.close();
        return response_;
    }

    public ApiClientResponse apiLogout() throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(baseUrl + API_LOGOUT);
        CloseableHttpResponse execute = getHttpClient().execute(post);
        ApiClientResponse response = new ApiClientResponse(execute);
        execute.close();
        logger.debug("status {}", response.getStatusLine());
        return response;

    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public ApiClientResponse uploadRecord(String docXml, Long tdarId, Long accountId, File... files) throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(baseUrl + API_INGEST_UPLOAD);
        List<File> errors = new ArrayList<>();
        for (File f : files) {
            if (f == null || !f.exists()) {
                errors.add(f);
            }
        }
        
        if (!CollectionUtils.isEmpty(errors)) {
            logger.error(">>>> Files don't exist for: {} -- {}", tdarId, files);
        }
        // post.setHeader(new BasicHeader("Accept-Charset:","utf-8"));
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody(RECORD, docXml, ContentType.create("application/xml", Consts.UTF_8));
        processIds(tdarId, accountId, builder);
        logger.trace("uploading:{}", docXml);
        addFiles(builder, files);

        post.setEntity(builder.build());
        CloseableHttpResponse response = getHttpClient().execute(post);
        ApiClientResponse toReturn = new ApiClientResponse(response);
        response.close();
        return toReturn;
    }

    private void addFiles(MultipartEntityBuilder builder, File... files) {
        if (files == null) {
            return;
        }

        logger.debug("\tadding files: {}", Arrays.asList(files));
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

    public ApiClientResponse updateFiles(String text, Long tdarId, Long accountId, File... files) throws ClientProtocolException, IOException {
        HttpPost post = new HttpPost(baseUrl + API_INGEST_UPDATE_FILES);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody(RECORD, text, ContentType.create("application/xml", Consts.UTF_8));

        processIds(tdarId, accountId, builder);
        addFiles(builder, files);
        post.setEntity(builder.build());
        CloseableHttpResponse response = getHttpClient().execute(post);
        ApiClientResponse resp = new ApiClientResponse(response);
        response.close();
        return resp;
    }

    @SuppressWarnings("unused")
    public ApiClientResponse viewRecord(Long id) throws ClientProtocolException, IOException {
        HttpGet get = new HttpGet(String.format("%s/api/view?id=%s", baseUrl, id));
        CloseableHttpResponse execute = httpClient.execute(get);
        CloseableHttpResponse response = getHttpClient().execute(get);
        ApiClientResponse resp = new ApiClientResponse(response);
        response.close();
        return resp;

    }

}
