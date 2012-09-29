package org.tdar.core.service;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.search.query.DynamicQueryComponent;

public class SearchServiceTest {

    public Logger logger = Logger.getLogger(getClass());
    HashSet<DynamicQueryComponent> createFields;

    public SearchServiceTest() {
        createFields = SearchService.createFields(InformationResource.class, "");
    }

    @Test
    public void testMultiChildFieldAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("resourceCreators.creator.name"))
                found = true;
            logger.trace(dqc.getLabel());
        }
        assertTrue("Child @Field annotation not found on: creatorPersons.person.institution.acronym", found);
    }

    @Test
    public void testFieldsAnnotations() {
        boolean found = false;
        boolean found2 = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("resourceProviderInstitution.name_auto"))
                found = true;
            if (dqc.getLabel().equalsIgnoreCase("resourceProviderInstitution.name"))
                found2 = true;
        }
        assertTrue("Child @Field annotation not found on: resourceProviderInstitution.name_auto", found);
        assertTrue("Child @Field annotation not found on: resourceProviderInstitution.name", found2);
    }

    @Test
    public void testSimpleChildFieldAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("activeCoverageDates.startDate"))
                found = true;
        }
        assertTrue("Child @Field annotation not found on: CoverageDate.startDate", found);
    }

    @Test
    public void testEmbeddedChildMethodAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("activeOtherKeywords.label"))
                found = true;
        }
        assertTrue("Child @Field annotation not found on: activeOtherKeywords.label", found);
    }

    @Test
    public void testIdAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("updatedBy.id"))
                found = true;
        }
        assertTrue("@Id annotation not found on: updatedBy.id", found);
    }

    @Test
    public void testSimpleFieldAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("id"))
                found = true;
        }
        assertTrue("@Field annotation not found on: id", found);
    }

    @Test
    public void testSimpleMethodAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("dateCreated"))
                found = true;
        }
        assertTrue("@Field annotation not found on: dateCreated", found);
    }

}
