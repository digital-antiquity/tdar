package org.tdar.core.bean.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * $Id$
 * 
 * Basic person class within tDAR. Can represent a registered user or a person
 * (with identifying email) entered as a contact or attributed reference, etc.
 * 
 * @author Allen Lee
 * @version $Revision$
 */
@Entity
@Table(name = "person")
@Indexed(index = "Person")
@XmlRootElement(name = "person")
public class Person extends Creator implements Comparable<Person> {

    private static final long serialVersionUID = -3863573773250268081L;

    @Transient
    private final static String[] JSON_PROPERTIES = { "id", "firstName", "lastName", "institution", "email", "name", "properName", "fullName" };

    public Person() {
    }

    public Person(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Column(nullable = false, name = "last_name")
    @Field(name = "lastName")
    @BulkImportField(label="Last Name",comment=BulkImportField.CREATOR_LNAME_DESCRIPTION,order =2)
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private String lastName;

    @Column(nullable = false, name = "first_name")
    @Field(name = "firstName")
    @BulkImportField(label="First Name",comment=BulkImportField.CREATOR_FNAME_DESCRIPTION,order=1)
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private String firstName;

    @Column(unique = true, nullable = true)
    @Field(name = "email")
    @BulkImportField(label="Email",order=3)
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private String email;

    @IndexedEmbedded(depth = 1)
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @BulkImportField(label="Resource Creator's ",comment=BulkImportField.CREATOR_PERSON_INSTITUTION_DESCRIPTION, order=50)
    private Institution institution;

    // can this user contribute resources?
    private boolean contributor;

    @Column(name = "contributor_reason", length = 512)
    private String contributorReason;

    // can this user access confidential resources?
    private boolean privileged;

    // did this user register with the system or were they entered by someone
    // else?
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private boolean registered;

    // private boolean deleted;

    // is this person a registered professional archaeologist? See
    // http://www.rpanet.org for more info
    private boolean rpa;

    // rpanet.org number (if applicable - using String since I'm not sure if
    // it's in numeric format)
    @Column(name = "rpa_number")
    private String rpaNumber;

    private String phone;

    private String password;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")
    @XStreamOmitField
    private Set<BookmarkedResource> bookmarkedResources;

    /**
     * Returns the person's name in [last name, first name] format.
     * 
     * @return formatted String name
     */
    @Transient
    public String getName() {
        return lastName + ", " + firstName;
    }

    @Transient
    public String getProperName() {
        return firstName + " " + lastName;
    }

    public void setName(String name) {
        String[] names = Person.split(name);
        if (names.length == 0) {
            return;
        }
        setLastName(names[0]);
        setFirstName(names[1]);
    }

    /**
     * FIXME: Only handles names in the form "FirstName LastName". So a name
     * like "Colin McGregor McCoy" would have "Colin" as the first name and
     * "McGregor McCoy" as the last name
     * 
     * @param name
     *            a full name in 'FirstName LastName' or 'LastName, FirstName'
     *            format.
     * @return String array of length 2 - [LastName, FirstName]
     */
    public static String[] split(String name) {
        String firstName = "";
        String lastName = "";
        int splitIndex = name.indexOf(',');
        if (splitIndex == -1) {
            splitIndex = name.indexOf(' ');
            if (splitIndex == -1) {
                // give up, warn?
                return new String[0];
            }
            firstName = name.substring(0, splitIndex);
            lastName = name.substring(splitIndex + 1, name.length());
        } else {
            lastName = name.substring(0, splitIndex);
            firstName = name.substring(splitIndex + 1, name.length());
        }
        return new String[] { lastName, firstName };
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null)
            return;
        this.lastName = lastName.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null)
            return;
        this.firstName = firstName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (StringUtils.isBlank(email)) {
            this.email = null;
        } else {
            this.email = email.toLowerCase();
        }
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String toString() {
        if (institution != null && !StringUtils.isBlank(institution.toString())) {
            return String.format("%s [%s | %s]", getName(), email, institution);
        }
        return String.format("%s [%s]", getName(), "No institution specified.");
    }

    /**
     * Compares by last name, first name, and then finally email.
     */
    public int compareTo(Person otherPerson) {
        if (this == otherPerson)
            return 0;
        int comparison = lastName.compareTo(otherPerson.lastName);
        if (comparison == 0) {
            comparison = firstName.compareTo(otherPerson.firstName);
            if (comparison == 0) {
                // last straw is email
                comparison = email.compareTo(otherPerson.email);
            }
        }
        return comparison;
    }

    public boolean isContributor() {
        return contributor;
    }

    public void setContributor(boolean contributor) {
        this.contributor = contributor;
    }

    @XmlTransient
    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isRpa() {
        return rpa;
    }

    public void setRpa(boolean rpa) {
        this.rpa = rpa;
    }

    public String getRpaNumber() {
        return rpaNumber;
    }

    public void setRpaNumber(String rpaNumber) {
        this.rpaNumber = rpaNumber;
    }

    @XmlTransient
    public String getContributorReason() {
        return contributorReason;
    }

    public void setContributorReason(String contributorReason) {
        this.contributorReason = contributorReason;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @XmlTransient
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<?> getEqualityFields() {
        return Arrays.asList(email);
    }

    @XmlTransient
    public Set<BookmarkedResource> getBookmarkedResources() {
        return bookmarkedResources;
    }

    public void setBookmarkedResources(Set<BookmarkedResource> bookmarkedResources) {
        this.bookmarkedResources = bookmarkedResources;
    }

    @Override
    public String[] getIncludedJsonProperties() {
        return JSON_PROPERTIES;
    }

    @Override
    public CreatorType getCreatorType() {
        return CreatorType.PERSON;
    }
    
    @XmlID
    @Transient
    public String getXmlId() {
        return getId().toString();
    }
    
    @Transient
    public String getInstitutionName() {
        String name = null;
        if(institution != null) {
            name = institution.getName();
        }
        return name;
    }
}
