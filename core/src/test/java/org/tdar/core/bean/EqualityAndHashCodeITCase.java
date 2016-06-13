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
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.utils.PersistableUtils;

public class EqualityAndHashCodeITCase extends AbstractIntegrationTestCase {

    @Test
    @Rollback
    public void testCreatorEquality() {
        Person person = new Person();
        person.setId(10l);
        assertNotEquals(person, null);
        LatitudeLongitudeBox llb = new LatitudeLongitudeBox();
        assertNotEquals(person, llb);
        assertEquals(person, (Creator<?>) person);
        Institution institution = new Institution();
        institution.setId(10l);
        assertNotEquals(person, institution);
    }

    @Test
    public void testSkeletonPersonRetentionInSet() {
        HashSet<Person> personSet = new HashSet<Person>();
        for (int i = 1; i < 3; i++) {
            Person person = new Person();
            Long lng = new Long(i);
            person.setId(lng);
            personSet.add(person);
        }
        logger.info("people: {}", personSet);
        assertEquals(2, personSet.size());
    }

    @Test
    @Rollback(true)
    public void testEqualsHashCode() {
        List<Dataset> datasets = datasetService.findAll();
        for (Dataset dataset : datasets) {
            // for every dataset, create a new one and make sure they're not equal
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
            if (dataset.getClass().equals(Dataset.class)) {
                // only deal with datasets in this test
//                Class<? extends Indexable>[] classes = LookupSource.RESOURCE.getClasses();
//                classes = (Class<? extends Indexable>[]) ArrayUtils.removeElement(classes, Dataset.class);
//                classes = (Class<? extends Indexable>[]) ArrayUtils.removeElement(classes, Resource.class);
//                for (Class<? extends Indexable> subtype : classes) {
//                    Class<? extends Resource> resourceSubtype = (Class<? extends Resource>) subtype;

                    for (Resource r : genericService.findAll(Resource.class)) {
                        if (r.getResourceType().isDataset()) {
                            continue;
                        }
                        assertFalse(dataset.equals(r));
                        assertFalse(dataset.hashCode() == r.hashCode());
                    }
//                }
            }
        }
    }

    // FIXME: not sure where this belongs, it was in DatasetControllerITCase originally
    @Test
    @Rollback
    public void testAuthorizedUserInEquality() {
        // with the equals and hashCode of AuthorizedUser, this is now never going to be true
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

            // person equality based on db identity. so the two person records should not be equal
            TdarUser person = new TdarUser();
            person.setEmail(persistedPerson.getEmail());
            // person.setRegistered(persistedPerson.isRegistered());
            person.setContributor(true);
            person.setLastName(persistedPerson.getLastName());
            person.setFirstName(persistedPerson.getFirstName());
            person.setPhone(persistedPerson.getPhone());
            assertNotEquals(persistedPerson, person);

            // the person record is 'transient'.
            assertTrue(PersistableUtils.isTransient(person));
            // if we simulate a save by giving it an ID, they are unequal
            person.setId(persistedPerson.getId() + 15L);
            assertNotEquals("these should still be equal even after save", persistedPerson, person);

            // now we set the id's to be the same. so they should be considered 'equal' dispite different field values
            person.setId(persistedPerson.getId());
            assertEquals("these should still be equal even after save", persistedPerson, person);
            assertEquals("therefore hashcodes should be the same", persistedPerson.hashCode(), person.hashCode());

            assertTrue("person should be found in set", personSet.contains(person));
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
        a.setId(b.getId());
        c.setId(b.getId());

        boolean eq = a.equals(b);
        logger.debug("a == b: {}", eq);
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

    @Test
    public void testPersonSet() {
        HashSet<Person> personSet = new HashSet<Person>();
        for (int i = 1; i < 3; i++) {
            Person person = new Person();
            Long lng = new Long(i);
            person.setId(lng);
            personSet.add(person);
        }
        // Person equality will always be based on equalityFields, and so the personset should only contain one instance.
        // changing to work with skeleton model
        assertEquals(2, personSet.size());
        logger.info("{}", personSet);
    }

}
