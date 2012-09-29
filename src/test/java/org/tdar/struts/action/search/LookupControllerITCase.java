package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.JsonModel;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceAnnotationType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;

public class LookupControllerITCase extends AbstractIntegrationTestCase {

    @Autowired
    private LookupController controller;
    private Logger log = Logger.getLogger(getClass());

    @Before
    public void initController() {
        controller = generateNewInitializedController(LookupController.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    public void testPersonLookupWithNoResults() {
        searchIndexService.indexAll(Person.class);
        controller.setFirstName("bobby");
        String result = controller.lookupPerson();
        assertEquals("operation successful", result, LookupController.SUCCESS);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should be empty", people.size(), 0);
    }

    @Test
    public void testPersonLookupTooShortOverride() {
        searchIndexService.indexAll(Person.class);
        controller.setLastName("B");
        controller.setMinLookupLength(0);
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertFalse("person list should have exactly 0 items", people.size() == 0);
    }

    @Test
    public void testPersonLookupTooShort() {
        searchIndexService.indexAll(Person.class);
        controller.setLastName("Br");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have exactly 0 items", people.size(), 0);
    }

    @Test
    public void testPersonLookupWithOneResult() {
        searchIndexService.indexAll(Person.class);
        controller.setEmail("test@tdar.org");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
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
        List<Indexable> people = controller.getResults();
    }

    @Test
    public void testRegisteredPersonLookupWithOneResult() {
        searchIndexService.indexAll(Person.class);
        controller.setFirstName("Keit");
        controller.setRegistered("true");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have exactly one item", 2, people.size());
    }

    @Test
    public void testPersonWithInstitution() {
        searchIndexService.indexAll(Person.class);
        controller.setEmail("test@tdar.org");
        controller.setInstitution("University of TEST");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have exactly one item", 1, people.size());
    }

    @Test
    public void testInstitutionAlone() {
        searchIndexService.indexAll(Person.class);
        // FIXME: should not need to be quoted
        controller.setInstitution("TQF");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have exactly one item", 1, people.size());
    }

    @Test
    public void testValidInstitutionWithSpace() {
        searchIndexService.indexAll(Person.class);
        // FIXME: should not need to be quoted
        controller.setInstitution("University of");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        logger.info("{}", people);
        assertEquals("person list should have exactly one item", 2, people.size());
    }

    @Test
    public void testInstitutionEmpty() {
        searchIndexService.indexAll(Person.class);
        // FIXME: should not need to be quoted
        controller.setInstitution("University ABCD");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have exactly one item", 0, people.size());
    }

    @Test
    public void testPersonLookupWithSeveralResults() {
        searchIndexService.indexAll(Person.class);
        // based on our test data this should return at least two records (john doe and jane doe)
        String partialLastName = "Mann";
        controller.setLastName(partialLastName);
        controller.lookupPerson();
        List<Indexable> people = controller.getResults();
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
        List<Indexable> institutions = controller.getResults();
        assertEquals("person list should be empty", institutions.size(), 0);
    }

    @Test
    public void testInstitutionLookupWithOneResult() {
        searchIndexService.indexAll(Institution.class);
        controller.setInstitution("tfqa");
        controller.lookupInstitution();
        List<Indexable> institutions = controller.getResults();
        assertTrue("only one result expected", institutions.size() == 1);
    }

    // given test script, searching 'digital' should return multiple results that start with 'Digital Antiquity'
    @Test
    public void testInstitutionLookupWithMultiple() {
        searchIndexService.indexAll(Institution.class);
        controller.setInstitution("University");
        controller.lookupInstitution();
        List<Indexable> institutions = controller.getResults();
        assertTrue("more than one result expected", institutions.size() > 1);
    }

    // searching for string witn blanks should yield zero results.
    @Test
    public void testInstitutionLookupWithBlanks() {
        searchIndexService.indexAll(Institution.class);
        String blanks = "    ";
        controller.setInstitution(blanks);
        controller.lookupInstitution();
        List<Indexable> results = controller.getResults();
        Assert.assertEquals("expecting zero results", 0, results.size());
    }

    @Test
    public void testResourceLookupByType() {
        searchIndexService.indexAll(Resource.class);
        // get back all documents
        controller.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT));
        controller.lookupResource();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByProjectId() {
        searchIndexService.indexAll(Resource.class);
        controller.setProjectId(3073L);
        controller.lookupResource();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }
    
    @Test
    @Rollback(value=true)
    public void testDeletedResourceFilteredForNonAdmins() {
        //init(controller, getUser(TestConstants.ADMIN_USER_ID));

        searchIndexService.indexAll(Resource.class);
        controller.setUseSubmitterContext(true);
        Project proj  = createAndSaveNewProject("project to be deleted");

        searchIndexService.index(proj);

        controller.lookupResource();
        assertTrue(controller.getResults().contains(proj));
        
        //now delete the resource and makes sure it doesn't show up for the common rabble
        proj.setStatus(Status.DELETED);
        genericService.saveOrUpdate(proj);
        initController();
        controller.setUseSubmitterContext(true);
        searchIndexService.index(proj);
        controller.lookupResource();
        assertFalse(controller.getResults().contains(proj));

        //now pretend that it's an admin visiting the dashboard.  Even though they can view/edit everything, deleted items 
        //won't show up in their dashboard unless they are the submitter or have explicitly been given access rights, so we update the project's submitter
        proj.setSubmitter(getAdminUser());
        genericService.saveOrUpdate(proj);
        searchIndexService.index(proj);
        controller = generateNewController(LookupController.class);
        init(controller, getAdminUser());
        controller.setUseSubmitterContext(true); 
        controller.setRecordsPerPage(10000000);
        controller.lookupResource();
        assertTrue(controller.getResults().contains(proj));
    }

    @Test
    public void testKeywordLookup() {
        searchIndexService.indexAll(GeographicKeyword.class, CultureKeyword.class);
        controller.setKeywordType("culturekeyword");
        controller.setTerm("Folsom");
        controller.lookupKeyword();
        List<Indexable> resources = controller.getResults();
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
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() == 2);

        // FIXME: not properly simulating new page request
        controller.setTerm("ZZ");
        controller.lookupAnnotationKey();
        resources = controller.getResults();
        assertEquals("ZZ should return no results", 0, resources.size());
    }

