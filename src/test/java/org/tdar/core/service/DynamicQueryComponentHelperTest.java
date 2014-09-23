package org.tdar.core.service;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.bean.resource.Project;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.builder.DynamicQueryComponent;
import org.tdar.search.query.builder.DynamicQueryComponentHelper;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;

public class DynamicQueryComponentHelperTest {

    public Logger logger = Logger.getLogger(getClass());
    HashSet<DynamicQueryComponent> createFields = new HashSet<DynamicQueryComponent>();

    public DynamicQueryComponentHelperTest() {
        QueryBuilder qb = new ResourceQueryBuilder();
        for (Class<?> cls : qb.getClasses()) {
            createFields.addAll(DynamicQueryComponentHelper.createFields(cls, ""));
        }
    }

    @Test
    public void testMultiChildFieldAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("resourceCreators.creator.name")) {
                found = true;
            }
            logger.trace(dqc.getLabel());
        }
        assertTrue("Child @Field annotation not found on: creatorPersons.person.institution.acronym", found);
    }

    @Test
    public void testEnumAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase(QueryFieldNames.INTEGRATABLE)) {
                found = true;
            }
            if (dqc.getLabel().toLowerCase().endsWith("etype") || dqc.getLabel().toLowerCase().contains("status") || dqc.getLabel().contains("integratable")) {
                Assert.assertNotNull(dqc.getAnalyzer());
            }
        }
        assertTrue("found integratable annotation", found);
    }

    @Test
    public void testFieldsAnnotations() {
        boolean found = false;
        boolean found2 = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("resourceProviderInstitution.name_auto")) {
                found = true;
            }
            if (dqc.getLabel().equalsIgnoreCase("resourceProviderInstitution.name")) {
                found2 = true;
            }
        }
        assertTrue("Child @Field annotation not found on: resourceProviderInstitution.name_auto", found);
        assertTrue("Child @Field annotation not found on: resourceProviderInstitution.name", found2);
    }

    @Test
    public void testSimpleChildFieldAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("activeCoverageDates.startDate")) {
                found = true;
            }
        }
        assertTrue("Child @Field annotation not found on: CoverageDate.startDate", found);
    }

    @Test
    public void testEmbeddedChildMethodAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("activeOtherKeywords.label")) {
                found = true;
            }
        }
        assertTrue("Child @Field annotation not found on: activeOtherKeywords.label", found);
    }

    @Test
    public void testEmbeddedPrefixedChildAnnotation() {
        boolean found = false;
        HashSet<DynamicQueryComponent> createFields2 = DynamicQueryComponentHelper.createFields(Project.class, "");
        for (DynamicQueryComponent dqc : createFields2) {
            logger.trace(dqc.getLabel() + "{}" + dqc.getParent());
            if (dqc.getLabel().contains("informationResources.activeOtherKeywords.label")) {
                found = true;
            }
        }
        assertTrue("Child @Field annotation not found on: activeOtherKeywords.label", found);
    }

    @Test
    public void testIdAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("updatedBy.id")) {
                found = true;
            }
        }
        assertTrue("@Id annotation not found on: updatedBy.id", found);
    }

    @Test
    public void testSimpleFieldAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("id")) {
                found = true;
            }
        }
        assertTrue("@Field annotation not found on: id", found);
    }

    @Test
    public void testSimpleMethodAnnotation() {
        boolean found = false;
        for (DynamicQueryComponent dqc : createFields) {
            if (dqc.getLabel().equalsIgnoreCase("dateCreated")) {
                found = true;
            }
        }
        assertTrue("@Field annotation not found on: dateCreated", found);
    }

}
