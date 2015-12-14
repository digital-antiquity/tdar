package org.tdar.core.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;

@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration()
@ComponentScan(basePackages = { "org.tdar" },
excludeFilters = {
        @Filter(type = FilterType.ASSIGNABLE_TYPE,
                value = {
                        SimpleAppConfiguration.class
                })
})
@EnableCaching
public class TdarSearchAppConfiguration extends TdarAppConfiguration {


    private static final long serialVersionUID = -4500782466973090097L;

    public boolean disableHibernateSearch() {
        return false;
    }

}