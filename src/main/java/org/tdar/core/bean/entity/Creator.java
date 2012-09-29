package org.tdar.core.bean.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.tdar.core.bean.JsonModel;
import org.tdar.core.bean.Persistable;
import org.tdar.index.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.index.NonTokenizingLowercaseKeywordAnalyzer;

/**
 * $Id$
 * 
 * Base class for Persons and Institutions that can be assigned as a
 * ResourceCreator.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Table(name = "creator")
@Inheritance(strategy = InheritanceType.JOINED)
@XmlSeeAlso({ Person.class, Institution.class })
@XmlAccessorType(XmlAccessType.PROPERTY)
public abstract class Creator extends JsonModel.Base implements Persistable {

    private static final long serialVersionUID = 2296217124845743224L;

    public enum CreatorType {
        PERSON, INSTITUTION;
        public static CreatorType valueOf(Class<? extends Creator> cls) {
            if (cls.equals(Person.class)) {
                return CreatorType.PERSON;
            } else if (cls.equals(Institution.class)) {
                return CreatorType.INSTITUTION;
            }
            return null;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @DocumentId
    @Field
    @Analyzer(impl = KeywordAnalyzer.class)
    private Long id = -1L;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_created")
    private Date dateCreated;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    private Date lastUpdated;

    public Creator() {
        setDateCreated(new Date());
    }

    @Column(length = 64)
    private String url;

    private String location;

    @Fields({ @Field(name = "name", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
            @Field(name = "name_kwd", analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)) })
    public abstract String getName();

    @Fields({ @Field(name = "properName", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)) })
    public abstract String getProperName();

    @Fields({ @Field(name = "institution", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
            @Field(name = "institution_kwd", analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)) })
    public String getInstitutionName() {
        if (getCreatorType() == CreatorType.PERSON) {
            Person person = ((Person) this);
            if (person.getInstitution() != null) {
                return person.getInstitution().getName();
            }
        }
        return null;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public abstract CreatorType getCreatorType();

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }
        if (candidate instanceof Creator && getClass().isInstance(candidate)) {
            return Persistable.Base.isEqual(this, getClass().cast(candidate));
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (Persistable.Base.isTransient(this)) {
            return super.hashCode();
        }
        return Persistable.Base.toHashCode(this);
    }
}
