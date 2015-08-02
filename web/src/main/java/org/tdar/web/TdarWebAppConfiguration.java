package org.tdar.web;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.tdar.core.configuration.TdarAppConfiguration;
import org.tdar.core.service.processes.EmptyProcessManager;
import org.tdar.core.service.processes.AutowiredProcessManager;

@EnableAsync
@EnableCaching
@EnableScheduling
@Configuration
public class TdarWebAppConfiguration extends TdarAppConfiguration implements SchedulingConfigurer, AsyncConfigurer {

	private static final long serialVersionUID = 3444580855012578739L;

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

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(taskScheduler());
	}
	
	@Bean(name="processManager")
	public EmptyProcessManager processManager() {
		return new AutowiredProcessManager();
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

	@Bean(destroyMethod = "shutdown")
	public Executor taskScheduler() {
		return Executors.newScheduledThreadPool(2);
	}
	
}
