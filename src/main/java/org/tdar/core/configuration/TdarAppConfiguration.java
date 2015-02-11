package org.tdar.core.configuration;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.persistence.Transient;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.CrowdRestDao;
import org.tdar.core.dao.external.pid.EZIDDao;
import org.tdar.core.dao.external.pid.ExternalIDProvider;
import org.tdar.web.SessionData;

@Configuration
@ComponentScan(basePackages = { "org.tdar" })
@EnableTransactionManagement()
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@EnableAsync
@EnableCaching
@ImportResource(value = { "classpath:/spring-local-settings.xml" })
public class TdarAppConfiguration implements Serializable, SchedulingConfigurer, AsyncConfigurer {

    private static final long serialVersionUID = 6038273491995542363L;
    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public TdarAppConfiguration() {
        logger.debug("Initializing tDAR Application Context");
    }

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(@Qualifier("tdarMetadataDataSource") DataSource dataSource) {
        Properties properties = new Properties();

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
        builder.scanPackages(new String[] { "org.tdar.core" });
        builder.addPackages(new String[] { "org.tdar.core" });
        builder.addProperties(properties);
        return builder.buildSessionFactory();
    }

    @Bean(name = "mailSender")
    public JavaMailSender getJavaMailSender(@Value("${mail.smtp.host:localhost}") String hostname) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(hostname);
        return sender;
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

    @Bean(name = "sessionData")
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public SessionData getSessionData() {
        return new SessionData();
    }

    @Bean
    @Primary
    public HibernateTransactionManager transactionManager(@Qualifier("tdarMetadataDataSource") DataSource dataSource) throws PropertyVetoException {
        HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(getSessionFactory(dataSource));
        return hibernateTransactionManager;
    }

    @Bean
    @Qualifier("tdarDataTx")
    public DataSourceTransactionManager dataTransactionManager(@Qualifier("tdarDataImportDataSource") DataSource dataSource) throws PropertyVetoException {
        DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager(dataSource);
        return dataSourceTransactionManager;
    }

    @Bean
    public SimpleCacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<>();
        caches.add(cacheBean());
        caches.add(new ConcurrentMapCache("rssFeed"));
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    @Bean
    public Cache cacheBean() {
        Cache cache = new ConcurrentMapCache("default");
        return cache;
    }

    @Bean(name = "AuthenticationProvider")
    public AuthenticationProvider getAuthProvider() throws IOException {
        return new CrowdRestDao();
    }

    @Bean(name = "DoiProvider")
    public ExternalIDProvider getIdProvider() throws IOException {
        return new EZIDDao();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
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

    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(2);
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