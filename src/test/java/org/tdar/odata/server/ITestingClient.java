package org.tdar.odata.server;

import java.io.IOException;

import org.apache.commons.httpclient.HttpMethodBase;

public interface ITestingClient {

    void createClient() throws Exception;

    // void startClient() throws Exception;
    //
    // void stopClient() throws Exception;

    HttpMethodBase sendRequest(String url) throws IOException;

}