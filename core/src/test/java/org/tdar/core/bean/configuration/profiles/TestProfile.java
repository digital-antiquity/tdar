package org.tdar.core.bean.configuration.profiles;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("test")
@EnableScheduling
public class TestProfile implements AsyncConfigurer {

    public transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Executor getAsyncExecutor() {
        SyncTaskExecutor executor = new SyncTaskExecutor();
        // executor.setCorePoolSize(2);
        // executor.setMaxPoolSize(5);
        // executor.setQueueCapacity(50);
        // executor.setThreadNamePrefix("async-");
        // executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {

            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                logger.error("exception in async: {} {} ", method, params, ex);
            }
        };
    }

}
