package org.tdar.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for executing simple, sessionless http requests.
 */
public class SimpleHttpUtils {

    private static Logger logger = LoggerFactory.getLogger(SimpleHttpUtils.class);

    /**
     * Return httpclient that trusts self-signed certs. Wraps any checked exceptions in a runtime exception.
     *
     * @return
     * @throws java.lang.RuntimeException
     */
    public static CloseableHttpClient createClient() {
        try {
            return SimpleHttpUtils.createClientChecked(null);
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("failed to create http client", e);
        }
    }

    /**
     * Return httpclient that trusts self-signed certs. Wraps any checked exceptions in a runtime exception.
     *
     * @return
     * @throws java.lang.RuntimeException
     */
    public static CloseableHttpClient createClient(Integer timeoutInSeconds) {
        try {
            return SimpleHttpUtils.createClientChecked(timeoutInSeconds);
        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("failed to create http client", e);
        }
    }

    /**
     * send post request to specified url with specified name/value pairs. Wraps any checked exceptions in a runtime exception.
     * 
     * @param url
     * @param parms
     * @return
     */
    public static CloseableHttpResponse post(String url, List<NameValuePair> parms) {
        HttpPost post = new HttpPost(url);
        post.setEntity(new UrlEncodedFormEntity(parms, Consts.UTF_8));
        CloseableHttpResponse httpResponse = null;
        try {
            httpResponse = createClient().execute(post);
        } catch (IOException e) {
            logger.error("so, this happened: {}", e);
            throw new RuntimeException("IO when attempting to execute post", e);
        }
        return httpResponse;
    }

    /**
     * parse a closeable response and return a Pair containint the HttpStatus code and the response body. Swallows all exceptions and closes response object.
     * 
     * @return
     */
    public static Pair<Integer, String> parseResponse(CloseableHttpResponse closeableResponse) {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(closeableResponse.getEntity().getContent()))) {
            StringBuilder sb = new StringBuilder();
            for (String line : IOUtils.readLines(rd)) {
                sb.append(line);
            }
            String html = sb.toString();
            Pair<Integer, String> pair = new Pair<>(closeableResponse.getStatusLine().getStatusCode(), html);
            return pair;
        } catch (IOException ignored) {
        }
        return null;
    }

    /** return a basic name/value pair **/
    static public NameValuePair nameValuePair(String key, String val) {
        return new BasicNameValuePair(key, val);
    }

    private static CloseableHttpClient createClientChecked(Integer timeoutInSeconds)
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        RequestConfig requestConfig = RequestConfig.DEFAULT;
        if (timeoutInSeconds != null) {
            int timeoutInMs = timeoutInSeconds * 1000;
            requestConfig = RequestConfig.custom().setConnectTimeout(timeoutInMs).setConnectionRequestTimeout(timeoutInMs).setSocketTimeout(timeoutInMs)
                    .build();
        }
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(requestConfig).evictExpiredConnections().build();
        return httpclient;
    }

}
