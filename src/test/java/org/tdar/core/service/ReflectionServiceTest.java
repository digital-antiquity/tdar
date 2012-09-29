package org.tdar.core.service;

import java.lang.reflect.Field;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.resource.Resource;

public class ReflectionServiceTest {

    ReflectionService reflectionService = new ReflectionService();

    @Test
    public void testCultureKeywordReferences() {
        Set<Field> set = reflectionService.findFieldsReferencingClass(Resource.class, CultureKeyword.class);
        Assert.assertEquals(1, set.size());
    }

    @Test
    public void testPersonReferences() {
        Set<Field> set = reflectionService.findFieldsReferencingClass(Resource.class, Person.class);
        Assert.assertEquals(2, set.size());
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
