package org.tdar.core;

import org.springframework.test.context.ContextConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.web.TdarWebAppConfiguration;

@ContextConfiguration(classes=TdarWebAppConfiguration.class)
public abstract class AbstractIntegrationWebTestCase extends AbstractIntegrationTestCase {

}
