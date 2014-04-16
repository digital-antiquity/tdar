package org.tdar.core.configuration;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;
import org.tdar.web.SessionData;

@Configuration
@ComponentScan(basePackages = {"org.tdar"})
@EnableTransactionManagement
@EnableAspectJAutoProxy(proxyTargetClass=true)
//@PropertySource(value = {  "classpath:/tdar.properties" }, ignoreResourceNotFound = true)
@ImportResource(value = { "classpath:/spring-local-settings.xml" })
public class TdarAppConfiguration implements Serializable {

    private static final long serialVersionUID = 6038273491995542363L;

    public TdarAppConfiguration() {
        System.err.println("HI");
    }
    
    @Bean(name="sessionFactory")
    public SessionFactory getSessionFactory(@Qualifier("tdarMetadataDataSource") DataSource dataSource)  {
        Properties properties = new Properties();

        LocalSessionFactoryBuilder builder = new LocalSessionFactoryBuilder(dataSource);
        builder.scanPackages(new String[] {"org.tdar.core"});
        builder.addPackages(new String[] {"org.tdar.core"});
        builder.addProperties(properties);
        return builder.buildSessionFactory();
    }

    @Bean
    public JavaMailSender getJavaMailSender(@Value("${mail.smtp.host:localhost}") String hostname) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(hostname);
        return sender;
    }

    @Bean
    //@Value("#{'${my.list.of.strings}'.split(',')}") 
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
    public HibernateTransactionManager transactionManager(@Qualifier("tdarMetadataDataSource") DataSource dataSource) throws PropertyVetoException {
        return new HibernateTransactionManager(getSessionFactory(dataSource));
    }
    
}
