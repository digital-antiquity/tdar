package org.tdar.core.configuration;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.tdar.web.SessionData;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * <!-- tDAR data sources -->
 * <import resource='spring-data-sources.xml' />
 * <!-- local settings, connection pooling and crowd or ldap auth providers -->
 * <import resource="spring-local-settings.xml" />
 * <!-- hibernate session factory configuration -->
 * <import resource='spring-hibernate.xml' />
 * 
 */
@Configuration
@ComponentScan(basePackages = {"org.tdar"})
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass=true)
@PropertySource(value = { "classpath:/hibernate.properties", "classpath:/crowd.properties", "classpath:/tdar.properties" }, ignoreResourceNotFound = true)
@ImportResource(value = { "classpath:/spring-data-sources.xml", "classpath:/spring-local-settings.xml" }) //"classpath://spring-hibernate.xml", 
public class TdarAppConfiguration implements Serializable {

    private static final long serialVersionUID = 6038273491995542363L;

    @Value("${javax.persistence.jdbc.driver}")
    private String driverClassName;
    @Value("${javax.persistence.jdbc.url}")
    private String url;
    @Value("${javax.persistence.jdbc.user}")
    private String username;
    @Value("${javax.persistence.jdbc.password}")
    private String password;

    @Value("${hibernate.dialect}")
    private String hibernateDialect;
    @Value("${hibernate.show_sql}")
    private String hibernateShowSql;
    @Value("${hibernate.hbm2ddl.auto}")
    private String hibernateHbm2ddlAuto;

    @Bean
    public DataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource ds = new ComboPooledDataSource();
        ds.setDriverClass(driverClassName);
        ds.setJdbcUrl(url);
        ds.setUser(username);
        ds.setPassword(password);
//        ds.setMinPoolSize(minPoolSize);
//        ds.setMaxPoolSize(maxPoolSize);
//        ds.setMaxIdleTime(maxIdleTime);
//        ds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
//        ds.setPreferredTestQuery(preferredTestQuery);
//        ds.setTestConnectionOnCheckin(testConnectionOnCheckin);
        return ds;
    }

    @Bean(name="sessionFactory")
    public SessionFactory getSessionFactory() throws PropertyVetoException {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", hibernateDialect);
        properties.put("hibernate.show_sql", hibernateShowSql);
        properties.put("current_session_context_class", "thread");

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource());
        builder.scanPackages(new String[] {"org.tdar.core"});
        builder.addPackages(new String[] {"org.tdar.core"});
        builder.addProperties(properties);
        return builder.buildSessionFactory();
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("${mail.smtp.host:localhost}");
        return sender;
    }

    @Bean
    public FreeMarkerConfigurationFactoryBean getFreemarkerMailConfiguration() {
        FreeMarkerConfigurationFactoryBean freemarkerConfig = new FreeMarkerConfigurationFactoryBean();
        List<String> templateLoaderPaths = new ArrayList<>();
        templateLoaderPaths.add("classpath:/freemarker-templates");
        templateLoaderPaths.add("file:/WEB-INF/freemarker-templates");
        templateLoaderPaths.add("classpath:/WEB-INF/content");
        templateLoaderPaths.add("classpath:src/main/webapp");
        templateLoaderPaths.add("file:src/main/webapp");
        templateLoaderPaths.add("classpath:/freemarker-templates-test");
        templateLoaderPaths.add("classpath:/templates");
        templateLoaderPaths.add("file:/templates");
        freemarkerConfig.setTemplateLoaderPaths(templateLoaderPaths.toArray(new String[0]));
        return freemarkerConfig;
    }

    @Bean(name="sessionData")
    @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public SessionData getSessionData() {
        return new SessionData();
    }

    @Bean
    public HibernateTransactionManager transactionManager() throws PropertyVetoException {
        return new HibernateTransactionManager(getSessionFactory());
    }
}
