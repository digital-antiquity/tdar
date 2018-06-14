package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;

public class ReflectionServiceTestCase {

    ReflectionService reflectionService = new ReflectionServiceImpl();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testMarshallSupport() throws NoSuchBeanDefinitionException, ClassNotFoundException {
        ReflectionService reflectionService = new ReflectionServiceImpl();
        String test = "Document";
        Class<? extends Persistable> document = reflectionService.getMatchingClassForSimpleName(test);
        logger.debug("{}", document);
        assertEquals(Document.class, document);
    }

    @Test
    public void testHasLabel() throws ClassNotFoundException {
        Set<Class<? extends HasLabel>> findClassesThatImplement = ReflectionHelper.findClassesThatImplement(HasLabel.class);
        for (Class<? extends HasLabel> cls : findClassesThatImplement) {
            if (cls.isEnum()) {
                for (Object obj : cls.getEnumConstants()) {
                    String label = ((HasLabel) obj).getLabel();
                    assertNotNull(label);
                    if (obj instanceof Localizable) {
                        Localizable l = ((Localizable) obj);
                        logger.debug(l.getLocaleKey() + "==" + label);
                    }
                    logger.trace("cls: {} label: {}", cls, label);
                }
            }

        }
    }

    // @Test
    // public void testEnums() throws ClassNotFoundException {
    // Set<BeanDefinition> findClassesThatImplement = reflectionService.findClassesThatImplement(Persistable.class);
    // for (BeanDefinition def : findClassesThatImplement) {
    // for (Field field : Class.forName(def.getBeanClassName()).getDeclaredFields()) {
    // if (field.getType().isEnum()) {
    // logger.debug(def.getBeanClassName() + "." + field.getName());
    // }
    // }
    // }
    // }

    @Test
    public void testCultureKeywordReferences() {
        Set<Field> set = reflectionService.findFieldsReferencingClass(Resource.class, CultureKeyword.class);
        Assert.assertEquals(1, set.size());
    }

    @Test
    public void testPersonReferences() {
        Set<Field> set = reflectionService.findFieldsReferencingClass(Resource.class, TdarUser.class);
        Assert.assertEquals(3, set.size());
    }

    @Test
    public void testCallFieldSetter() throws SecurityException, NoSuchFieldException {
        Person person = new Person("Bill", "Gates", "billg@microsoft.com");
        person.setDescription("CEO");
        String newDescription = "Philanthropist";
        Field descriptionField = Creator.class.getDeclaredField("description");
        reflectionService.callFieldSetter(person, descriptionField, newDescription);

        Assert.assertEquals(newDescription, person.getDescription());
    }

}
