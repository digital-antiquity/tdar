package org.tdar.core.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Document;

public class ReflectionServiceTestCase {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testMarshallSupport() throws NoSuchBeanDefinitionException, ClassNotFoundException {
        ReflectionService reflectionService = new ReflectionService();
        String test = "Document";
        Class<? extends Persistable> document = reflectionService.getMatchingClassForSimpleName(test);
        logger.debug("{}", document);
        assertEquals(Document.class, document);
    }
}
