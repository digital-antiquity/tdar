package org.tdar.dataone;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.dataone.service.DataOneService;

@Configuration
@EnableAsync
//@EnableScheduling
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