package org.tdar.search.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.EntityService;
import org.tdar.search.bean.PersonSearchOption;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.CreatorSearchInterface;
import org.tdar.utils.MessageHelper;

public class PersonSearchITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    CreatorSearchInterface<Person> creatorSearchService;

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    EntityService entityService;

    private int min = 3;

    @Override
    public void reindex() {
        searchIndexService.purgeAll(LookupSource.PERSON);
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
    };

    @Test
    @Rollback
    public void testPersonRelevancy() throws SearchException, SearchIndexException, IOException, ParseException {
        List<Person> people = new ArrayList<>();
        Person whelan = new Person("Mary", "Whelan", null);
        people.add(whelan);
        Person mmc = new Person("Mary", "McCready", null);
        Person mmc2 = new Person("McCready", "Mary", null);
        people.add(mmc);
        people.add(new Person("Doug", "Mary", null));
        people.add(mmc2);
        people.add(new Person("Mary", "Robbins-Wade", null));
        people.add(new Person("Robbins-Wade", "Mary", null));
        for (Person p : people) {
            updateAndIndex(p);
        }

        SearchResult<Person> result = searchPerson("Mary Whelan");

        List<Person> results = result.getResults();
        logger.debug("Results: {}", results);
        assertTrue(results.get(0).equals(whelan));
        assertTrue(results.size() == 1);

        result = searchPerson("Mary McCready");
        results = result.getResults();
        logger.debug("Results: {}", results);
        assertTrue(results.contains(mmc));
        assertTrue(results.contains(mmc2));
        assertTrue(results.size() == 2);
    }

    private void updateAndIndex(Indexable doc) throws SearchException, SearchIndexException, IOException {
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
    }

    private SearchResult<Person> searchPerson(String term) throws ParseException, SearchException, SearchIndexException, IOException {
        SearchResult<Person> result = new SearchResult<>();
        PersonSearchOption personSearchOption = PersonSearchOption.ALL_FIELDS;
        creatorSearchService.findPerson(term, personSearchOption, result, MessageHelper.getInstance());
        assertResultsOkay(term, result);
        return result;
    }

    @Test
    @Rollback
    public void testPersonSearch() throws ParseException, SearchException, SearchIndexException, IOException {
        String term = "Manney";
        searchPerson(term);
    }

    @Test
    @Rollback
    public void testPersonFullNameSearch() throws ParseException, SearchException, SearchIndexException, IOException {
        String term = "Joshua Watts";
        searchPerson(term);
    }

    private void assertResultsOkay(String term, SearchResult<Person> controller_) {
        assertNotEmpty(controller_.getResults());
        for (Object obj : controller_.getResults()) {
            Person inst = (Person) obj;
            if (!term.contains(" ")) {
                assertTrue(String.format("Creator %s should match %s", inst, term), inst.getProperName().toLowerCase().contains(term.toLowerCase()));
            } else {
                assertTrue(String.format("Creator %s should match %s", inst.getProperName(), term),
                        StringUtils.contains(term.toLowerCase(), inst.getFirstName().toLowerCase()));
                assertTrue(String.format("Creator %s should match %s", inst.getProperName(), term),
                        StringUtils.contains(term.toLowerCase(), inst.getLastName().toLowerCase()));

            }
        }
        logger.info("{}", controller_.getResults());
    }

    @Test
    public void testPersonLookupWithNoResults() throws SearchException, SearchIndexException, IOException, ParseException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        Person person_ = setupPerson("bobby", null, null, null);
        SearchResult<Person> result = findPerson(person_, null, null, min);
        List<Person> people = result.getResults();
        assertEquals("person list should be empty", people.size(), 0);
    }

    @Test
    @Rollback
    public void testUserLookupWithrelevancy() throws SearchException, SearchIndexException, IOException, ParseException {
        TdarUser person = setupMScottPeople();
        SearchResult<Person> result = findPerson(null, "M Scott Thompson", true, min, SortOption.RELEVANCE);

        List<Person> people = result.getResults();
        logger.debug("results:{} ", people);
        assertEquals(person, people.get(0));

        // searching by username
        result = findPerson(null, "mscottthompson@sua.edu", null, min, SortOption.RELEVANCE, getBasicUser());
        people = result.getResults();
        logger.debug("results:{} ", people);
        assertEquals(person, people.get(0));

        result = findPerson(null, "Scott Th", null, min, SortOption.RELEVANCE);
        people = result.getResults();
        logger.debug("results:{} ", people);
        assertEquals(person, people.get(0));
    }

    @Test
    @Rollback
    public void testUserLastNameSpaceWithRelevancy() throws SearchException, SearchIndexException, IOException, ParseException {
        TdarUser person = setupMScottPeople();
        Person person_ = setupPerson(null, "Scott Th", null, null);
        SearchResult<Person> result = findPerson(person_, null, null, min, SortOption.RELEVANCE);
        List<Person> people = result.getResults();
        logger.debug("results:{} ", people);
        assertEquals(person, people.get(0));

    }

    private TdarUser setupMScottPeople() {
        TdarUser person = new TdarUser("M", "Savala", "savala@coxasdsad.net", "savala@coxasdsad.net");
        genericService.saveOrUpdate(person);
        person = new TdarUser("m", "Gomez", "gomez@hotmaadsasdil.com", "gomez@hotmaadsasdil.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser("M", "R", "mr@gmasdasdail.com", "mr@gmasdasdail.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser("M", "W", "wm@gmaiadssadl.com", "wm@gmaiadssadl.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser("Scott", "Ron", "scott@coasdsadx.net", "scott@coasdsadx.net");
        genericService.saveOrUpdate(person);
        person = new TdarUser("Frederick", "Scott", "fredrerick@hotasdasdmail.com", "fredrerick@hotasdasdmail.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser("Anne", "Scott", "anne@mchasdasdsi.com", "anne@mchasdasdsi.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser("Susan", "Scott", "susan@gmailasda.com", "susan@gmailasda.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser("Kathleen", "Scott", "katheen@designworkasdasds-tn.com", "katheen@designworkasdasds-tn.com");
        genericService.saveOrUpdate(person);
        person = new TdarUser("Scott", "Ortman", "ortman@coloradoasd.edu", "ortman@coloradoasd.edu");
        genericService.saveOrUpdate(person);
        person = new TdarUser("M", "Scott Thompson", "mscottthompson@sua.edu", "mscottthompson@sua.edu");
        genericService.saveOrUpdate(person);
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        return person;
    }

    @Test
    public void testPersonLookupTooShortOverride() throws SearchException, SearchIndexException, IOException, ParseException {
        Person person_ = setupPerson(null, "B", null, null);
        SearchResult<Person> result = findPerson(person_, null, null, 0);
        List<Person> people = result.getResults();
        assertFalse("person list should have exactly 0 items", people.size() == 0);
    }

    @Test
    public void testPersonLookupTooShort() throws SearchException, SearchIndexException, IOException, ParseException {
        Person person_ = setupPerson(null, "Br", null, null);
        SearchResult<Person> result = findPerson(person_, null, null, min);
        List<Person> people = result.getResults();
        assertEquals("person list should have exactly 0 items", people.size(), 0);
    }

    @Test
    public void testPersonLookupWithOneResult() throws SearchException, SearchIndexException, IOException, ParseException {
        Person person_ = setupPerson(null, null, "test@tdar.org", null);
        SearchResult<Person> result = findPerson(person_, null, null, min,null,getBasicUser());
        List<Person> people = result.getResults();
        assertEquals("person list should have exactly one item", people.size(), 1);
    }

    @Test
    // we should properly escape input
    public void testPersonWithInvalidInput() throws SearchException, SearchIndexException, IOException, ParseException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        // FIXME: need more invalid input examples than just paren
        Person person_ = setupPerson(null, " (     ", null, null);
        SearchResult<Person> result = findPerson(person_, null, null, min);
        List<Person> people = result.getResults();
        assertTrue(CollectionUtils.isEmpty(people));
    }

    @Test
    @Rollback
    // we should properly escape input
    public void testPersonByUsername() throws SearchException, SearchIndexException, IOException, ParseException {
        TdarUser user = new TdarUser("billing", "admin", "billingadmin@tdar.net");
        user.setUsername("billingAdmin");
        user.markUpdated(getAdminUser());
        genericService.saveOrUpdate(user);
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        Person person_ = setupPerson(null, null, null, null);
        SearchResult<Person> result = findPerson(person_, "billingAdmin", true, min);

        List<Person> people = result.getResults();
        assertNotEmpty(people);
        assertTrue(people.contains(user));
    }

    @Test
    public void testRegisteredPersonLookupWithResults() throws SearchException, SearchIndexException, IOException, ParseException {
        Person person_ = setupPerson("Keit", null, null, null);
        SearchResult<Person> result = findPerson(person_, null, true, min);
        List<Person> people = result.getResults();
        for (Person p : people) {
            logger.debug("{} {} {}", p.getClass().getSimpleName(), p.getId(), p.isRegistered());
        }
        assertEquals("person list should have exactly two items", 2, people.size());
    }

    @Test
    @Rollback(true)
    public void testPersonWithInstitution() throws SearchException, SearchIndexException, IOException, ParseException, InterruptedException {
        searchIndexService.purgeAll();
        String institution = "University of TEST is fun";
        String email = "test1234@tdar.org";
        Person person = new Person("a", "test", email);
        Institution inst = new Institution(institution);
        genericService.save(person);
        genericService.save(inst);
        person.setInstitution(inst);
        genericService.saveOrUpdate(person);
        genericService.saveOrUpdate(inst);
        logger.debug("{}", person);
        searchIndexService.index(person);
        Person person_ = setupPerson(null, null, email, institution);
        SearchResult<Person> result = findPerson(person_, null, null, min);
        List<Person> people = result.getResults();
        assertTrue("person list should contain the person created", people.contains(person));
    }

    @Test
    public void testPersonLookupWithSeveralResults() throws SearchException, SearchIndexException, IOException, ParseException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        // based on our test data this should return at least two records (john doe and jane doe)
        String partialLastName = "Mann";
        Person person_ = setupPerson(null, partialLastName, null, null);
        SearchResult<Person> result = findPerson(person_, null, null, min);
        if (result.getResults() != null) {
            logger.debug("people size:" + result.getResults().size() + "value:" + result.getResults());
        }
        assertTrue("at least two people in search results", result.getResults().size() >= 2);
    }

    @Test
    @Rollback
    public void testUserLookup() throws SearchException, SearchIndexException, IOException, ParseException {
        setupUsers();
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        // based on our test data this should return at least two records (john doe and jane doe)
        String name = "John H";
        Person person_ = setupPerson(null, null, null, null);
        SearchResult<Person> result = findPerson(person_, name, null, 0);
        List<Person> people = result.getResults();
        logger.debug("people: {}", people);
        if (people != null) {
            logger.debug("people size:" + people.size() + "value:" + people);
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

        // normally these two requests would belong to separate hibernate sessions. We flush the session here so that the we don't get back the
        // same cached objects that the controller sanitized in the previous lookup.
        genericService.clearCurrentSession();
        genericService.markReadOnly();

        // okay now "log in" and make sure that email lookup is still working
        String email = "james.t.devos@dasu.edu";
        Person person_ = setupPerson(null, null, email, null);
        SearchResult<Person> result = findPerson(person_, null, null, 0, null, getBasicUser());

        assertEquals(1, result.getResults().size());
        Person jim = (Person) result.getResults().get(0);
        assertEquals(email, jim.getEmail());
    }

    @Test
    @Rollback
    public void testUnauthenticatedWithEmail() throws ParseException, SearchException, SearchIndexException, IOException {
        Person person = new Person();
        person.setEmail("tiffany.clark@dsu.edu");
        String msg = null;
        try {
            boolean seen = true;
            SearchResult<Person> findPerson = findPerson(person, null, null, 0);
            for (Person p : findPerson.getResults()) {
                if (StringUtils.equals(p.getEmail(), person.getEmail())) {
//                    seen = true;
                } else {
                    seen = false;
                }
            }
            assertFalse("should have seen people that don't match email", seen);
        } catch (Exception e) {
            msg = e.getMessage();
        }
        assertFalse(msg != null);
    }

    @Test
    @Rollback
    public void testAuthenticatedWithEmail() throws ParseException, SearchException, SearchIndexException, IOException {
        Person person = new Person();
        person.setEmail("tiffany.clark@dsu.edu");
        SearchResult<Person> result = new SearchResult<>();
        result.setAuthenticatedUser(getBasicUser());
        creatorSearchService.findPerson(person, null, null, result, MessageHelper.getInstance(), 0);
        // we ignore the email parameter
        assertEquals(1, result.getResults().size());
    }

    @Test
    @Rollback(true)
    public void testInstitutionAlone() throws SearchException, SearchIndexException, IOException, ParseException {
        Person person = new Person("a test", "person", null);
        Institution inst = new Institution("TQF");
        genericService.saveOrUpdate(person);
        person.setInstitution(inst);
        genericService.saveOrUpdate(inst);
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        Person person_ = setupPerson(null, null, null, "TQF");
        SearchResult<Person> result = findPerson(person_, null, null, min);
        List<Person> people = result.getResults();
        assertTrue("person list should countain the person", people.contains(person));
    }

    @Test
    @Rollback(true)
    public void testValidInstitutionWithSpace() throws SearchException, SearchIndexException, IOException, ParseException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        Person person = setupPerson(null, null, null, "University of");
        SearchResult<Person> result = findPerson(person, null, null, min);
        List<Person> people = result.getResults();
        logger.info("{}", people);
        assertTrue("person list should have at least two items", people.size() >= 2);
        for (Person pers : result.getResults()) {
            assertTrue(pers.getInstitution().getName().contains(" "));
        }
    }
    

    @Test
    @Rollback(true)
    public void testPersonEscapeIssues() throws SearchException, SearchIndexException, IOException, ParseException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        SearchResult<Person> result = findPerson(new Person(), "Margaret Nelson(CNH", null, min);
        List<Person> people = result.getResults();
        logger.info("{}", people);
        // expecting not to have an exception, it's okay if it's empty
        assertTrue("person list should have at least two items", people.size() >= 0);
        for (Person pers : result.getResults()) {
            assertTrue(pers.getInstitution().getName().contains(" "));
        }
    }

    @Test
    @Rollback(true)
    public void testInstitutionEmpty() throws SearchException, SearchIndexException, IOException, ParseException {
        searchIndexService.indexAll(getAdminUser(), LookupSource.PERSON);
        // FIXME: should not need to be quoted
        Person person = setupPerson(null, null, null, "University ABCD");
        SearchResult<Person> result = findPerson(person, null, null, min);
        List<Person> people = result.getResults();
        assertEquals("person list should have 0 item(s)", 0, people.size());
    }

    private Person setupPerson(String firstName, String lastName, String email, String institution) {
        Person person = new Person(firstName, lastName, email);
        if (StringUtils.isNotBlank(institution)) {
            person.setInstitution(new Institution(institution));
        }
        return person;
    }

    private SearchResult<Person> findPerson(Person person_, String term, Boolean registered, int min2, SortOption relevance, TdarUser user)
            throws ParseException, SearchException, SearchIndexException, IOException {
        SearchResult<Person> result = new SearchResult<>();
        result.setAuthenticatedUser(user);
        result.setSortField(relevance);
        creatorSearchService.findPerson(person_, term, registered, result, MessageHelper.getInstance(), min2);
        return result;
    }

    private SearchResult<Person> findPerson(Person person, String term, Boolean registered, int min2, SortOption option) throws ParseException, SearchException, SearchIndexException, IOException {
        return findPerson(person, term, registered, min2, option, null);
    }
    private SearchResult<Person> findPerson(Person person, String term, Boolean registered, int min2) throws ParseException, SearchException, SearchIndexException, IOException {
        return findPerson(person, term, registered, min2, null, null);
    }
}