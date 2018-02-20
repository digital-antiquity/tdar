package org.tdar.core.configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.tdar.core.cache.Caches;
import org.tdar.core.service.processes.manager.AutowiredProcessManager;
import org.tdar.core.service.processes.manager.ProcessManager;

@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration()
@EnableCaching
public class TdarBaseWebAppConfiguration extends TdarAppConfiguration implements SchedulingConfigurer {

    private static final long serialVersionUID = 2229498250188301893L;

    // @Bean(destroyMethod = "shutdown")
    // public net.sf.ehcache.CacheManager ehCacheManager() {
    // CacheConfiguration cacheConfiguration = new CacheConfiguration();
    // cacheConfiguration.setName("myCacheName");
    //// cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
    // cacheConfiguration.diskStorePath(System.getProperty("java.io.tmpdir"));
    // cacheConfiguration.maxElementsOnDisk(1000);
    // cacheConfiguration.maxElementsInMemory(100);
    // cacheConfiguration.eternal(true);
    //
    // net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
    // config.addCache(cacheConfiguration);
    //
    // return net.sf.ehcache.CacheManager.create(config);
    // }
    // http://stackoverflow.com/questions/21944202/using-ehcache-in-spring-4-without-xml#21944585
    // http://www.codingpedia.org/ama/spring-caching-with-ehcache/
    // @Bean
    // @Override
    // public AbstractCacheManager cacheManager() {
    // return new EhCacheCacheManager(ehCacheManager());
    // }

    @Override
    public Collection<? extends Cache> getCachesToLoad() {
        List<Cache> caches = new ArrayList<>();
        caches.add(new ConcurrentMapCache(Caches.RSS_FEED));
        caches.add(new ConcurrentMapCache(Caches.BROWSE_DECADE_COUNT_CACHE));
        caches.add(new ConcurrentMapCache(Caches.BROWSE_YEAR_COUNT_CACHE));
        caches.add(new ConcurrentMapCache(Caches.DECADE_COUNT_CACHE));
        caches.add(new ConcurrentMapCache(Caches.HOMEPAGE_FEATURED_ITEM_CACHE));
        caches.add(new ConcurrentMapCache(Caches.HOMEPAGE_MAP_CACHE));
        caches.add(new ConcurrentMapCache(Caches.HOMEPAGE_RESOURCE_COUNT_CACHE));
        caches.add(new ConcurrentMapCache(Caches.WEEKLY_POPULAR_RESOURCE_CACHE));
        return caches;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Bean(name = "processManager")
    public ProcessManager processManager() {
        return new AutowiredProcessManager();
    }

    @Bean(destroyMethod = "shutdown")
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(2);
    }
}