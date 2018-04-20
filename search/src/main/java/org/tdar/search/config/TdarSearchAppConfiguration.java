package org.tdar.search.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.tdar.core.configuration.TdarAppConfiguration;

@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration()
@EnableCaching
public class TdarSearchAppConfiguration extends TdarAppConfiguration implements HasSearchIndex {

    private static final long serialVersionUID = -4500782466973090097L;

    public boolean disableHibernateSearch() {
        return false;
    }

}