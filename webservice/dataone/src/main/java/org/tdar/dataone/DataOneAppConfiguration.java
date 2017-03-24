package org.tdar.dataone;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.tdar.core.configuration.AbstractAppConfiguration;

@Configuration
@EnableAsync
@EnableScheduling
@Primary
@ComponentScan(basePackages = { "org.tdar.dataone", "org.tdar.core.dao.base" },
        excludeFilters = {
                @Filter(type = FilterType.REGEX, pattern = "org.tdar.core.(service|filestore).*")
        },
        includeFilters = {
                @Filter(type = FilterType.REGEX, pattern = "org.tdar.core.dao.(GenericDao|ObfuscationDao)")
        })

public class DataOneAppConfiguration extends AbstractAppConfiguration {

    private static final long serialVersionUID = 1462888504584855775L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    // @Override
    // public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    // taskRegistrar.setScheduler(taskScheduler());
    // }
    //
    //
    // @Bean(destroyMethod = "shutdown")
    // public Executor taskScheduler() {
    // return Executors.newScheduledThreadPool(2);
    // }

}