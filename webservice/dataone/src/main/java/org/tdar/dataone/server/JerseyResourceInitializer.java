package org.tdar.dataone.server;

import javax.persistence.Transient;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.dataone.server.jibx.JIBXBodyReader;
import org.tdar.dataone.server.jibx.JIBXBodyWriter;;

public class JerseyResourceInitializer extends ResourceConfig {
    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public JerseyResourceInitializer() {

        packages(true, "org.tdar.dataone", "org.dataone");
        logger.debug("REGISTERING JERSEY");
        register(JIBXBodyReader.class, MessageBodyReader.class);
        register(JIBXBodyWriter.class, MessageBodyWriter.class);
    }

}