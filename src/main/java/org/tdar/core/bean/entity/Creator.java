package org.tdar.core.bean.entity;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.search.Explanation;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.JsonModel;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.query.QueryFieldNames;

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
public abstract class Creator extends JsonModel.Base implements Persistable, HasName, HasStatus, Indexable, Updatable, OaiDcProvider,
        Obfuscatable,
        Addressable {

    private transient boolean obfuscated;

    private static final long serialVersionUID = 2296217124845743224L;

    public enum CreatorType {
        PERSON("P"), INSTITUTION("I");
        private String code;

        private CreatorType(String code) {
            this.code = code;
        }

        public static CreatorType valueOf(Class<? extends Creator> cls) {
            if (cls.equals(Person.class)) {
                return CreatorType.PERSON;
            } else if (cls.equals(Institution.class)) {
                return CreatorType.INSTITUTION;
            }
            return null;
        }

        public String getCode() {
            return this.code;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @DocumentId
    @Field
    @Analyzer(impl = KeywordAnalyzer.class)
    private Long id = -1L;
    /*
     * @Boost(.5f)
     * 
     * @IndexedEmbedded
     * 
     * @ManyToOne()
     * 
     * @JoinColumn(name = "updater_id")
     * private Person updatedBy;
     */
    @Field(norms = Norms.NO, store = Store.YES)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated", nullable = true)
    @DateBridge(resolution = Resolution.MILLISECOND)
    private Date dateUpdated;

    @Temporal(TemporalType.DATE)
    @Column(name = "date_created")
    private Date dateCreated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Field(norms = Norms.NO, store = Store.YES)
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    private Status status = Status.ACTIVE;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    public Creator() {
        setDateCreated(new Date());
        setDateUpdated(new Date());
    }

    @Column(length = 64)
    private String url;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "creator_id")
    @NotNull
    private Set<Address> addresses = new LinkedHashSet<Address>();

    private String location;

    private transient Float score = -1f;
    private transient Explanation explanation;
    private transient boolean readyToIndex = true;

    // @OneToMany(cascade = CascadeType.ALL, mappedBy = "creator", fetch = FetchType.LAZY, orphanRemoval = true)
    // private Set<ResourceCreator> resourceCreators = new LinkedHashSet<ResourceCreator>();

    @Fields({ @Field(name = "name", analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)),
            @Field(name = "name_kwd", analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)),
            @Field(name = QueryFieldNames.CREATOR_NAME_SORT, norms = Norms.NO, store = Store.YES) })
    public abstract String getName();

    @Fields({ @Field(name = QueryFieldNames.PROPER_NAME, analyzer = @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)) })
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
    @XmlAttribute
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
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
        if (this == null || candidate == null) {
            return false;
        }
        try {
            return Persistable.Base.isEqual(this, Creator.class.cast(candidate));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (Persistable.Base.isTransient(this)) {
            return super.hashCode();
        }
        return Persistable.Base.toHashCode(this);
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    @Transient
    @XmlTransient
    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    @Transient
    @XmlTransient
    public Explanation getExplanation() {
        return explanation;
    }

    public void setExplanation(Explanation explanation) {
        this.explanation = explanation;
    }

    @Transient
    @XmlTransient
    public boolean isDedupable() {
        return true;
    }

    public String getSynonymFormattedName() {
        return getProperName();
    }

    @Override
    public void markUpdated(Person p) {
        // setUpdatedBy(p);
        setDateUpdated(new Date());
    }

    @XmlAttribute
    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == Status.DELETED;
    }

    /*
     * @XmlIDREF
     * 
     * @XmlAttribute(name = "updaterId")
     * public Person getUpdatedBy() {
     * return updatedBy;
     * }
     * 
     * public void setUpdatedBy(Person updatedBy) {
     * this.updatedBy = updatedBy;
     * }
     */
    @XmlTransient
    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public String getTitle() {
        return getProperName();
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    @XmlTransient
    @JSONTransient
    public boolean isObfuscated() {
        return obfuscated;
    }

    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    public String getUrlNamespace() {
        return "browse/creators";
    }

    public abstract boolean hasNoPersistableValues();

    @Transient
    @XmlTransient
    public boolean isReadyToIndex() {
        return readyToIndex;
    }

    public void setReadyToIndex(boolean readyToIndex) {
        this.readyToIndex = readyToIndex;
    }

    public Set<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }

}
