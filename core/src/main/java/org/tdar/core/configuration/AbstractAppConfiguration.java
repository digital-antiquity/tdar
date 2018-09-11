package org.tdar.core.configuration;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
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
import org.tdar.configuration.ConfigurationAssistant;
import org.tdar.configuration.PooledDataSourceWrapper;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.processes.manager.BaseProcessManager;
import org.tdar.core.service.processes.manager.ProcessManager;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@EnableTransactionManagement()
@EnableAspectJAutoProxy(proxyTargetClass = true)

@PropertySource(value = AbstractAppConfiguration.HIBERNATE_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "classpath:" + AbstractAppConfiguration.HIBERNATE_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = AbstractAppConfiguration.TDAR_PROPERTIES, ignoreResourceNotFound = true)
@PropertySource(value = "classpath:" + AbstractAppConfiguration.TDAR_PROPERTIES, ignoreResourceNotFound = true)

@Configuration
public abstract class AbstractAppConfiguration implements Serializable {

    public static final String ORG_TDAR = "org.tdar";
    protected static final String HIBERNATE_PROPERTIES = "hibernate.properties";
    protected static final String TDAR_PROPERTIES = "tdar.properties";
    private static final long serialVersionUID = 2190713147269025044L;
    public transient Logger logger = LoggerFactory.getLogger(getClass());
    public static final transient Logger staticLogger = LoggerFactory.getLogger(AbstractAppConfiguration.class);

    public AbstractAppConfiguration() {
        logger.debug("Initializing Simple Application Context");

        /*
         * tDAR primarily uses the SLF4j fascade for all logging of the application, but then filters that through to log4j2 on the backend.
         * This does produce some complexities with Hibernate. These issues are related to the following:
         * * hibernate tries to auto-discover the logging source
         * * some versions of jboss-logging will introduce their own custom versions of log4j
         * * commons-logging and other logging options can also produce conflicts.
         */
        System.setProperty("org.jboss.logging.provider", "slf4j");
        System.setProperty("pdfbox.fontcache", System.getProperty("java.io.tmpdir") + File.separatorChar);
        System.setProperty("java.awt.headless", "true");
        ImageIO.scanForPlugins();
    }

    @Autowired
    protected Environment env;

    // @Autowired
    public void setEnvironment(Environment env) {
        logger.debug(" active profiles: {}", Arrays.asList(env.getActiveProfiles()));
        logger.debug("default profiles: {}", Arrays.asList(env.getDefaultProfiles()));
    }

    @Bean(name = "tdarMetadataDataSource")
    public DataSource tdarMetadataDataSource() {
        try {
            return new PooledDataSourceWrapper(getMetadataDatabaseName(), env).getDataSource();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMetadataDatabaseName() {
        return "tdarmetadata";
    }

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(@Qualifier("tdarMetadataDataSource") DataSource dataSource)
            throws FileNotFoundException, IOException, URISyntaxException {
        Properties properties = new Properties();

        properties.load(ConfigurationAssistant.toInputStream(HIBERNATE_PROPERTIES));

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
        builder.scanPackages(new String[] { getHibernatePackageScan() });
        builder.addProperties(properties);
        // SessionBuilder sessionBuilder = builder.buildSessionFactory().withOptions().eventListeners(new FilestoreLoggingSessionEventListener());
        return builder.buildSessionFactory();
    }

    public String getHibernatePackageScan() {
        return ORG_TDAR;
    }

    public boolean disableHibernateSearch() {
        return true;
    }

    @Bean(name = "obfuscationEnabled")
    public Boolean isObfuscationEnabled() {
        return !TdarConfiguration.getInstance().obfuscationInterceptorDisabled();
    }

    @Bean(name = "mailSender")
    public JavaMailSender getJavaMailSender() {
        String hostname = env.getProperty("mail.smtp.host", "localhost");
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


}
