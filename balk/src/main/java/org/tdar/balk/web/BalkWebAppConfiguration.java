package org.tdar.balk.web;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.CrowdRestDao;

@Configuration
@EnableScheduling
@ImportResource(value = { "classpath:spring-local-settings.xml" })
public class BalkWebAppConfiguration extends SimpleAppConfiguration {

    private static final long serialVersionUID = 3444580855012578739L;

    @Override
    public String getHibernatePackageScan() {
        return "org.tdar.balk.bean";
    }

    @Override
    public boolean disableHibernateSearch() {
        return true;
    }

    @Bean(name = "AuthenticationProvider")
    public AuthenticationProvider getAuthProvider() throws IOException {
        return new CrowdRestDao();
    }
    
    @Override
    public String getMetadataDatabaseName() {
        return "tdarbalk";
    }

    @Bean(name="obfuscationEnabled")
    public Boolean isObfuscationEnabled() {
        return false;
    }

}
