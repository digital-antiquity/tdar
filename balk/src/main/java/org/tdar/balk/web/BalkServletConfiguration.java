package org.tdar.balk.web;

import java.io.Serializable;

import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.struts2.dispatcher.filter.StrutsExecuteFilter;
import org.apache.struts2.dispatcher.filter.StrutsPrepareFilter;
import org.apache.struts2.dispatcher.listener.StrutsListener;
import org.apache.struts2.sitemesh.FreemarkerDecoratorServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.web.AbstractServletConfiguration;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.opensymphony.sitemesh.webapp.SiteMeshFilter;

public class BalkServletConfiguration extends AbstractServletConfiguration
        implements Serializable, WebApplicationInitializer {

    private static final long serialVersionUID = -6063648713073283277L;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public BalkServletConfiguration() {
        super("Initializing tDAR Servlet");
    }

    @Override
    public String getAppPropertyPrefix() {
        return "balk";
    }

    @Override
    public Class<? extends SimpleAppConfiguration> getConfigurationClass() {
        return BalkWebAppConfiguration.class;
    }

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        if (StringUtils.isNotBlank(getFailureMessage())) {
            throw new ServletException(getFailureMessage());
        }
        if (!configuration.isProductionEnvironment()) {
            onDevStartup(container);
        }

        setupContainer(container);
        container.addListener(StrutsListener.class);

        configureUrlRewriteRule(container);

        setupOpenSessionInViewFilter(container);

        configureFreemarker(container);

        configureStrutsAndSiteMeshFilters(container);

    }

    private void onDevStartup(ServletContext container) {
        if (configuration.isProductionEnvironment()) {
            throw new IllegalStateException("dev startup tasks not allowed in production");
        }
//      logServerInfo(container);
    }

    private void configureFreemarker(ServletContext container) {
        ServletRegistration.Dynamic freemarker = container.addServlet("sitemesh-freemarker",
                FreemarkerDecoratorServlet.class);
        freemarker.setInitParameter("default_encoding", "UTF-8");
        freemarker.setLoadOnStartup(1);
        freemarker.addMapping("*.dec");
    }

    @SuppressWarnings("unused")
    private void configureCxfForTag(ServletContext container) {
        ServletRegistration.Dynamic cxf = container.addServlet("cxf", CXFServlet.class);
        cxf.setLoadOnStartup(1);
        cxf.addMapping("/services/*");
    }


    private void configureUrlRewriteRule(ServletContext container) {
        Dynamic urlRewriteFilter = container.addFilter("URLRewriteFilter", UrlRewriteFilter.class);
        urlRewriteFilter.addMappingForUrlPatterns(strutsDispacherTypes, false, ALL_PATHS);
        urlRewriteFilter.setInitParameter("confReloadCheckInterval", configuration.getURLRewriteRefresh());
        urlRewriteFilter.setInitParameter("logLevel", "slf4j");
    }

    private void configureStrutsAndSiteMeshFilters(ServletContext container) {
        Dynamic strutsPrepare = container.addFilter("struts-prepare", StrutsPrepareFilter.class);
        strutsPrepare.addMappingForUrlPatterns(strutsDispacherTypes, false, ALL_PATHS);
        Dynamic sitemesh = container.addFilter("sitemesh", SiteMeshFilter.class);
        sitemesh.addMappingForUrlPatterns(strutsDispacherTypes, false, ALL_PATHS);
        Dynamic strutsExecute = container.addFilter("struts-execute", StrutsExecuteFilter.class);
        strutsExecute.addMappingForUrlPatterns(strutsDispacherTypes, false, ALL_PATHS);
    }

}
