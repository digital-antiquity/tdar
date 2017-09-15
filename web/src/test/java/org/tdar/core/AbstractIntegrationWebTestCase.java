package org.tdar.core;

import org.springframework.test.context.ContextConfiguration;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.configuration.TdarBaseWebAppConfiguration;

@ContextConfiguration(classes=TdarBaseWebAppConfiguration.class)
public abstract class AbstractIntegrationWebTestCase extends AbstractIntegrationTestCase {

}
