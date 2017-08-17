package org.tdar.core.bean.entity;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.entity.PersonDao;
import org.tdar.core.service.GenericService;

public class PersonITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    @Autowired
    private PersonDao personDao;

    private static String NEW_NAME = "first name";
    private static String NEW_NAME2 = "first";

    @Test
    public void testEmptyFind() {
        Person p = genericService.find(Person.class, (Long) null);
        assertNull(p);
    }

    
    @Test
    @Rollback
    public void testPersonSimiar() {
        genericService.save(new Person("S.", "Wells", null));
        genericService.save(new Person("S", "Wells", null));
        List<Person> findSimilarPeople = entityService.findSimilarPeople(new TdarUser("Susan J.", "Wells", "S@b,com"));
        logger.debug("people: {}", findSimilarPeople);
        assertTrue(findSimilarPeople.size() == 3);
    }
    
    @Test
    @Rollback
    public void testFindByExample() {
        // create a user that we will try to find
        TdarUser user1 = createAndSaveNewUser();
        user1.setDescription("this sia tst");
        user1.setUsername("1234");
        genericService.update(user1);

        // now create a prototypical user object that we'll use to find the first user
        TdarUser example = new TdarUser();
        example.setId(null);
        example.setEmail(user1.getEmail());

        Set<Person> results = personDao.findByPerson(example);
        assertThat("findByExample should find only one person", results, hasSize(1));
    }

    @Test
    @Rollback(true)
    public void testCreatePerson() {
        TdarUser person = createAndSaveNewUser();
        genericService.saveOrUpdate(person);
        logger.debug("{}", person);
        assertNotNull(person);
        Long id = person.getId();
        assertNotNull(id);
        assertTrue(id > 0);
        person = null;
        TdarUser user = genericService.find(TdarUser.class, id);
        logger.debug("user:{} ", user);

    }

    @Test
    @Rollback(true)
    public void testFindPerson() {
        Person person = new Person();
        person.setFirstName("j");
        person.setLastName("");
        person.setEmail("");
        Person findOrSaveCreator = entityService.findOrSaveCreator(person);
        logger.info("person: {} {}", findOrSaveCreator, findOrSaveCreator.getId());
        assertTrue(12540 < findOrSaveCreator.getId());
        Person person2 = new Person();
        person2.setFirstName("jim");
        person2.setLastName("");
        Person findOrSaveCreator2 = entityService.findOrSaveCreator(person2);
        logger.info("person: {} {}", findOrSaveCreator2, findOrSaveCreator2.getId());
        assertTrue(12540 < findOrSaveCreator2.getId());
        Person person3 = new Person();
        person3.setFirstName("jim");
        person3.setLastName("DeVos");
        Person findOrSaveCreator3 = entityService.findOrSaveCreator(person3);
        logger.info("person: {} {}", findOrSaveCreator3, findOrSaveCreator3.getId());
        assertEquals(8608L, findOrSaveCreator3.getId().longValue());
    }

    @Test
    @Rollback(true)
    public void testModifyPerson() {
        Person person = createAndSaveNewUser();
        Long id = person.getId();
        getLogger().info("testing findById / modify/update");
        person.setFirstName(NEW_NAME);
        genericService.saveOrUpdate(person);
        person = null;

        person = genericService.find(Person.class, id);
        assertTrue(person.getFirstName().equals(NEW_NAME));

        person.setFirstName(NEW_NAME2);
        genericService.update(person);
        person = null;

        person = genericService.find(Person.class, id);
        assertTrue(person.getFirstName().equals(NEW_NAME2));

    }

    @Test
    @Rollback(true)
    public void testDeletePerson() {
        Person person = createAndSaveNewUser();
        Long id = person.getId();

        getLogger().info("testing delete");
        genericService.delete(person);
        assertEquals(Status.DELETED, genericService.find(Person.class, id).getStatus());

    }

    @Test
    @Rollback(true)
    public void testUpdatePersons() {
        List<Person> people = new ArrayList<Person>();
        Person person = createAndSaveNewPerson(TestConstants.DEFAULT_EMAIL, "");
        people.add(person);

        Long id = person.getId();
        assertNotNull(person);
        assertNotNull(person.getId());
        assertTrue(person.getId() > 0);

        Person person2 = createAndSaveNewPerson("test2@localhost.com", "3");
        Long id2 = person2.getId();
        people.add(person2);
        genericService.save(people);

        getLogger().info("testing collection  modify/update");

        person = genericService.find(Person.class, id);
        assertTrue(person.getFirstName().equals(TestConstants.DEFAULT_FIRST_NAME));

        person2 = genericService.find(Person.class, id2);
        assertTrue(person2.getFirstName().equals(TestConstants.DEFAULT_FIRST_NAME + "3"));
    }

    @Test
    @Rollback(true)
    public void testDeletePersons() {
        List<Person> people = new ArrayList<Person>();
        Person person = createAndSaveNewPerson(TestConstants.DEFAULT_EMAIL, "");
        Long id = person.getId();
        people.add(person);
        Person person2 = createAndSaveNewPerson("test2@localhost.com", "3");
        Long id2 = person2.getId();
        people.add(person2);

        getLogger().info("testing collection delete");
        genericService.delete(people);
        assertNull(genericService.find(Person.class, id));
        assertNull(genericService.find(Person.class, id2));

    }

    @Test
    @Rollback(true)
    public void testPersonAutoFields() {
        Person person = createAndSaveNewPerson(TestConstants.DEFAULT_EMAIL, "");
        getLogger().debug("email is: " + person.getEmail());
        getLogger().debug("id: " + person.getId());
        // getLogger().debug("creatorType: " + person.getCreatorType());

        assertEquals("should have CreatorType.PERSON set on CreatorType", CreatorType.PERSON, person.getCreatorType());
        assertNotNull("should have a date created", person.getDateCreated());
    }

    @SuppressWarnings("unused")
    @Test(expected = org.hibernate.NonUniqueObjectException.class)
    @Rollback
    public void testPersonBecomesWritable() {
        Person person = createAndSaveNewPerson("robert.loblaw@mailinator.org", "");
        Institution institution = new Institution();
        institution.setName("Loblaw At Law");
        genericService.save(institution);
        person.setInstitution(institution);
        genericService.saveOrUpdate(person);

        // object wasn't read-only to begin with, but who cares

        Institution inst = person.getInstitution();
        // markWritable detaches person, but we re-assign it so we're cool.... OR ARE WE??
        person = genericService.markWritable(person);
        // ... later in the code
        institution.setName("Bob Loblaw and Associates");
        // This call will fail. some jerk done detached our institution!
        genericService.saveOrUpdate(institution);
    }

}
