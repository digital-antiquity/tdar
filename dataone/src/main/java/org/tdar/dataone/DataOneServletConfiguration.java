package org.tdar.dataone;

import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.WebApplicationInitializer;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.web.AbstractServletConfiguration;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class DataOneServletConfiguration extends AbstractServletConfiguration implements Serializable, WebApplicationInitializer {

	private static final long serialVersionUID = -7854589210621698330L;


    public DataOneServletConfiguration() {
        super("Initializing dataOne Servlet");
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
        ServletRegistration.Dynamic dataOne = container.addServlet("dataone", SpringServlet.class);
        dataOne.setLoadOnStartup(1);
        dataOne.setInitParameter("jersey.config.server.provider.packages", "org.tdar.dataone.server");
        dataOne.addMapping("/dataone/*");
    }

}
