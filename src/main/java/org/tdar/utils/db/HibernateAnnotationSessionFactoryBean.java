package org.tdar.utils.db;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * $Id$
 * FIXME: replace with Spring
 * org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean?
 * @author Adam Brin
 * @version $Revision$
 */
public class HibernateAnnotationSessionFactoryBean extends LocalSessionFactoryBean {
  private Logger logger = Logger.getLogger(getClass());
  private List<String> annotatedClasses_ = new ArrayList<String>();

  public void setAnnotatedClasses(List<String> classes) {
    annotatedClasses_ = classes;
  }

  public void populateAnnotatedClasses() {
    ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
    scanner.addIncludeFilter(new AnnotationTypeFilter(NamedQuery.class));
    String basePackage = "org/tdar/";
    for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
      annotatedClasses_.add(bd.getBeanClassName());
    }
  }

  @Override
  protected void postProcessConfiguration(Configuration config) throws HibernateException {
    super.postProcessConfiguration(config);
    if (annotatedClasses_ == null) {
      logger.info("No annotated classes to register with Hibernate.");
      return;
    }

    populateAnnotatedClasses();
    config.addPackage("org.tdar.core.dao");

    for (String className : annotatedClasses_) {
      try {
        Class<?> clazz = config.getClass().getClassLoader().loadClass(className);
        config.addAnnotatedClass(clazz);

        logger.trace("Class " + className + " added to Hibernate config.");
      } catch (MappingException e) {
        throw new ApplicationContextException("Unable to register class " + className, e);
      } catch (ClassNotFoundException e) {
        throw new ApplicationContextException("Unable to register class " + className, e);
      }
    }
  }
}
