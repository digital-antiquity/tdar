package org.tdar.core.configuration;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.tdar.core.cache.Caches;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.CrowdRestDao;
import org.tdar.core.dao.external.pid.EZIDDao;
import org.tdar.core.dao.external.pid.ExternalIDProvider;

@Configuration
@EnableCaching
public class TdarAppConfiguration extends IntegrationAppConfiguration implements Serializable {

    private static final long serialVersionUID = 6038273491995542363L;
    public static final int SCHEDULER_POOL_SIZE = 2;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }


    @Bean
    public SimpleCacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(getCachesToLoad());
        return cacheManager;
    }

    protected Collection<? extends Cache> getCachesToLoad() {
        return new ArrayList<>();

    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(SCHEDULER_POOL_SIZE);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncUncaughtExceptionHandler() {

            @Override
            public void handleUncaughtException(Throwable ex, Method method, Object... params) {
                logger.error("exception in async: {} {} ", method, params);
            }
        };
    }

}