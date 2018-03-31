package org.tdar.oai;

import java.io.IOException;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.tdar.core.configuration.AbstractAppConfiguration;
import org.tdar.core.dao.external.auth.AuthenticationProvider;

@Configuration
@Primary
@ComponentScan(basePackages = { "org.tdar.oai", "org.tdar.core.dao.base" },
        excludeFilters = {
                @Filter(type = FilterType.REGEX, pattern = "org.tdar.core.(service|filestore).*")
        },
        includeFilters = {
                @Filter(type = FilterType.REGEX, pattern = "org.tdar.core.dao.(GenericDao|ObfuscationDao)")
        })

public class OaiPmhAppConfiguration extends AbstractAppConfiguration {

    private static final long serialVersionUID = 4421598143486325549L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Bean(name = "AuthenticationProvider")
    public AuthenticationProvider getAuthProvider() throws IOException {
        return null;
    }

}