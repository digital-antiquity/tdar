package org.tdar.core.configuration;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.processes.manager.BaseProcessManager;
import org.tdar.core.service.processes.manager.ProcessManager;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@EnableTransactionManagement()
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = { "org.tdar" }, excludeFilters = {})
@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration
public class SimpleAppConfiguration implements Serializable {

    private static final String HIBERNATE_PROPERTIES = "hibernate.properties";
    private static final long serialVersionUID = 2190713147269025044L;
    public transient Logger logger = LoggerFactory.getLogger(getClass());
    public static transient Logger staticLogger = LoggerFactory.getLogger(SimpleAppConfiguration.class);

    public SimpleAppConfiguration() {
        /*
         * tDAR primarily uses the SLF4j fascade for all logging of the application, but then filters that through to log4j2 on the backend.
         * This does produce some complexities with Hibernate. These issues are related to the following:
         * * hibernate tries to auto-discover the logging source
         * * some versions of jboss-logging will introduce their own custom versions of log4j
         * * commons-logging and other logging options can also produce conflicts.
         */
        System.setProperty("org.jboss.logging.provider", "slf4j");

    }
    
    @Autowired
    private Environment env;

/*    @Bean(name = "tdarMetadataDataSource")
    public DataSource tdarMetadataDataSource() {
        logger.debug(env.toString());
        logger.debug(env.getProperty("javax.persistence.jdbc.driver"));
        try {
            ComboPooledDataSource ds = new ComboPooledDataSource();
            ds.setDriverClass(env.getRequiredProperty("javax.persistence.jdbc.driver"));
            ds.setJdbcUrl(env.getRequiredProperty("javax.persistence.jdbc.url"));
            ds.setUser(env.getRequiredProperty("javax.persistence.jdbc.user"));
            ds.setPassword(env.getRequiredProperty("javax.persistence.jdbc.password"));
            ds.setAcquireIncrement(5);
            ds.setIdleConnectionTestPeriod(60);
            ds.setMaxPoolSize(env.getRequiredProperty("tdarmetadata.maxConnections", Integer.class));
            ds.setMaxStatements(50);
            ds.setMinPoolSize(env.getRequiredProperty("tdarmetadata.minConnections", Integer.class));
            return ds;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

*/

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(@Qualifier("tdarMetadataDataSource") DataSource dataSource) throws FileNotFoundException, IOException, URISyntaxException {
        Properties properties = new Properties();
        
        URL resource = getClass().getClassLoader().getResource(HIBERNATE_PROPERTIES);
        logger.trace("{}", resource);
        File file = new File(resource.toURI());
        String dir = System.getenv(ConfigurationAssistant.DEFAULT_CONFIG_PATH);
        if (dir != null) {
            file = new File(dir, HIBERNATE_PROPERTIES);
        }
        properties.load(new FileReader(file));

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
        builder.scanPackages(new String[] { "org.tdar" });
        builder.addProperties(properties);
        if (disableHibernateSearch()) {
            builder.setProperty("hibernate.search.autoregister_listeners", "false");
            builder.setProperty("hibernate.search.indexing_strategy", "manual");
        }
        return builder.buildSessionFactory();
    }

    public boolean disableHibernateSearch() {
        return true;
    }

    @Bean(name = "mailSender")
    public JavaMailSender getJavaMailSender(@Value("${mail.smtp.host:localhost}") String hostname) {
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
    @Primary
    public HibernateTransactionManager transactionManager(@Qualifier("tdarMetadataDataSource") DataSource dataSource) throws PropertyVetoException, FileNotFoundException, IOException, URISyntaxException {
        HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager(getSessionFactory(dataSource));
        return hibernateTransactionManager;
    }

    @Bean(name = "processManager")
    public ProcessManager processManager() {
        return new BaseProcessManager();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer placeholder = new PropertySourcesPlaceholderConfigurer();
        String CONFIG_DIR = System.getenv(ConfigurationAssistant.DEFAULT_CONFIG_PATH);
        staticLogger.debug("USING CONFIG PATH:" + CONFIG_DIR);
        List<Resource> resources = new ArrayList<>();
        String[] propertyFiles = { HIBERNATE_PROPERTIES };
        for (String propertyFile : propertyFiles) {
            if (CONFIG_DIR == null) {
                ClassPathResource resource = new ClassPathResource(propertyFile);
                resources.add(resource);
            } else {
                FileSystemResource resource = new FileSystemResource(new File(CONFIG_DIR, propertyFile));
                resources.add(resource);
            }
        }
        placeholder.setLocations(resources.toArray(new Resource[0]));
        return placeholder;
    }
}
