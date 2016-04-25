package org.tdar.oai.server;

import javax.persistence.Transient;

import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JerseyResourceInitializer extends ResourceConfig {
    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public JerseyResourceInitializer() {
        logger.debug("starting oai-pmh server");
        packages(true, "org.tdar.oai.server");
        register(OaiPmhServer.class);
    }

}