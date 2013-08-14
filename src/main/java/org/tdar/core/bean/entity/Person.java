package org.tdar.core.bean.entity;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.query.QueryFieldNames;

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
public class Person extends Creator implements Comparable<Person>, Dedupable<Person>, Validatable {

    @Transient
    private static final String[] IGNORE_PROPERTIES_FOR_UNIQUENESS = { "id", "institution", "dateCreated", "dateUpdated", "registered",
            "contributor", "totalLogins", "lastLogin", "penultimateLogin", "emailPublic", "phonePublic", "status", "synonyms", "occurrence",
            "proxyInstitution", "proxyNote" };

    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "merge_creator_id")
    private Set<Person> synonyms = new HashSet<Person>();

    private static final long serialVersionUID = -3863573773250268081L;

    @Transient
    private final static String[] JSON_PROPERTIES = { "id", "firstName", "lastName", "institution", "email", "name", "properName", "fullName", "registered","tempDisplayName" };

    @Transient
    private transient String tempDisplayName;
    
    public Person() {
    }

    public Person(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    private transient String wildcardName;

    @Column(nullable = false, name = "last_name")
    @BulkImportField(label = "Last Name", comment = BulkImportField.CREATOR_LNAME_DESCRIPTION, order = 2)
    @Fields({ @Field(name = QueryFieldNames.LAST_NAME, analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
            @Field(name = QueryFieldNames.LAST_NAME_SORT, norms = Norms.NO, store = Store.YES) })
    @Length(max = 255)
    private String lastName;

    @Column(nullable = false, name = "first_name")
    @BulkImportField(label = "First Name", comment = BulkImportField.CREATOR_FNAME_DESCRIPTION, order = 1)
    @Fields({ @Field(name = QueryFieldNames.FIRST_NAME, analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
            @Field(name = QueryFieldNames.FIRST_NAME_SORT, norms = Norms.NO, store = Store.YES) })
    @Length(max = 255)
    private String firstName;

    @Column(unique = true, nullable = true)
    @Field(name = "email", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class))
    @BulkImportField(label = "Email", order = 3)
    @Length(max = 255)
    private String email;

    @Column(unique = true, nullable = true)
    @Length(max = 255)
    private String username;

    @Column(nullable = false, name = "email_public", columnDefinition = "boolean default FALSE")
    private Boolean emailPublic = Boolean.FALSE;

    @IndexedEmbedded(depth = 1)
    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE ,CascadeType.DETACH}, optional = true)
    // FIXME: this causes PersonController to throw non-unique key violations again when changing from one persistent Institution
    // to another persistent Institution. WHY
    // @Cascade({org.hibernate.annotations.CascadeType.SAVE_UPDATE})
    @BulkImportField(label = "Resource Creator's ", comment = BulkImportField.CREATOR_PERSON_INSTITUTION_DESCRIPTION, order = 50)
    private Institution institution;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE ,CascadeType.DETACH}, optional = true)
    /* who to contact when owner is no longer 'reachable' */
    private Institution proxyInstitution;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "proxy_note")
    private String proxyNote;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_login")
    private Date lastLogin;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "penultimate_login")
    private Date penultimateLogin;

    @Column(name = "total_login")
    private Long totalLogins = 0l;

    // can this user contribute resources?
    @Column(name = "contributor", nullable = false, columnDefinition = "boolean default FALSE")
    private Boolean contributor = Boolean.FALSE;

    @Column(name = "contributor_reason", length = 512)
    @Length(max = 512)
    private String contributorReason;

    // did this user register with the system or were they entered by someone
    // else?
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private boolean registered = false;

    // rpanet.org number (if applicable - using String since I'm not sure if
    // it's in numeric format)
    @Column(name = "rpa_number")
    @Length(max = 255)
    private String rpaNumber;

    @Length(max = 255)
    private String phone;

    @Column(nullable = false, name = "phone_public", columnDefinition = "boolean default FALSE")
    private Boolean phonePublic = Boolean.FALSE;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person")
    private Set<BookmarkedResource> bookmarkedResources;

    /**
     * Returns the person's name in [last name, first name] format.
     * 
     * @return formatted String name
     */
    @Override
    @Transient
    public String getName() {
        return lastName + ", " + firstName;
    }

    @Override
    @Transient
    public String getProperName() {
        return firstName + " " + lastName;
    }


    /**
     * set the user firstname, lastname from string in "last first" format.  anything other than simple
     * two word string is ignored.
     * @param properName
     */
    public void setName(String name) {
        String[] names = Person.split(name);
        if (names.length == 2) {
            setLastName(names[0]);
            setFirstName(names[1]);
        }
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

    public Boolean getEmailPublic() {
        return emailPublic;

    }

    public void setEmailPublic(Boolean toggle) {
        this.emailPublic = toggle;
    }

    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    public String toString() {
        if (institution != null && !StringUtils.isBlank(institution.toString())) {
            return String.format("%s [%s | %s | %s]", getName(), getId(), email, institution);
        }
        return String.format("%s [%s | %s]", getName(), getId(), "No institution specified.");
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

    public Boolean getContributor() {
        return contributor;
    }

    public void setContributor(Boolean contributor) {
        this.contributor = contributor;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
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

    public Boolean getPhonePublic() {
        return phonePublic;
    }

    public void setPhonePublic(Boolean toggle) {
        this.phonePublic = toggle;
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

    @Transient
    public String getInstitutionName() {
        String name = null;
        if (institution != null) {
            name = institution.getName();
        }
        return name;
    }

    @Override
    public boolean isDedupable() {
        return !isRegistered();
    }

    public Long getTotalLogins() {
        if (totalLogins == null) {
            return 0L;
        }
        return totalLogins;
    }

    public void setTotalLogins(Long totalLogins) {
        this.totalLogins = totalLogins;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.penultimateLogin = this.lastLogin;
        this.lastLogin = lastLogin;
    }

    public void incrementLoginCount() {
        totalLogins = getTotalLogins() + 1;
    }

    public Date getPenultimateLogin() {
        return penultimateLogin;
    }

    public void setPenultimateLogin(Date penultimateLogin) {
        this.penultimateLogin = penultimateLogin;
    }

    public static String[] getIgnorePropertiesForUniqueness() {
        return IGNORE_PROPERTIES_FOR_UNIQUENESS;
    }

    public List<Obfuscatable> obfuscate() {
        setObfuscated(true);
        // check if email and phone are actually confidential
        if (!getEmailPublic()) {
            setEmail(null);
        }
        if (!getPhonePublic()) {
            setPhone(null);
        }
        setRegistered(false);
        setContributor(false);
        setRpaNumber(null);
        setLastLogin(null);
        setPenultimateLogin(null);
        setTotalLogins(null);
        return Arrays.asList((Obfuscatable) getInstitution());
    }

    @Override
    public boolean isValidForController() {
        return StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName);
    }

    @Transient
    @Override
    public boolean hasNoPersistableValues() {
        if (StringUtils.isBlank(email) && (institution == null || StringUtils.isBlank(institution.getName())) && StringUtils.isBlank(lastName) &&
                StringUtils.isBlank(firstName) && Persistable.Base.isNullOrTransient(getId())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isValid() {
        return isValidForController() && getId() != null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Person> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<Person> synonyms) {
        this.synonyms = synonyms;
    }

    @Transient
    @XmlTransient
    public String getWildcardName() {
        return wildcardName;
    }

    public void setWildcardName(String wildcardName) {
        this.wildcardName = wildcardName;
    }

    @Field(norms = Norms.NO, store = Store.YES)
    @DateBridge(resolution = Resolution.MILLISECOND)
    public Date getDateUpdated() {
        return super.getDateUpdated();
    }

    public Institution getProxyInstitution() {
        return proxyInstitution;
    }

    public void setProxyInstitution(Institution proxyInstitution) {
        this.proxyInstitution = proxyInstitution;
    }

    public String getProxyNote() {
        return proxyNote;
    }

    public void setProxyNote(String proxyNote) {
        this.proxyNote = proxyNote;
    }

    /* convenience for struts in case of error on INPUT, better than "NULL NULL" */
    public String getTempDisplayName() {
        if (StringUtils.isBlank(tempDisplayName) && StringUtils.isNotBlank(getProperName())) {
            setTempDisplayName(getProperName());
        }
        return tempDisplayName;
    }

    public void setTempDisplayName(String tempName) {
        this.tempDisplayName = tempName;
    }
}
