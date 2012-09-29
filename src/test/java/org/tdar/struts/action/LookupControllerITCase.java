package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.JsonModel;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceAnnotationType;
import org.tdar.core.bean.resource.ResourceType;

public class LookupControllerITCase extends AbstractIntegrationTestCase {

    @Autowired
    private LookupController controller;
    private Logger log = Logger.getLogger(getClass());

    @Before
    public void initController() {
        controller = generateNewController(LookupController.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    public void testPersonLookupWithNoResults() {
        searchIndexService.indexAll(Person.class);
        controller.setFirstName("bobby");
        String result = controller.lookupPerson();
        assertEquals("operation successful", result, LookupController.SUCCESS);
        List<JsonModel> people = controller.getJsonResults();
        assertEquals("person list should be empty", people.size(), 0);
    }

    @Test
    public void testPersonLookupWithOneResult() {
        searchIndexService.indexAll(Person.class);
        controller.setEmail("test@tdar.org");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<JsonModel> people = controller.getJsonResults();
        assertEquals("person list should have exactly one item", people.size(), 1);
    }

    @Test
    // we should properly escape input
    public void testPersonWithInvalidInput() {
        searchIndexService.indexAll(Person.class);
        // FIXME: need more invalid input examples than just paren
        controller.setLastName("(    ");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<JsonModel> people = controller.getJsonResults();
    }

    @Test
    public void testRegisteredPersonLookupWithOneResult() {
        searchIndexService.indexAll(Person.class);
        controller.setFirstName("Keit");
        controller.setRegistered("true");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<JsonModel> people = controller.getJsonResults();
        assertEquals("person list should have exactly one item", 2, people.size());
    }

    @Test
    public void testPersonWithInstitution() {
        searchIndexService.indexAll(Person.class);
        controller.setEmail("test@tdar.org");
        controller.setInstitution("University of TEST");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<JsonModel> people = controller.getJsonResults();
        assertEquals("person list should have exactly one item", 1, people.size());
    }

    @Test
    public void testInstitutionAlone() {
        searchIndexService.indexAll(Person.class);
        // FIXME: should not need to be quoted
        controller.setInstitution("TQF");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<JsonModel> people = controller.getJsonResults();
        assertEquals("person list should have exactly one item", 1, people.size());
    }

    @Test
    public void testValidInstitutionWithSpace() {
        searchIndexService.indexAll(Person.class);
        // FIXME: should not need to be quoted
        controller.setInstitution("University of");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<JsonModel> people = controller.getJsonResults();
        logger.info(people);
        assertEquals("person list should have exactly one item", 2, people.size());
    }

    @Test
    public void testInstitutionEmpty() {
        searchIndexService.indexAll(Person.class);
        // FIXME: should not need to be quoted
        controller.setInstitution("University ABCD");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<JsonModel> people = controller.getJsonResults();
        assertEquals("person list should have exactly one item", 0, people.size());
    }

    @Test
    public void testPersonLookupWithSeveralResults() {
        searchIndexService.indexAll(Person.class);
        // based on our test data this should return at least two records (john doe and jane doe)
        String partialLastName = "Mann";
        controller.setLastName(partialLastName);
        controller.lookupPerson();
        List<JsonModel> people = controller.getJsonResults();
        if (people != null) {
            this.log.debug("people size:" + people.size() + "value:" + people);
        }
        assertTrue("at least two people in search results", people.size() >= 2);
    }

    @Test
    public void testInstitutionLookupWithNoResults() {
        searchIndexService.indexAll(Institution.class);
        controller.setInstitution("fdaksfddfde");
        controller.lookupInstitution();
        List<JsonModel> institutions = controller.getJsonResults();
        assertEquals("person list should be empty", institutions.size(), 0);
    }

    @Test
    public void testInstitutionLookupWithOneResult() {
        searchIndexService.indexAll(Institution.class);
        controller.setInstitution("tfqa");
        controller.lookupInstitution();
        List<JsonModel> institutions = controller.getJsonResults();
        assertTrue("only one result expected", institutions.size() == 1);
    }

    // given test script, searching 'digital' should return multiple results that start with 'Digital Antiquity'
    @Test
    public void testInstitutionLookupWithMultiple() {
        searchIndexService.indexAll(Institution.class);
        controller.setInstitution("University");
        controller.lookupInstitution();
        List<JsonModel> institutions = controller.getJsonResults();
        assertTrue("more than one result expected", institutions.size() > 1);
    }

    // searching for string witn blanks should yield zero results.
    @Test
    public void testInstitutionLookupWithBlanks() {
        searchIndexService.indexAll(Institution.class);
        String blanks = "    ";
        controller.setInstitution(blanks);
        controller.lookupInstitution();
        List<JsonModel> results = controller.getJsonResults();
        Assert.assertEquals("expecting zero results", 0, results.size());
    }

    @Test
    public void testResourceLookupByType() {
        searchIndexService.indexAll(Resource.class);
        // get back all documents
        controller.setResourceType(ResourceType.DOCUMENT.toString());
        controller.lookupResource();
        List<JsonModel> resources = controller.getJsonResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByProjectId() {
        searchIndexService.indexAll(Resource.class);
        controller.setProjectId(3073L);
        controller.lookupResource();
        List<JsonModel> resources = controller.getJsonResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testKeywordLookup() {
        searchIndexService.indexAll(GeographicKeyword.class, CultureKeyword.class);
        controller.setKeywordType("culturekeyword");
        controller.setTerm("Folsom");
        controller.lookupKeyword();
        List<JsonModel> resources = controller.getJsonResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testAnnotationLookup() {
        ResourceAnnotationKey key = new ResourceAnnotationKey();
        key.setKey("ISSN");
        key.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key);
        ResourceAnnotationKey key2 = new ResourceAnnotationKey();
        key2.setKey("ISBN");
        key2.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key2);

        searchIndexService.indexAll(ResourceAnnotationKey.class);
        controller.setTerm("IS");
        controller.lookupAnnotationKey();
        List<JsonModel> resources = controller.getJsonResults();
        assertTrue("at least one document", resources.size() == 2);

        // FIXME: not properly simulating new page request
        controller.setTerm("ZZ");
        controller.lookupAnnotationKey();
        resources = controller.getJsonResults();
        assertEquals("ZZ should return no results", 0, resources.size());
    }

    // TODO: need test for title

    // TODO: need filtered test (e.g. only ontologies in a certain project)

}
