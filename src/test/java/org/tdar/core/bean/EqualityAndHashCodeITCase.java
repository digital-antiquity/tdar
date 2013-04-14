package org.tdar.core.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;

public class EqualityAndHashCodeITCase extends AbstractIntegrationTestCase {

    @Test
    public void testSkeletonPersonRetentionInSet() {
        HashSet<Person> personSet = new HashSet<Person>();
        for (int i = 1; i < 3; i++) {
            Person person = new Person();
            Long lng = new Long(i);
            person.setId(lng);
            personSet.add(person);
        }
        logger.info("people: {}" , personSet);
        assertEquals(2, personSet.size());
    }

    
    @Test
    @Rollback(true)
    public void testEqualsHashCode() {
        List<Dataset> datasets = datasetService.findAll();
        for (Dataset dataset : datasets) {
            Dataset freshDataset = createAndSaveNewDataset();
            assertFalse(dataset.equals(freshDataset));
            assertFalse(dataset.hashCode() == freshDataset.hashCode());
            freshDataset = new Dataset();
            freshDataset.setTitle("fresh dataset");
            assertFalse(dataset.equals(freshDataset));
            assertFalse(dataset.hashCode() == freshDataset.hashCode());
            freshDataset.setId(dataset.getId());
            assertEquals(dataset, freshDataset);
            assertEquals(dataset.hashCode(), freshDataset.hashCode());
            // sanity check on other subtypes
            for (Class<? extends Resource> resourceSubtype : new Class[] { Ontology.class, Document.class, Image.class, CodingSheet.class, Project.class }) {
                for (Resource r : genericService.findAll(resourceSubtype)) {
                    assertFalse(dataset.equals(r));
                    assertFalse(dataset.hashCode() == r.hashCode());
                }
            }
        }
    }

    // FIXME: not sure where this belongs, it was in DatasetControllerITCase originally
    @Test
    @Rollback
    public void testAuthorizedUserInEquality() {
        //with the equals and hashCode of AuthorizedUser, this is now never going to be true
        AuthorizedUser authorizedUser = new AuthorizedUser(getAdminUser(), GeneralPermissions.VIEW_ALL);
        AuthorizedUser authorizedUser2 = new AuthorizedUser(getAdminUser(), GeneralPermissions.VIEW_ALL);
        assertNotEquals(authorizedUser, authorizedUser2);
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
    //FIXME This test currently fails because it violates the hashCode contract {@link java.lang.Object#hashCode()}.
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
            Person person = new Person();
            person.setFirstName(TestConstants.DEFAULT_FIRST_NAME);
            person.setLastName(TestConstants.DEFAULT_LAST_NAME);
            person.setEmail(emailPrefix + i + TestConstants.DEFAULT_EMAIL);
            assertFalse(persistedPerson.equals(person));
            //setting the ID should now make this person 'equal' to one of the person objects in the set,  per our definition of equality
            person.setId(persistedPerson.getId());
            
            //now that we set the id, it's safe to lock down the hashcode
            int hashcode1 = person.hashCode();
            logger.debug("locking down person.hashCode() to: {}", hashcode1);

            //hashcode should be locked down, changing the id should not effect hashcode
            person.setId(person.getId()  + 15L);
            assertEquals(hashcode1, person.hashCode());
            
            //okay now set ID back to original value
            person.setId(person.getId() - 15L);
            
            assertEquals(persistedPerson, person);
            if(!personSet.contains(person)) {
                logger.error("hashset.contains() should be true for {} but was false", person); 
                int[] hashcodes = new int[personSet.size()];
                int j= 0;
                for(Person peep : personSet) {
                    hashcodes[j++] = peep.hashCode();
                }
                logger.error("personset hashcodes: {}", hashcodes);
                logger.error("    person hashcode: {}", person.hashCode());
                
                String expectation = "If two objects are equal according to the equals(Object) method, then calling the hashCode method on each of the two " +
                        "objects must produce the same integer result.";
                assertEquals(expectation, persistedPerson, person);
                assertEquals(expectation, persistedPerson.hashCode(), person.hashCode());
            }
            //assertTrue(personSet + " should contain " + person, personSet.contains(person));
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
    //this test is a bit more academic, but is another examlple of where our hashcode/equals implementation fails.
    //per java docs: if A == B is true, and B==C is true, then A == C should be true
    public void testPersonTransitiveEquality() {
        Person a = new Person("Loblaw", "Bob", "bob.loblaw@compuserve.net");
        Person b = new Person("Loblaw", "Bob", "bob.loblaw@compuserve.net");
        Person c = new Person("Loblaw", "Bob", "bob.loblaw@compuserve.net");
        genericService.save(b);
        assertEquals("a should equal b", a, b);
        assertEquals("b should equal c", b, c);
        assertEquals("a should equal c", a, c);
    }

    @Test
    public void twoUniqueAnnotations() {

        ResourceAnnotation a1 = new ResourceAnnotation();
        ResourceAnnotation a2 = new ResourceAnnotation();
        ResourceAnnotationKey k1 = new ResourceAnnotationKey();
        ResourceAnnotationKey k2 = new ResourceAnnotationKey();
        String sameValue = "a value";

        k1.setKey("key1");
        k2.setKey("key2");
        a1.setResourceAnnotationKey(k1);
        a1.setValue(sameValue);
        a2.setResourceAnnotationKey(k2);
        a2.setValue(sameValue);

        Assert.assertNotSame("these keys should have different hashcodes", k1.hashCode(), k2.hashCode());
        // set the id to be the same, they should *still* have different hashcodes
        Long id = -1L;
        k1.setId(id);
        k2.setId(id);
        Assert.assertNotSame("these keys should have different hashcodes", k1.hashCode(), k2.hashCode());
        Assert.assertFalse(k1.equals(k2));
        Assert.assertFalse(a1.equals(a2));
        Assert.assertNotSame("these annotations should have different hashcodes", a1.hashCode(), a2.hashCode());

        a1.setId(id);
        a2.setId(id);
        Assert.assertNotSame("these annotations should have different hashcodes", a1.hashCode(), a2.hashCode());
        Assert.assertFalse(a1.equals(a2));

        // okay, put these in a set and make sure the set has two items
        Set<ResourceAnnotation> set = new HashSet<ResourceAnnotation>();
        set.add(a1);
        set.add(a2);
        Assert.assertEquals("set should have two items in it", 2, set.size());
    }

}
