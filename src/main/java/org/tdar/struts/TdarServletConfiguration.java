package org.tdar.struts;

import java.io.Serializable;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.struts2.dispatcher.ng.filter.StrutsExecuteFilter;
import org.apache.struts2.dispatcher.ng.filter.StrutsPrepareFilter;
import org.apache.struts2.dispatcher.ng.listener.StrutsListener;
import org.apache.struts2.sitemesh.FreemarkerDecoratorServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate4.support.OpenSessionInViewFilter;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.tdar.core.configuration.TdarAppConfiguration;
import org.tdar.core.configuration.TdarConfiguration;

import com.opensymphony.sitemesh.webapp.SiteMeshFilter;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class TdarServletConfiguration implements Serializable, WebApplicationInitializer {

    private static final long serialVersionUID = -6063648713073283277L;

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    public final String BAR = "\r\n*************************************************************************\r\n";
    EnumSet<DispatcherType> allDispacherTypes = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR);
    private String failureMessage;

    public TdarServletConfiguration() {
        try {
            TdarConfiguration.getInstance().initialize();
        } catch (Throwable t) {
            failureMessage = t.getMessage() + " (see initial exception for details)";
            logger.error("\r\n\r\n" + BAR + t.getMessage() + BAR, t);
        }
    }

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        if (StringUtils.isNotBlank(failureMessage)) {
            throw new ServletException(failureMessage);
        }
        @SuppressWarnings("resource")
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(TdarAppConfiguration.class);
        container.addListener(new ContextLoaderListener(rootContext));
        ServletRegistration.Dynamic cxf = container.addServlet("cxf", CXFServlet.class);
        cxf.setLoadOnStartup(1);
        cxf.addMapping("/services/*");
        ServletRegistration.Dynamic freemarker = container.addServlet("sitemesh-freemarker", FreemarkerDecoratorServlet.class);
        freemarker.setInitParameter("default_encoding", "UTF-8");
        freemarker.setLoadOnStartup(1);
        freemarker.addMapping("*.dec");

        container.addListener(RequestContextListener.class);
        container.addListener(StrutsListener.class);

        if (!TdarConfiguration.getInstance().isOdataEnabled()) {
            ServletRegistration.Dynamic oData = container.addServlet("odata", SpringServlet.class);
            oData.setLoadOnStartup(1);
            oData.addMapping("/odata.svc/*");
            oData.setInitParameter("javax.ws.rs.Application", "org.odata4j.jersey.producer.resources.ODataApplication");
            oData.setInitParameter("odata4j.producerfactory", "org.tdar.odata.server.TDarProducerFactory");
        }

        FilterRegistration urlRewrite = container.getFilterRegistration("UrlRewriteFilter");
        urlRewrite.addMappingForUrlPatterns(allDispacherTypes, false, "/*");

        Dynamic openSessionInView = container.addFilter("osiv-filter", OpenSessionInViewFilter.class);
        openSessionInView.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), true, "/*");

        Dynamic strutsPrepare = container.addFilter("struts-prepare", StrutsPrepareFilter.class);
        strutsPrepare.addMappingForUrlPatterns(allDispacherTypes, true, "/*");
        Dynamic sitemesh = container.addFilter("sitemesh", SiteMeshFilter.class);
        sitemesh.addMappingForUrlPatterns(allDispacherTypes, true, "/*");
        Dynamic strutsExecute = container.addFilter("struts-execute", StrutsExecuteFilter.class);
        strutsExecute.addMappingForUrlPatterns(allDispacherTypes, true, "/*");
        
    }

}