package org.tdar.odata.server;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpTestingClient implements ITestingClient {

    private CloseableHttpClient client;

    public HttpTestingClient() {
        super();
    }

    @Override
    public void createClient() throws Exception {
        client = HttpClientBuilder.create().build();
        // client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
    }

    @Override
    public HttpResponse sendRequest(String url) throws IOException {
//        client.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds = new UsernamePasswordCredentials(Constant.TEST_USER_NAME, Constant.TEST_PASSWORD);
        HttpClientContext context = HttpClientContext.create();
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope("localhost", 8888, AuthScope.ANY_REALM), defaultcreds);
        context.setCredentialsProvider(credsProvider);
//        context.setAuthSchemeRegistry(authRegistry);
//        context.setAuthCache(authCache);
        HttpGet get = new HttpGet(url);
//        get.setDoAuthentication(true);
//        context.set
        return client.execute(get, context);
//         String encoding = Base64.encodeBase64String((Constant.TEST_USER_NAME + ":" + Constant.TEST_PASSWORD).getBytes("UTF-8"));
        // exchange.addRequestHeader("Authorization", "Basic " + encoding);
        // client.send(exchange);
    }
}
