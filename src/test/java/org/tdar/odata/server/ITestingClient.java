package org.tdar.odata.server;

import java.io.IOException;

import org.eclipse.jetty.client.ContentExchange;

public interface ITestingClient {

    void createClient() throws Exception;

    void startClient() throws Exception;

    void stopClient() throws Exception;

    ContentExchange sendRequest(String url) throws IOException;

}