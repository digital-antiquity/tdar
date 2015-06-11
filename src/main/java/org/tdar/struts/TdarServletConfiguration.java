package org.tdar.struts;

import java.io.Serializable;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import net.sf.ehcache.constructs.web.ShutdownListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.struts2.dispatcher.ng.filter.StrutsExecuteFilter;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareFilter;
import org.apache.struts2.dispatcher.ng.listener.StrutsListener;
import org.apache.struts2.sitemesh.FreemarkerDecoratorServlet;
import org.ebaysf.web.cors.CORSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.support.OpenSessionInViewFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.tdar.core.configuration.TdarAppConfiguration;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.web.StaticContentServlet;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import com.opensymphony.sitemesh.webapp.SiteMeshFilter;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class TdarServletConfiguration implements Serializable, WebApplicationInitializer {

    private static final String ALL_PATHS = "/*";

    private static final long serialVersionUID = -6063648713073283277L;

    public static final String HOSTED_CONTENT_BASE_URL = "/hosted";

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    public final String BAR = "*************************************************************************";
    // NOTE: when changing these, you must test both TOMCAT and JETTY as they behave differently
    EnumSet<DispatcherType> allDispacherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR);
    EnumSet<DispatcherType> strutsDispacherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ERROR);
    private String failureMessage;

    public TdarServletConfiguration() {
        logger.debug("Initializing tDAR Servlet");

        try {
            TdarConfiguration.getInstance().initialize();
        } catch (Throwable t) {
            failureMessage = t.getMessage() + " (see initial exception for details)";
            logger.error("\r\n\r\n" + BAR + "\r\n" + t.getMessage() + "\r\n" + BAR + "\r\n", t);
        }
    }

    TdarConfiguration configuration = TdarConfiguration.getInstance();

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        if (StringUtils.isNotBlank(failureMessage)) {
            throw new ServletException(failureMessage);
        }
        if (!configuration.isProductionEnvironment()) {
            onDevStartup(container);
        }

        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(TdarAppConfiguration.class);
        container.addListener(new ContextLoaderListener(rootContext));
        configureCxfForTag(container);
        configureFreemarker(container);

        container.addListener(RequestContextListener.class);
        container.addListener(StrutsListener.class);
        container.addListener(ShutdownListener.class);

        configureOdata(container);

        configureUrlRewriteRule(container);

        if (configuration.getContentSecurityPolicyEnabled()) {
            logger.debug("enabling cors");
            configureCorsFilter(container);
        }

        Dynamic openSessionInView = container.addFilter("osiv-filter", OpenSessionInViewFilter.class);
        openSessionInView.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false, ALL_PATHS);

        configureStrutsAndSiteMeshFilters(container);

        if (!configuration.isStaticContentEnabled()) {
            ServletRegistration.Dynamic staticContent = container.addServlet("static-content", StaticContentServlet.class);
            staticContent.setInitParameter("default_encoding", "UTF-8");
            staticContent.setLoadOnStartup(1);
            staticContent.addMapping(HOSTED_CONTENT_BASE_URL + "/*");
        }

    }

    private void onDevStartup(ServletContext container) {
        if (configuration.isProductionEnvironment()) {
            throw new IllegalStateException("dev startup tasks not allowed in production");
        }
        logServerInfo(container);
    }

    /**
     * Logs out basic server information for the specified condtainer
     */
    private void logServerInfo(ServletContext container) {
        logger.info(BAR);
        logger.info("SERVER INFO");
        logger.info("\t       server:{}", container.getServerInfo());
        logger.info("\t servlet spec:{}.{}", container.getMajorVersion(), container.getMinorVersion());
        logger.info("\t context name:{}", container.getServletContextName());
        logger.info(BAR);
    }

    private void configureFreemarker(ServletContext container) {
        ServletRegistration.Dynamic freemarker = container.addServlet("sitemesh-freemarker", FreemarkerDecoratorServlet.class);
        freemarker.setInitParameter("default_encoding", "UTF-8");
        freemarker.setLoadOnStartup(1);
        freemarker.addMapping("*.dec");
    }

    private void configureCxfForTag(ServletContext container) {
        ServletRegistration.Dynamic cxf = container.addServlet("cxf", CXFServlet.class);
        cxf.setLoadOnStartup(1);
        cxf.addMapping("/services/*");
    }

    private void configureOdata(ServletContext container) {
        if (configuration.isOdataEnabled()) {
            ServletRegistration.Dynamic oData = container.addServlet("odata", SpringServlet.class);
            oData.setLoadOnStartup(1);
            oData.addMapping("/odata.svc/*");
            oData.setInitParameter("javax.ws.rs.Application", "org.odata4j.jersey.producer.resources.ODataApplication");
            oData.setInitParameter("odata4j.producerfactory", "org.tdar.odata.server.TDarProducerFactory");
        }
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

    private void configureCorsFilter(ServletContext container) {
        // http://software.dzhuvinov.com/cors-filter-configuration.html [doesn't work]
        // https://github.com/eBay/cors-filter [seems to not work with same-origin post requests on alpha
        Dynamic corsFilter = container.addFilter("CORS", CORSFilter.class);
        corsFilter.setInitParameter("cors.allowed.origins", configuration.getAllAllowedDomains());
        corsFilter.setInitParameter("cors.preflight.maxage", "3600");
        corsFilter.setInitParameter("cors.allowed.methods", "GET,POST,HEAD,PUT,DELETE,OPTIONS");
        corsFilter.setInitParameter("cors.logging.enabled", "true");
        corsFilter.addMappingForUrlPatterns(strutsDispacherTypes, false, ALL_PATHS);
    }

}
