package org.tdar.odata.server;

import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.tdar.core.configuration.TdarConfiguration;

public class HttpTestingClient implements ITestingClient {

    private HttpClient client;

    public HttpTestingClient() {
        super();
    }

    @Override
    public void createClient() throws Exception {
        client = new HttpClient();
        // client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
    }

    @Override
    public HttpMethodBase sendRequest(String url) throws IOException {
        client.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds = new UsernamePasswordCredentials(Constant.TEST_USER_NAME, Constant.TEST_PASSWORD);
        TdarConfiguration instance = TdarConfiguration.getInstance();
        client.getState().setCredentials(new AuthScope("localhost", 8888, AuthScope.ANY_REALM), defaultcreds);
        GetMethod get = new GetMethod(url);
        get.setDoAuthentication(true);
        client.executeMethod(get);
        // String encoding = Base64.encodeBase64String((Constant.TEST_USER_NAME + ":" + Constant.TEST_PASSWORD).getBytes("UTF-8"));
        // exchange.addRequestHeader("Authorization", "Basic " + encoding);
        // client.send(exchange);
        return get;
    }
}
