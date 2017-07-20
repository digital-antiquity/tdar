package org.tdar.experimental;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * this test is an expermient to see if we can use a treeset to weed out dupes using an a Comparartor that is *not* consistent with equals.
 * 
 * @author jimdevos
 * 
 */
public class TreeSetTest {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    // Not a tdar person. just a simple person class for use in this test.
    class Person {
        int id;
        String lastName;
        String firstName;

        public Person(int id, String first, String last) {
            this.id = id;
            lastName = last;
            firstName = first;
        }

        @Override
        // this is just a simple test: don't be a jerk and compare me to some other class type
        public boolean equals(Object that) {
            if (that == null) {
                return false;
            }
            return this.id == ((Person) that).id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return String.format("%s, %s (%s)", lastName, firstName, id);
        }
    }

    @SuppressWarnings("static-method")
    public TreeSet<Person> createTreeset(Comparator<Person> comparator) {
        return null;
    }

    // consistent: uses same field to sort that is used in equals()
    Comparator<Person> compareId = new Comparator<Person>() {
        @Override
        public int compare(Person o1, Person o2) {
            return o1.id - o2.id;
        }
    };

    // inconsistent, uses last name field. two equal items may not yeild compareTo value of 0 (and vice versa)
    Comparator<Person> compareLastName = new Comparator<Person>() {
        @Override
        public int compare(Person p1, Person p2) {
            return p1.lastName.compareTo(p2.lastName);
        }
    };

    public void logPeople(String title, Set<Person> people) {
        logger.debug(" **************" + title + "************");
        for (Person p : people) {
            logger.trace("person: {}", p);
        }
    }

    @Test
    public void comparitorFlattensUnequalItems() {
        // yes, yes... it's a bad idea to use a comparitor that isn't consistent with equals... but can we use it *at all*?

        Set<Person> set1 = new TreeSet<>(compareId);
        Set<Person> set2 = new TreeSet<>(compareLastName);

        // we know how this set will be distinct off of id
        Set<Person> set3 = new HashSet<>();

        for (int i = 25; i > 0; i--) {
            Person bob = new Person(i, "Bob", "Loblaw");
            set1.add(bob);
            set2.add(bob);
            set3.add(bob);
        }

        logPeople("bobs by id", set1);
        logPeople("bobs by lastname", set2);
        logPeople("unsorted bobs", set3);

        Assert.assertEquals("expecting 100 bobs", 25, set1.size());
        Assert.assertEquals("expecting 100 bobs", 25, set3.size());

        Assert.assertEquals("expecting that our inconsistent comparator has created filtered set of a single bob", 1, set2.size());
    }

    // going in the opposite direction, can we get treeset w/ multiple items when we know that they are equal?
    @Test
    public void comparitorConsidersEqualItemsUnique() {
        Set<Person> set1 = new HashSet<>();
        Set<Person> set2 = new TreeSet<>(compareLastName);
        for (int i = 0; i < 10; i++) {
            Person bob = new Person(1, "Bob", "Loblaw" + i);
            set1.add(bob);
            set2.add(bob);
        }

        logPeople("hashset of bobs", set1);
        logPeople("treeset of bobs with inconsistent comparator", set2);

        Assert.assertEquals("hashset should see all bobs w/ same id as equal", 1, set1.size());
        Assert.assertEquals("set with inconsistent comparitor considers equal people to be unique", 10, set2.size());
    }

}
