package org.tdar.odata.server;

import java.io.IOException;

import org.apache.http.HttpResponse;

public interface ITestingClient {

    void createClient() throws Exception;

    // void startClient() throws Exception;
    //
    // void stopClient() throws Exception;

HttpResponse sendRequest(String url) throws IOException;

}