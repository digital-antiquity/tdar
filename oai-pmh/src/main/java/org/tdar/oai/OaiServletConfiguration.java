package org.tdar.oai;

import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.WebApplicationInitializer;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.web.AbstractServletConfiguration;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class OaiServletConfiguration extends AbstractServletConfiguration implements Serializable, WebApplicationInitializer {

	private static final long serialVersionUID = -4414884961889682778L;

	public OaiServletConfiguration() {
        super("Initializing OAI Servlet");
    }

    @Override
	public Class<? extends SimpleAppConfiguration> getConfigurationClass() {
		return SimpleAppConfiguration.class;
	}

    @Override
    public void onStartup(ServletContext container) throws ServletException {
        if (StringUtils.isNotBlank(getFailureMessage())) {
            throw new ServletException(getFailureMessage());
        }

        setupContainer(container);
		setupOpenSessionInViewFilter(container);

        // http://stackoverflow.com/questions/16231926/trying-to-create-a-rest-service-using-jersey
        ServletRegistration.Dynamic oaiPmh = container.addServlet("oaipmh", SpringServlet.class);
        oaiPmh.setLoadOnStartup(1);
        oaiPmh.setInitParameter("com.sun.jersey.config.property.packages", "org.tdar.oai.server");
        oaiPmh.addMapping("/oai-pmh/*");
    }

}
