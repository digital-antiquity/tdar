package org.tdar.core.configuration;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.CrowdRestDao;
import org.tdar.core.dao.external.pid.EZIDDao;
import org.tdar.core.dao.external.pid.ExternalIDProvider;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration()
@EnableCaching
public class TdarAppConfiguration extends IntegrationAppConfiguration implements Serializable {

    private static final long serialVersionUID = 6038273491995542363L;

    public TdarAppConfiguration() {
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
    public SimpleCacheManager cacheManager() {
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
            ComboPooledDataSource ds = new ComboPooledDataSource();
            ds.setDriverClass(env.getRequiredProperty("javax.persistence.jdbc.driver"));
            ds.setJdbcUrl(env.getRequiredProperty("javax.persistence.jdbc.url"));
            ds.setUser(env.getRequiredProperty("javax.persistence.jdbc.user"));
            ds.setPassword(env.getRequiredProperty("javax.persistence.jdbc.password"));
            ds.setAcquireIncrement(5);
            ds.setIdleConnectionTestPeriod(60);
            ds.setMaxPoolSize(env.getRequiredProperty("tdardata.maxConnections", Integer.class));
            ds.setMaxStatements(50);
            ds.setMinPoolSize(env.getRequiredProperty("tdardata.minConnections", Integer.class));
            return ds;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}