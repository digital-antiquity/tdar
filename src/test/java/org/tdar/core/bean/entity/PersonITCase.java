package org.tdar.core.bean.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.service.GenericService;

public class PersonITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    private static String NEW_NAME = "first name";
    private static String NEW_NAME2 = "first";

    @Test
    public void testEmptyFind() {
        Person p = genericService.find(Person.class, (Number)null);
        assertNull(p);
    }

    public Person setupPerson() {
        return createAndSaveNewPerson();
    }

    @Test
    @Rollback(true)
    public void testCreatePerson() {
        Person person = setupPerson();
        assertNotNull(person);
        assertNotNull(person.getId());
        assertTrue(person.getId() > 0);
    }

    @Test
    @Rollback(true)
    public void testModifyPerson() {
        Person person = setupPerson();
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
        Person person = setupPerson();
        Long id = person.getId();

        getLogger().info("testing delete");
        genericService.delete(person);
        assertNull(genericService.find(Person.class, id));

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
    
    @Test
    @Rollback(true)
    public void testPersonEqualsHashCode() {
        final String emailPrefix = "uniquely";
        LinkedHashSet<Person> personSet = new LinkedHashSet<Person>();
        ArrayList<Long> ids = new ArrayList<Long>();
        int numberOfPersonsToCreate = 10;
        for (int i = 0; i < numberOfPersonsToCreate; i++) {
            Person person = createAndSaveNewPerson(emailPrefix + i + TestConstants.DEFAULT_EMAIL, "");
            personSet.add(person);
            ids.add(person.getId());
        }
        assertEquals(numberOfPersonsToCreate, personSet.size());
        ArrayList<Person> personList = new ArrayList<Person>(personSet);
        for (int i = 0; i < numberOfPersonsToCreate; i++) {
            Person persistedPerson = personList.get(i);
            Person person = new Person();
            assertNotSame(persistedPerson.hashCode(), person.hashCode());
            person.setFirstName(TestConstants.DEFAULT_FIRST_NAME);
            assertFalse(personSet.contains(person));
            assertNotSame(persistedPerson.hashCode(), person.hashCode());
            assertFalse(persistedPerson.equals(person));
            person.setLastName(TestConstants.DEFAULT_LAST_NAME);
            assertFalse(personSet.contains(person));
            assertNotSame(persistedPerson.hashCode(), person.hashCode());
            assertFalse(persistedPerson.equals(person));
            
            person.setEmail(emailPrefix + i + TestConstants.DEFAULT_EMAIL);
            // FIXME: hack to get hashCode() to not think this is a transient instance and return
            // Object.hashCode().. should rethink this.
            person.setId(3L);
            assertTrue(personSet + " should contain " + person, personSet.contains(person));
            assertEquals(persistedPerson.hashCode(), person.hashCode());
            assertTrue(personSet.contains(person));
            assertEquals(persistedPerson, person);
            
            person.setId(ids.get(i));
            assertTrue(personSet.contains(person));
            assertEquals(personList.get(i), person);
            assertEquals(persistedPerson.hashCode(), person.hashCode());
        }
    }

}
