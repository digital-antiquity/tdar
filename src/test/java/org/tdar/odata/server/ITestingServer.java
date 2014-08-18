package org.tdar.odata.server;

import org.tdar.core.bean.entity.Person;

public interface ITestingServer {

    void createServer();

    void startServer() throws Exception;

    void stopServer() throws Exception;

    void setPerson(Person person);

}