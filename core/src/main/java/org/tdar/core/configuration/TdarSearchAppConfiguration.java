package org.tdar.core.configuration;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.sql.DataSource;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.CrowdRestDao;
import org.tdar.core.dao.external.pid.EZIDDao;
import org.tdar.core.dao.external.pid.ExternalIDProvider;

@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration()
@EnableCaching
public class TdarSearchAppConfiguration extends TdarAppConfiguration implements Serializable {


    private static final long serialVersionUID = -4500782466973090097L;

    public TdarSearchAppConfiguration() {
        logger.debug("Initializing tDAR Application Context");
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
    public AbstractCacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(getCachesToLoad());
        return cacheManager;
    }

    protected Collection<? extends Cache> getCachesToLoad() {
        return new ArrayList<>();
    }

    public boolean disableHibernateSearch() {
        return false;
    }

    @Bean(name = "tdarGeoDataSource")
    public DataSource tdarGeoDataSource() {
        try {
            String prefix = "tdargisdata";
            return configureDataSource(prefix);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}