package org.tdar.core.configuration;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.springframework.web.context.WebApplicationContext;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.processes.manager.BaseProcessManager;
import org.tdar.core.service.processes.manager.ProcessManager;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@EnableTransactionManagement()
@EnableAspectJAutoProxy(proxyTargetClass = true)
// NOTE: this exclude filter is to ensure that we don't instantiate every @Configuration by default. @Configuration is a subclass of @Component, so the
// autowiring happens by default w/o this
@ComponentScan(basePackages = { "org.tdar" },
        excludeFilters = {
                @Filter(type = FilterType.ASSIGNABLE_TYPE,
                        value = {
                                SimpleAppConfiguration.class
                        })
        })
@PropertySource(value = SimpleAppConfiguration.HIBERNATE_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "classpath:" + SimpleAppConfiguration.HIBERNATE_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "file://${TDAR_CONFIG_PATH}/" + SimpleAppConfiguration.HIBERNATE_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = SimpleAppConfiguration.TDAR_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "classpath:" + SimpleAppConfiguration.TDAR_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "file://${TDAR_CONFIG_PATH}/" + SimpleAppConfiguration.TDAR_PROPERTIES, ignoreResourceNotFound = true)

@Configuration
public abstract class SimpleAppConfiguration implements Serializable {

    protected static final String HIBERNATE_PROPERTIES = "hibernate.properties";
    protected static final String TDAR_PROPERTIES = "tdar.properties";
    private static final long serialVersionUID = 2190713147269025044L;
    public transient Logger logger = LoggerFactory.getLogger(getClass());
    public static transient Logger staticLogger = LoggerFactory.getLogger(SimpleAppConfiguration.class);

    public SimpleAppConfiguration() {
        logger.debug("Initializing Simple Application Context");

        /*
         * tDAR primarily uses the SLF4j fascade for all logging of the application, but then filters that through to log4j2 on the backend.
         * This does produce some complexities with Hibernate. These issues are related to the following:
         * * hibernate tries to auto-discover the logging source
         * * some versions of jboss-logging will introduce their own custom versions of log4j
         * * commons-logging and other logging options can also produce conflicts.
         */
        System.setProperty("org.jboss.logging.provider", "slf4j");
        System.setProperty("pdfbox.fontcache",System.getProperty("java.io.tmpdir") + File.separatorChar);
        System.setProperty("java.awt.headless", "true");
        ImageIO.scanForPlugins();
    }

    @Autowired
    protected Environment env;
//    private SessionFactory buildSessionFactory;

