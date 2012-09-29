package org.tdar.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

public class SimpleSerializerTest {
  private transient Logger log = Logger.getLogger(SimpleSerializerTest.class);

  class Person {
    private String firstName;
    private String lastName;
    private Integer age;
    private transient String dontSerializeMe;

    public Person() {
    }

    public Person(String first, String last, Integer age) {
      firstName = first;
      lastName = last;
      this.age = age;
      dontSerializeMe = "dsm:" + firstName + " " + lastName;
    }

    public String getFirstName() {
      return firstName.toUpperCase();
    }

    public String getLastName() {
      return lastName.toUpperCase();
    }

    public Integer getAge() {
      return age;
    }

    public String getDontSerializeMe() {
      return dontSerializeMe;
    }

  }

  class Athlete extends Person {
    private String sport;

    public Athlete() {
      super();
    }

    public Athlete(String first, String last, Integer age, String sport) {
      super(first, last, age);
      this.sport = sport;
    }

    public String getSport() {
      return sport.toUpperCase();
    }
  }

  private List<Person> getPeople() {
    List<Person> people = new ArrayList<Person>();
    people.add(new Person("Jim", "deVos", 36));
    people.add(new Person("Kelly", "deVos", 36));
    people.add(new Athlete("Manute", "Bol", 47, "Basketball"));
    return people;
  }

  @Test
  public void testXmlCreated() {
    List<Person> people = getPeople();
    assertNotNull(people);

    SimpleSerializer ss = new SimpleSerializer();
    String xml = ss.toXml(people);
    log.debug(xml);
    assertTrue("non-empty string returned", xml.length() > 0);
  }

  @Test
  public void testWhiteListWorking() {
    List<Person> people = getPeople();
    SimpleSerializer ss = new SimpleSerializer();
    ss.addToWhitelist(Athlete.class, "sport");
    ss.addToWhitelist(Person.class, "firstName");
    String xml = ss.toXml(people);
    log.info(xml);
    assertTrue("sport tag is whitelisted and should appear in document", xml.indexOf("sport") >= 0);
    assertTrue("firstName tag is whitelisted and should appear in document", xml.indexOf("firstName") >= 0);
    assertFalse("lastname not in whitelist and should appear in document", xml.indexOf("lastName") >= 0);
  }

  @Test
  public void testAccessViaGetters() {
    List<Person> people = getPeople();
    SimpleSerializer ss = new SimpleSerializer();
    ss.addToWhitelist(Athlete.class, "sport");
    ss.addToWhitelist(Person.class, "firstName");
    String xml = ss.toXml(people);
    log.info(xml);
    // assertTrue("sport tag is whitelisted and should appear in document",
    // xml.indexOf("sport")>=0);
    // assertTrue("firstName tag is whitelisted and should appear in document",
    // xml.indexOf("firstName")>=0);
    // assertFalse("lastname not in whitelist and should appear in document",
    // xml.indexOf("lastName")>=0);

  }

}