    // TODO: need test for title

    // TODO: need filtered test (e.g. only ontologies in a certain project)

    public void initControllerFields() {
        searchIndexService.indexAll();
        List<String> types = new ArrayList<String>();
        types.add("DOCUMENT");
        types.add("ONTOLOGY");
        types.add("CODING_SHEET");
        types.add("IMAGE");
        types.add("DATASET");
        controller.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT,
                ResourceType.ONTOLOGY, ResourceType.IMAGE, ResourceType.DATASET,
                ResourceType.CODING_SHEET));
    }

    @Test
    public void testResourceLookup() {
        initControllerFields();
        controller.setTitle("HARP");
        controller.lookupResource();

        JSONArray jsonArray = new JSONArray();
        for (Indexable persistable : controller.getResults()) {
            if (persistable instanceof JsonModel) {
                jsonArray.add(((JsonModel) persistable).toJSON());
            }
        }
        String json = jsonArray.toString();
        logger.debug("resourceLookup results:{}", json);
        // assertTrue(json.contains("iTotalRecords"));
        assertTrue(json.contains("HARP"));
    }
    
    @Test
    public void testLookingForInstitution() {
        searchIndexService.indexAll(Institution.class);
        String name1 = "U.S. Department of the Interior";
        String term = "U.S. Department of";
        
        List<Institution> insts = Arrays.asList(new Institution(name1), new Institution("National Geographic Society (U.S.)")
        , new Institution("Robertson Research (U.S.)")
        , new Institution("Southeastern Archaeological Center (U.S.)")
        , new Institution("Southwestern Anthropological Research Group (U.S.)")
        , new Institution("U.S. Department of Agricultural Forest Service"));
        genericService.save(insts);
        searchIndexService.index(insts.toArray(new Institution[0]));
        controller.setInstitution(term);
        controller.lookupInstitution();
        List<Indexable> results = controller.getResults();
        logger.debug("results:{}", results);
        Assert.assertEquals("expecting interior and agriculture forest service", 2, results.size());
        
        initControllerFields();
        controller.setInstitution("U.S.");
        controller.lookupInstitution();
        results = controller.getResults();
        logger.debug("results:{}", results);
        for(Indexable indx :  results) {
            Institution inst = (Institution)indx;
            assertTrue(inst.getName().toLowerCase().contains("u.s."));
        }
        
        initControllerFields();
        controller.setInstitution("u.s.");
        controller.lookupInstitution();
        results = controller.getResults();
        logger.debug("results:{}", results);
        for(Indexable indx :  results) {
            Institution inst = (Institution)indx;
            assertTrue(inst.getName().toLowerCase().contains("u.s."));
        }
        initControllerFields();
        controller.setInstitution("US");
        controller.lookupInstitution();
        results = controller.getResults();
        logger.debug("results:{}", results);
        for(Indexable indx :  results) {
            Institution inst = (Institution)indx;
            assertTrue(inst.getName().toLowerCase().contains("u.s."));
        }
        
        
    }
    

}
