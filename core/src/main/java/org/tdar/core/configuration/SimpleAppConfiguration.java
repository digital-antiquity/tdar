package org.tdar.core.configuration;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@EnableTransactionManagement()
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = { "org.tdar" }, excludeFilters = {})
@ImportResource(value = { "classpath:spring-local-settings.xml" })
@Configuration
public class SimpleAppConfiguration implements Serializable {

    private static final long serialVersionUID = 2190713147269025044L;
    public transient Logger logger = LoggerFactory.getLogger(getClass());

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

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(@Qualifier("tdarMetadataDataSource") DataSource dataSource) {
        Properties properties = new Properties();

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
        builder.scanPackages(new String[] { "org.tdar" });
        builder.addProperties(properties);
        return builder.buildSessionFactory();
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
    public HibernateTransactionManager transactionManager(@Qualifier("tdarMetadataDataSource") DataSource dataSource) throws PropertyVetoException {
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
        String CONFIG_DIR = System.getenv("TDAR_CONFIG_PATH");
        List<Resource> resources = new ArrayList<>();
        String[] propertyFiles = { "hibernate.properties" };
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
