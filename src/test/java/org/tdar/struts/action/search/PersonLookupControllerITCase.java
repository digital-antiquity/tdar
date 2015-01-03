package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.ReflectionService;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.lookup.PersonLookupAction;
import org.tdar.struts.interceptor.ObfuscationResultListener;

import com.opensymphony.xwork2.Action;

public class PersonLookupControllerITCase extends AbstractIntegrationTestCase {

    @Autowired
    ObfuscationService obfuscationService;

    @Autowired
    ReflectionService reflectionService;

    @Autowired
    private PersonLookupAction controller;
    private Logger log = Logger.getLogger(getClass());

    @Before
    public void initController() {
        controller = generateNewInitializedController(PersonLookupAction.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    public void testPersonLookupWithNoResults() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setFirstName("bobby");
        String result = controller.lookupPerson();
        assertEquals("operation successful", result, Action.SUCCESS);
        List<Person> people = controller.getResults();
        assertEquals("person list should be empty", people.size(), 0);
    }

    @Test
    @Rollback
    public void testUserLookupWithrelevancy() {
        TdarUser person = new TdarUser();
        person.setLastName("Savala");
        person.setFirstName("M");
        person.setEmail("savala@coxasdsad.net");
        person.setUsername("savala@coxasdsad.net");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("Gomez");
        person.setFirstName("m");
        person.setEmail("gomez@hotmaadsasdil.com");
        person.setUsername("gomez@hotmaadsasdil.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("R");
        person.setFirstName("M");
        person.setEmail("mr@gmasdasdail.com");
        person.setUsername("mr@gmasdasdail.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("W");
        person.setFirstName("M");
        person.setEmail("wm@gmaiadssadl.com");
        person.setUsername("wm@gmaiadssadl.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("Scott");
        person.setFirstName("Ron");
        person.setEmail("scott@coasdsadx.net");
        person.setUsername("scott@coasdsadx.net");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("Scott");
        person.setFirstName("Frederick");
        person.setEmail("fredrerick@hotasdasdmail.com");
        person.setUsername("fredrerick@hotasdasdmail.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("Scott");
        person.setFirstName("Anne");
        person.setEmail("anne@mchasdasdsi.com");
        person.setUsername("anne@mchasdasdsi.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("Scott");
        person.setFirstName("Susan");
        person.setEmail("susan@gmailasda.com");
        person.setUsername("susan@gmailasda.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("Scott");
        person.setFirstName("Kathleen");
        person.setEmail("katheen@designworkasdasds-tn.com");
        person.setUsername("katheen@designworkasdasds-tn.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser();
        person.setLastName("Ortman");
        person.setFirstName("Scott");
        person.setEmail("ortman@coloradoasd.edu");
        person.setUsername("ortman@coloradoasd.edu");
        person = new TdarUser();
        person.setLastName("Scott Thompson");
        person.setFirstName("M");
        person.setEmail("mscottthompson@sua.edu");
        person.setUsername("mscottthompson@sua.edu");
        genericService.saveOrUpdate(person);

        // searching by name
        controller.setTerm("M Scott Thompson");
        controller.setRegistered("true");
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setSortField(SortOption.RELEVANCE);
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
        logger.debug("results:{} ", people);
        assertEquals(person, people.get(0));

        // searching by username
        controller = generateNewInitializedController(PersonLookupAction.class, getAdminUser());
        controller.setTerm("mscottthompson@sua.edu");
        controller.setSortField(SortOption.RELEVANCE);
        result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        people = controller.getResults();
        logger.debug("results:{} ", people);
        assertEquals(person, people.get(0));

        // searching by username
        controller = generateNewInitializedController(PersonLookupAction.class, getAdminUser());
        controller.setTerm("M Scott Th");
        controller.setSortField(SortOption.RELEVANCE);
        result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        people = controller.getResults();
        logger.debug("results:{} ", people);
        assertEquals(person, people.get(0));
    }

    @Test
    public void testPersonLookupTooShortOverride() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setLastName("B");
        controller.setMinLookupLength(0);
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
        assertFalse("person list should have exactly 0 items", people.size() == 0);
    }

    @Test
    public void testPersonLookupTooShort() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setLastName("Br");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
        assertEquals("person list should have exactly 0 items", people.size(), 0);
    }

    @Test
    public void testPersonLookupWithOneResult() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setEmail("test@tdar.org");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
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
        List<Person> people = controller.getResults();
    }

    @Test
    @Rollback
    // we should properly escape input
    public void testPersonByUsername() {
        TdarUser user = new TdarUser("billing", "admin", "billingadmin@tdar.net");
        user.setUsername("billingAdmin");
        user.markUpdated(getAdminUser());
        genericService.saveOrUpdate(user);
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setTerm("billingAdmin");
        controller.setRegistered("true");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
        assertNotEmpty(people);
        assertTrue(people.contains(user));
    }

    @Test
    public void testRegisteredPersonLookupWithOneResult() {
        searchIndexService.indexAll(getAdminUser(), Person.class, TdarUser.class);
        controller.setFirstName("Keit");
        controller.setRegistered("true");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
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
        List<Person> people = controller.getResults();
        assertTrue("person list should contain the persion created", people.contains(person));
    }

    @Test
    public void testPersonLookupWithSeveralResults() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        // based on our test data this should return at least two records (john doe and jane doe)
        String partialLastName = "Mann";
        controller.setLastName(partialLastName);
        controller.lookupPerson();
        List<Person> people = controller.getResults();
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
        List<Person> people = controller.getResults();
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
        TdarUser person = new TdarUser(string, string2, string3);
        person.setUsername(string3);
        person.setContributor(true);
        genericService.saveOrUpdate(person);
    }

    @Test
    @Rollback
    public void testSanitizedPersonRecords() throws Exception {

        // important! normally the SessionSecurityInterceptor would mark the session as readonly, but we need to do it manually in a test
        genericService.markReadOnly();

        searchIndexService.indexAll(getAdminUser(), Person.class);
        // "log out"
        controller = generateNewController(PersonLookupAction.class);
        initAnonymousUser(controller);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        controller.setMinLookupLength(0);
        controller.lookupPerson();
        ObfuscationResultListener listener = new ObfuscationResultListener(obfuscationService, reflectionService, null, null);
        listener.prepareResult(controller);
        assertTrue(controller.getResults().size() > 0);
        for (Indexable result : controller.getResults()) {
            assertNull(((Person) result).getEmail());
        }

        // normally these two requests would belong to separate hibernate sessions. We flush the session here so that the we don't get back the
        // same cached objects that the controller sanitized in the previous lookup.
        genericService.clearCurrentSession();
        genericService.markReadOnly();

        // okay now "log in" and make sure that email lookup is still working
        controller = generateNewInitializedController(PersonLookupAction.class, getAdminUser());
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        controller.setMinLookupLength(0);
        String email = "james.t.devos@dasu.edu";
        controller.setEmail(email);
        controller.lookupPerson();
        assertEquals(1, controller.getResults().size());
        Person jim = (Person) controller.getResults().get(0);
        assertEquals(email, jim.getEmail());
    }

    @Test
    @Rollback(true)
    public void testInstitutionAlone() {
        Person person = new Person("a test", "person", null);
        Institution inst = new Institution("TQF");
        genericService.saveOrUpdate(person);
        person.setInstitution(inst);
        genericService.saveOrUpdate(inst);
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setInstitution("TQF");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
        assertTrue("person list should have exactly one item", people.contains(person));
    }

    @Test
    @Rollback(true)
    public void testValidInstitutionWithSpace() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        controller.setInstitution("University of");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
        logger.info("{}", people);
        assertTrue("person list should have at least two items", people.size() >= 2);
        for (Indexable p : controller.getResults()) {
            Person pers = (Person) p;
            assertTrue(pers.getInstitution().getName().contains(" "));
        }
    }

    @Test
    @Rollback(true)
    public void testInstitutionEmpty() {
        searchIndexService.indexAll(getAdminUser(), Person.class);
        // FIXME: should not need to be quoted
        controller.setInstitution("University ABCD");
        String result = controller.lookupPerson();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Person> people = controller.getResults();
        assertEquals("person list should have 0 item(s)", 0, people.size());
    }
}
