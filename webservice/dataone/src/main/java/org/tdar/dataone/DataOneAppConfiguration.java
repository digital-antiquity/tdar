package org.tdar.dataone;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.tdar.core.configuration.SimpleAppConfiguration;

@Configuration
@EnableAsync
//@EnableScheduling
@Primary
@ComponentScan(basePackages = { "org.tdar.dataone", "org.tdar.dao" },
excludeFilters = {
        @Filter(type = FilterType.ASSIGNABLE_TYPE,
                value = {
                        SimpleAppConfiguration.class
                })
})

public class DataOneAppConfiguration extends SimpleAppConfiguration {

    private static final long serialVersionUID = 1462888504584855775L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());


//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        taskRegistrar.setScheduler(taskScheduler());
//    }
//
//
//    @Bean(destroyMethod = "shutdown")
//    public Executor taskScheduler() {
//        return Executors.newScheduledThreadPool(2);
//    }

}