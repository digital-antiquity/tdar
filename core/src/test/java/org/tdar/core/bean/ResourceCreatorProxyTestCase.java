package org.tdar.core.bean;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.service.ResourceCreatorProxy;

public class ResourceCreatorProxyTestCase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testRCP() {
        ResourceCreatorProxy rcp = new ResourceCreatorProxy();
        logger.debug("{}",rcp);
    }
}
