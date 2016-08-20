package org.tdar.balk.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.tdar.core.configuration.TdarBaseWebAppConfiguration;

@Configuration
@ImportResource(value = { "classpath:spring-local-settings.xml" })

public class BalkWebAppConfiguration extends TdarBaseWebAppConfiguration {

    private static final long serialVersionUID = 3444580855012578739L;


    @Override
    public boolean disableHibernateSearch() {
        return true;
    }

}
