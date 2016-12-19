package org.tdar.tag.config;

import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.web.WebApplicationInitializer;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.web.AbstractServletConfiguration;

public class TagServletConfiguration extends AbstractServletConfiguration implements Serializable, WebApplicationInitializer {

    private static final long serialVersionUID = -7854589210621698330L;

    public TagServletConfiguration() {
        super("Initializing TAG Servlet");
    }

    @Override
    public Class<? extends SimpleAppConfiguration> getConfigurationClass() {
        return TagAppConfiguration.class;
    }
    
    public String getAppPropertyPrefix() {
        return "tag";
    }

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        if (!TdarConfiguration.getInstance().tagEnabled() &&
                TdarConfiguration.getInstance().tagEmbedded()) {
            return;
        }
        if (StringUtils.isNotBlank(getFailureMessage())) {
            throw new ServletException(getFailureMessage());
        }
        setupContainer(container);
        setupOpenSessionInViewFilter(container);

        // http://stackoverflow.com/questions/16231926/trying-to-create-a-rest-service-using-jersey
        ServletRegistration.Dynamic cxf = container.addServlet("cxf", CXFServlet.class);
        cxf.setLoadOnStartup(1);
        cxf.addMapping("/*");
    }

}
