package org.tdar.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.tdar.core.configuration.SimpleAppConfiguration;

@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration()
public class TdarSimpleSearchAppConfiguration extends SimpleAppConfiguration implements HasSearchIndex {


	private static final long serialVersionUID = 1223691178620339445L;

	public boolean disableHibernateSearch() {
        return false;
    }
    

}