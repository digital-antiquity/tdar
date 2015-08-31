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

    public TdarAppConfiguration() {
        logger.debug("Initializing tDAR Application Context");
    }

    @Bean
    // @Value("#{'${my.list.of.strings}'.split(',')}")
    public FreeMarkerConfigurationFactoryBean getFreemarkerMailConfiguration() {
        FreeMarkerConfigurationFactoryBean freemarkerConfig = new FreeMarkerConfigurationFactoryBean();
        List<String> templateLoaderPaths = new ArrayList<>();
        templateLoaderPaths.add("classpath:/freemarker-templates");
        templateLoaderPaths.add("file:/WEB-INF/freemarker-templates");
        templateLoaderPaths.add("classpath:/WEB-INF/content");
        templateLoaderPaths.add("classpath:src/main/webapp");
        templateLoaderPaths.add("file:src/main/webapp");
        templateLoaderPaths.add("classpath:/freemarker-templates-test");
        templateLoaderPaths.add("classpath:/templates");
        templateLoaderPaths.add("file:/templates");
        freemarkerConfig.setTemplateLoaderPaths(templateLoaderPaths.toArray(new String[0]));
        return freemarkerConfig;
    }

    @Bean(name = "AuthenticationProvider")
    public AuthenticationProvider getAuthProvider() throws IOException {
        return new CrowdRestDao();
    }

    @Bean(name = "DoiProvider")
    public ExternalIDProvider getIdProvider() throws IOException {
        return new EZIDDao();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(2);
        pool.setMaxPoolSize(5);
        pool.setThreadNamePrefix("pool-");
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Bean
    public SimpleCacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(getCachesToLoad());
        return cacheManager;
    }

    protected Collection<? extends Cache> getCachesToLoad() {
        return new ArrayList<>();
    }

}