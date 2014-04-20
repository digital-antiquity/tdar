package org.tdar.odata.server;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.odata4j.jersey.producer.server.ODataJerseyServer;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.ODataProducerFactory;
import org.odata4j.producer.resources.DefaultODataApplication;
import org.odata4j.producer.resources.DefaultODataProducerProvider;
import org.odata4j.producer.resources.RootApplication;
import org.odata4j.producer.server.ODataServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.AuthenticationToken;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.web.SessionData;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

public class HttpTestingServer implements ITestingServer {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private SessionData sessionData;

    @Autowired(required = true)
    private ODataProducerFactory producerFactory;

    private ODataServer server;

    private TdarUser person;

    public SessionData getSessionData() {
        return sessionData;
    }

    @Override
    public void createServer() {
        configureProducer();
        Authenticator authenticator = new BasicAuthenticator("tDAR_odata_realm")
        {

            @Override
            public Result authenticate(HttpExchange exchange)
            {
                return super.authenticate(exchange);
            }

            @Override
            public boolean checkCredentials(String username, String password) {
                boolean isAuthenticated = false;
                getSessionData().setAuthenticationToken(null);
                if (Constant.TEST_USER_NAME.equals(username) && Constant.TEST_PASSWORD.equals(password))
                {
                    AuthenticationToken authenticationToken = AuthenticationToken.create(person);
                    getSessionData().setAuthenticationToken(authenticationToken);
                    isAuthenticated = true;
                    getLogger().info("Session data set for OData request: authentication succeeded.");
                }
                else
                {
                    getLogger().info("Session data NOT set for OData request: authentication failed.");
                }
                return isAuthenticated;
            }
        };

        Filter filter = new SpringAdapterRequestContextFilter();
        server = new ODataJerseyServer(Constant.SERVICE_URL, DefaultODataApplication.class, RootApplication.class)
                .addHttpServerFilter(filter)
                .setHttpServerAuthenticator(authenticator);
    }

    @Override
    public void startServer() throws Exception {
        server.start();
    }

    @Override
    public void stopServer() throws Exception {
        server.stop();
    }

    protected void configureProducer() {
        Properties properties = null;
        ODataProducer producer = producerFactory.create(properties);
        DefaultODataProducerProvider.setInstance(producer);
    }

    private Logger getLogger() {
        return logger;
    }

    @Override
    public void setPerson(TdarUser person) {
        this.person = person;
    }
}