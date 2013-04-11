package org.tdar.core.bean.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericService;

public class PersonITCase extends AbstractIntegrationTestCase {

    @Autowired
    private GenericService genericService;

    private static String NEW_NAME = "first name";
    private static String NEW_NAME2 = "first";

    @Test
    public void testEmptyFind() {
        Person p = genericService.find(Person.class, (Long) null);
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

    @Test
    @Rollback(true)
    /**
     * 
     * This test makes various assertions based on our expectations of our implementation of hashCode() and equals(). How we think it works: 
     *  - If object.id == -1,  hashcode is object.id.hashCode()
     *  - If object.id != -1,  hashcode is based on object.equalityFields
     *  - we cache an object's hashCode() value to avoid "hiding" set/map items by modifying their contents (e.g. saving an object in a set)
     *  
     */
    // FIXME This test currently fails because it violates the hashCode contract {@link java.lang.Object#hashCode()}.
    public void testPersonEqualsHashCode() {
        final String emailPrefix = "uniquely";
        LinkedHashSet<Person> personSet = new LinkedHashSet<Person>();
        ArrayList<Long> ids = new ArrayList<Long>();
        int numberOfPersonsToCreate = 10;
        for (int i = 0; i < numberOfPersonsToCreate; i++) {
            Person person = createAndSaveNewPerson(emailPrefix + i + TestConstants.DEFAULT_EMAIL, "");
            ids.add(person.getId());
            personSet.add(person);
        }

        assertEquals(numberOfPersonsToCreate, personSet.size());
        ArrayList<Person> personList = new ArrayList<Person>(personSet);
        for (int i = 0; i < numberOfPersonsToCreate; i++) {
            Person persistedPerson = personList.get(i);

            //person equality based on business key. if we copy the business key fields the two person objects should be equal
            Person person = new Person();
            person.setEmail(persistedPerson.getEmail());
            person.setRegistered(persistedPerson.isRegistered());
            person.setLastName(persistedPerson.getLastName());
            person.setFirstName(persistedPerson.getFirstName());
            person.setPhone(persistedPerson.getPhone());
            assertEquals(persistedPerson, person);

            //the person record is 'transient'.  
            assertTrue(Persistable.Base.isTransient(person));
            //if we simulate a save by giving it an ID...
            person.setId(persistedPerson.getId() + 15L);

            //... it should still be equal to persistedPerson
            assertEquals("these should still be equal even after save", persistedPerson, person);
            assertEquals("therefore hashcodes should be the same", persistedPerson.hashCode(), person.hashCode());



            assertEquals(persistedPerson, person);
            if (!personSet.contains(person)) {
                logger.error("hashset.contains() should be true for {} but was false", person);
                int[] hashcodes = new int[personSet.size()];
                int j = 0;
                for (Person peep : personSet) {
                    hashcodes[j++] = peep.hashCode();
                }
                logger.error("personset hashcodes: {}", hashcodes);
                logger.error("    person hashcode: {}", person.hashCode());

                String expectation = "If two objects are equal according to the equals(Object) method, then calling the hashCode method on each of the two " +
                        "objects must produce the same integer result.";
                assertEquals(expectation, persistedPerson, person);
                assertEquals(expectation, persistedPerson.hashCode(), person.hashCode());
            }
            assertEquals(persistedPerson.hashCode(), person.hashCode());
            assertTrue(personSet.contains(person));
            assertEquals(persistedPerson, person);

            person.setId(ids.get(i));
            assertTrue(personSet.contains(person));
            assertEquals(personList.get(i), person);
            assertEquals(persistedPerson.hashCode(), person.hashCode());
        }
    }

    @Test
    @Rollback
    // this test is a bit more academic, but is another examlple of where our hashcode/equals implementation fails.
    // per java docs: if A == B is true, and B==C is true, then A == C should be true
    public void testPersonTransitiveEquality() {
        Person a = new Person("Loblaw", "Bob", "bob.loblaw@compuserve.net");
        Person b = new Person("Loblaw", "Bob", "bob.loblaw@compuserve.net");
        Person c = new Person("Loblaw", "Bob", "bob.loblaw@compuserve.net");
        genericService.save(b);
        
        boolean eq = a.equals(b);
        logger.debug("a == b: {}", eq);
        assertEquals("a should equal b", a, b);
        assertEquals("b should equal c", b, c);
        assertEquals("a should equal c", a, c);
    }
}
