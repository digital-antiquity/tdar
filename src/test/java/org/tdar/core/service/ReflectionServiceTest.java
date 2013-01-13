package org.tdar.core.service;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.util.bulkUpload.CellMetadata;

public class ReflectionServiceTest {

    ReflectionService reflectionService = new ReflectionService();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void testBulkUpload() {
        LinkedHashSet<CellMetadata> fields = reflectionService.findBulkAnnotationsOnClass(Document.class);
        for (CellMetadata field : fields) {
            logger.info(field.getName() + "|" + field.getDisplayName() + "|" + field.getMappedClass());
        }
    }

    @Test
    public void testCultureKeywordReferences() {
        Set<Field> set = reflectionService.findFieldsReferencingClass(Resource.class, CultureKeyword.class);
        Assert.assertEquals(1, set.size());
    }

    @Test
    public void testPersonReferences() {
        Set<Field> set = reflectionService.findFieldsReferencingClass(Resource.class, Person.class);
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
