package org.tdar.dataone;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.tdar.core.configuration.SimpleAppConfiguration;

@Configuration
@EnableAsync
//@EnableScheduling
public class DataOneAppConfiguration extends SimpleAppConfiguration {

    private static final long serialVersionUID = 1462888504584855775L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());


}