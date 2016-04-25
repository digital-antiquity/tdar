package org.tdar.odata;

import java.io.Serializable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.WebApplicationInitializer;
import org.tdar.core.configuration.IntegrationAppConfiguration;
import org.tdar.core.configuration.SimpleAppConfiguration;
import org.tdar.web.AbstractServletConfiguration;

import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

public class OdataServletConfiguration extends AbstractServletConfiguration
		implements Serializable, WebApplicationInitializer {


	private static final long serialVersionUID = 4546769134752615151L;

	public OdataServletConfiguration() {
		super("Initializing tDAR Odata Servlet");
	}
	
    @Override
	public Class<? extends SimpleAppConfiguration> getConfigurationClass() {
		return IntegrationAppConfiguration.class;
	}


	@Override
	public void onStartup(ServletContext container) throws ServletException {
		if (StringUtils.isNotBlank(getFailureMessage())) {
			throw new ServletException(getFailureMessage());
		}
		setupContainer(container);
		configureOdata(container);
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

}
