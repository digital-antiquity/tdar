package org.tdar.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.orm.hibernate4.support.OpenSessionInViewFilter;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.tdar.core.configuration.ConfigurationAssistant;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.core.configuration.TdarAppConfiguration;
import org.tdar.core.configuration.TdarConfiguration;

import net.sf.ehcache.constructs.web.ShutdownListener;

public abstract class AbstractServletConfiguration {

    public static final String ALL_PATHS = "/*";
    public static final String HOSTED_CONTENT_BASE_URL = "/hosted";
    public static final String BAR = "*************************************************************************";
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    // NOTE: when changing these, you must test both TOMCAT and JETTY as they behave differently
    EnumSet<DispatcherType> allDispacherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR);
    EnumSet<DispatcherType> strutsDispacherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ERROR);

    protected TdarConfiguration configuration = TdarConfiguration.getInstance();
    private String failureMessage;

    public AbstractServletConfiguration(String msg) {
        logger.debug(msg);
        try {
            TdarConfiguration.getInstance().initialize();
        } catch (Throwable t) {
            setFailureMessage(t.getMessage() + " (see initial exception for details)");
            logger.error("\r\n\r\n" + BAR + "\r\n" + t.getMessage() + "\r\n" + BAR + "\r\n", t);
        }

    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public Class<? extends SimpleAppConfiguration> getConfigurationClass() {
        return TdarAppConfiguration.class;
    }

    protected void setupContainer(ServletContext container) {
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(getConfigurationClass());
        container.addListener(new ContextLoaderListener(rootContext));
        container.addListener(RequestContextListener.class);

        container.addListener(ShutdownListener.class);
    }

    protected void setupOpenSessionInViewFilter(ServletContext container) {
        Dynamic openSessionInView = container.addFilter("osiv-filter", OpenSessionInViewFilter.class);
        openSessionInView.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false, ALL_PATHS);
    }
}
