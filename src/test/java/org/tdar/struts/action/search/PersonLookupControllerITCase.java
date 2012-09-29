package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Person;

public class PersonLookupControllerITCase extends AbstractIntegrationTestCase {

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
}
