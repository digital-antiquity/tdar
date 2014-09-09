package org.tdar.odata.server;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;

public class HttpTestingClient implements ITestingClient {

    private HttpClient client;

    public HttpTestingClient() {
        super();
    }

    @Override
    public void createClient() throws Exception {
        client = new HttpClient();
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
    }

    @Override
    public void startClient() throws Exception {
        if (client != null)
        {
            client.start();
        }
    }

    @Override
    public void stopClient() throws Exception {
        if (client != null)
        {
            client.stop();
        }
    }

    @Override
    public ContentExchange sendRequest(String url) throws IOException {
        ContentExchange exchange = new ContentExchange(true);
        exchange.setURL(url);
        String encoding = Base64.encodeBase64String((Constant.TEST_USER_NAME + ":" + Constant.TEST_PASSWORD).getBytes("UTF-8"));
        exchange.addRequestHeader("Authorization", "Basic " + encoding);
        client.send(exchange);
        return exchange;
    }
}