    @Bean(name = "tdarMetadataDataSource")
    public DataSource tdarMetadataDataSource() {
        try {
            return configureDataSource("tdarmetadata");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(@Qualifier("tdarMetadataDataSource") DataSource dataSource)
            throws FileNotFoundException, IOException, URISyntaxException {
        Properties properties = new Properties();

        properties.load(ConfigurationAssistant.toInputStream(HIBERNATE_PROPERTIES));

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
        builder.scanPackages(new String[] { "org.tdar" });
        builder.addProperties(properties);
//        SessionBuilder sessionBuilder = builder.buildSessionFactory().withOptions().eventListeners(new FilestoreLoggingSessionEventListener());
        return builder.buildSessionFactory();
    }

    public boolean disableHibernateSearch() {
        return true;
    }

    @Bean(name = "mailSender")
    public JavaMailSender getJavaMailSender() {
        String hostname = env.getProperty("mail.smtp.host","localhost");
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(hostname);
        return sender;
    }

    @Bean(name = "sessionData")
    @Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public SessionData getSessionData() {
        return new SessionData();
    }

    @Bean
    public FreeMarkerConfigurationFactoryBean getFreemarkerMailConfiguration() {
        FreeMarkerConfigurationFactoryBean freemarkerConfig = new FreeMarkerConfigurationFactoryBean();
        List<String> templateLoaderPaths = getFreemarkerPaths();
        freemarkerConfig.setTemplateLoaderPaths(templateLoaderPaths.toArray(new String[0]));
        return freemarkerConfig;
    }

    protected List<String> getFreemarkerPaths() {
        List<String> templateLoaderPaths = new ArrayList<>();
        templateLoaderPaths.add("classpath:/freemarker-templates");
        templateLoaderPaths.add("file:/WEB-INF/freemarker-templates");
        templateLoaderPaths.add("classpath:/WEB-INF/content");
        templateLoaderPaths.add("classpath:src/main/webapp");
        templateLoaderPaths.add("file:src/main/webapp");
        templateLoaderPaths.add("classpath:/freemarker-templates-test");
        templateLoaderPaths.add("classpath:/templates");
        templateLoaderPaths.add("file:/templates");
        return templateLoaderPaths;
    }

    @Bean
    @Primary
    public HibernateTransactionManager transactionManager(@Qualifier("tdarMetadataDataSource") DataSource dataSource)
            throws PropertyVetoException, FileNotFoundException, IOException, URISyntaxException {
        HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(getSessionFactory(dataSource));
        return hibernateTransactionManager;
    }

    @Bean(name = "processManager")
    public ProcessManager processManager() {
        return new BaseProcessManager();
    }

    /**
     * Configure the dataSource by using the dataSource prefix (tdardata, tdarmetadata, tdargis); for jpa properties, the defaults of javax.persistance.jdbc.
     * can be swapped in.
     * 
     * @param prefix
     * @return
     * @throws PropertyVetoException
     */
    protected DataSource configureDataSource(String prefix) throws PropertyVetoException {
        ComboPooledDataSource ds = new ComboPooledDataSource();

        String driver_ = ".persistence.jdbc.driver";
        String url_ = ".persistence.jdbc.url";
        String user_ = ".persistence.jdbc.user";
        String password_ = ".persistence.jdbc.password";
        ds.setDriverClass(getProperty(prefix, driver_));
        setupAndLogJdbcConnectionString(prefix, ds, url_);
        ds.setUser(getProperty(prefix, user_));
        ds.setPassword(getProperty(prefix, password_));

        ds.setAcquireIncrement(env.getProperty(prefix + ".acquireIncrement", Integer.class, 5));
        ds.setPreferredTestQuery(env.getProperty(prefix + ".preferredTestQuery", String.class, "select 1"));
        ds.setMaxIdleTime(env.getProperty(prefix + ".maxIdleTime", Integer.class, 600));
        ds.setIdleConnectionTestPeriod(env.getProperty(prefix + ".idleConnectionTestPeriod", Integer.class, 300));
        ds.setMaxStatements(env.getProperty(prefix + ".maxStatements", Integer.class, 100));
        ds.setTestConnectionOnCheckin(env.getProperty(prefix + ".testConnectionOnCheckin", Boolean.class, true));
        ds.setMaxPoolSize(getChainedOptionalProperty(prefix, ".maxConnections", 10));
        ds.setMinPoolSize(getChainedOptionalProperty(prefix ,".minConnections", 1));
        return ds;
    }

	private void setupAndLogJdbcConnectionString(String prefix, ComboPooledDataSource ds, String url_) {
		String property = getProperty(prefix, url_);
        String prefix_ = "tdar";
        String appPrefix = System.getProperty("appPrefix");
		if (StringUtils.isNotBlank(appPrefix)) {
        	prefix_ = appPrefix;
        }
        if (property.contains("?")) {
        	property += "&ApplicationName="+ prefix_;
        } else {
        	property += "?ApplicationName="+ prefix_;
        }
		ds.setJdbcUrl(property);
        logger.debug(prefix_+ " JDBC Connection ("+prefix+"):"  + property);
	}

    private int getChainedOptionalProperty(String prefix, String key, Integer deflt) {
        String appPrefix = System.getProperty("appPrefix");
        String prefix_ = prefix;
        if (StringUtils.isNotBlank(appPrefix)) {
            prefix_ = appPrefix + "." + prefix;
        }
        
        Integer val = env.getProperty(prefix_ + key, Integer.class);
        if (val != null) {
            logger.debug(prefix_ + key + ": " + val);
            return val;
        }
        
        return env.getProperty(prefix + key, Integer.class, deflt);
    }

    /**
     * Allow for the override of the default connection properties (good for postGIS)
     * 
     * @param prefix
     * @param val_
     * @return
     */
    private String getProperty(String prefix, String val_) {
        String val = env.getProperty(prefix + val_);
        if (val == null) {
            val = env.getRequiredProperty("javax" + val_);
        } else {
            logger.debug("{} --> {}" , prefix + val_, val );
        }
        return val;
    }

}
