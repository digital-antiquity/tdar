package org.tdar.core.service;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Document;

public class ReflectionServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    ReflectionService reflectionService;

    @Test
    public void testMarshallSupport() throws NoSuchBeanDefinitionException, ClassNotFoundException {
        String test = "Document";
        Class<Persistable> document = reflectionService.getMatchingClassForSimpleName(test);
        logger.debug("{}", document);
        assertEquals(Document.class,document);
    }
}
