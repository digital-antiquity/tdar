package org.tdar.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.tdar.core.configuration.IntegrationAppConfiguration;

@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration()
public class TdarSearchIntegrationAppConfiguration extends IntegrationAppConfiguration implements HasSearchIndex {

    private static final long serialVersionUID = -2392015563642525226L;

    public boolean disableHibernateSearch() {
        return false;
    }

}