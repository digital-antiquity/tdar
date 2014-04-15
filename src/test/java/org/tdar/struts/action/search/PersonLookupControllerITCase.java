package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.UserInfo;

import com.opensymphony.xwork2.Action;

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
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setFirstName("bobby");
        String result = controller.lookupPerson();
        assertEquals("operation successful", result, Action.SUCCESS);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should be empty", people.size(), 0);
    }

    @Test
    public void testPersonLookupTooShortOverride() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setLastName("B");
        controller.setMinLookupLength(0);
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertFalse("person list should have exactly 0 items", people.size() == 0);
    }

    @Test
    public void testPersonLookupTooShort() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setLastName("Br");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have exactly 0 items", people.size(), 0);
    }

    @Test
    public void testPersonLookupWithOneResult() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setEmail("test@tdar.org");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have exactly one item", people.size(), 1);
    }

    @Test
    // we should properly escape input
    public void testPersonWithInvalidInput() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        // FIXME: need more invalid input examples than just paren
        controller.setLastName("(    ");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Indexable> people = controller.getResults();
    }

    @Test
    public void testRegisteredPersonLookupWithOneResult() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setFirstName("Keit");
        controller.setRegistered("true");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have exactly one item", 2, people.size());
    }

    @Test
    @Rollback(true)
    public void testPersonWithInstitution() {
        String institution = "University of TEST is fun";
        String email = "test1234@tdar.org";
        Person person = new Person("a", "test", email);
        Institution inst = new Institution(institution);
        genericService.save(person);
        genericService.save(inst);
        person.setInstitution(inst);
        genericService.saveOrUpdate(person);
        genericService.saveOrUpdate(inst);

        searchIndexService.index(person);
        controller.setEmail(email);
        controller.setInstitution(institution);
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertTrue("person list should contain the persion created", people.contains(person));
    }

    @Test
    public void testPersonLookupWithSeveralResults() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
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
    @Rollback
    public void testUserLookup() {
        setupUsers();
        searchIndexService.indexAll(getAdminUser(), Person.class);
        // based on our test data this should return at least two records (john doe and jane doe)
        String name = "John H";
        controller.setTerm(name);
        controller.lookupPerson();
        List<Indexable> people = controller.getResults();
        logger.debug("people: {}", people);
        if (people != null) {
            this.log.debug("people size:" + people.size() + "value:" + people);
        }
        assertTrue("at least two people in search results", people.size() >= 2);
        Person p1 = (Person) people.get(0);
        Person p2 = (Person) people.get(1);
        assertTrue("person name is John H", p1.getProperName().startsWith(name));
        assertTrue("person name is John H", p2.getProperName().startsWith(name));
    }

    private void setupUsers() {
        createUser("John", "Howard", "jh@asd.edu");
        createUser("John", "Anderies", "msdfaderies@ads.edu");
        createUser("Joshua", "Watts", "joasdftts@aas.edu");
        createUser("Annie", "Way", "agwfdsfadsaf@wuasdfsad.edu");
        createUser("John", "Wade", "wad@esadf.edu");
        createUser("John", "Wall", "johnw@gmsadfasdfail.com");
        createUser("John", "Wallrodt", "johnsdf@cladsfasdf.uc.edu");
        createUser("John", "Howard", "johsfsd@uasdfsagsd.ie");
        createUser("John", "Roney", "jrc@o.com");
        createUser("John", "de Bry", "jry@logy.org");
    }

    private void createUser(String string, String string2, String string3) {
        Person person = new Person(string, string2, string3);
        person.setUsername(string3);
        UserInfo userInfo = new UserInfo();
        userInfo.setUser(person);
        person.setUserInfo(userInfo);
        userInfo.setContributor(true);
        genericService.saveOrUpdate(person);
    }
}
