package org.tdar.core.bean.entity;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Cacheable;
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.URLConstants;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasImage;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonView;

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
@XmlSeeAlso({ Person.class, Institution.class, TdarUser.class })
@XmlAccessorType(XmlAccessType.PROPERTY)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.entity.Creator")
public abstract class Creator<T extends Creator<?>> implements Persistable, HasName, HasStatus, Indexable, Updatable, OaiDcProvider,
        Obfuscatable, Validatable, Addressable, XmlLoggable, HasImage,Slugable, HasEmail,HasLabel {

    protected final static transient Logger logger = LoggerFactory.getLogger(Creator.class);
    private transient boolean obfuscated;
    private transient Boolean obfuscatedObjectDifferent;
    public static final String OCCURRENCE = "occurrence";
    public static final String BROWSE_OCCURRENCE = "browse_occurrence";

    @Override
    public Boolean getObfuscatedObjectDifferent() {
        return obfuscatedObjectDifferent;
    }

    @Override
    public void setObfuscatedObjectDifferent(Boolean obfuscatedObjectDifferent) {
        this.obfuscatedObjectDifferent = obfuscatedObjectDifferent;
    }

    private static final long serialVersionUID = 2296217124845743224L;

    public enum CreatorType {
        PERSON("P"), INSTITUTION("I");
        private String code;

        private CreatorType(String code) {
            this.code = code;
        }

        public static CreatorType valueOf(Class<? extends Creator<?>> cls) {
            if (cls.equals(Person.class)) {
                return CreatorType.PERSON;
            } else if (cls.equals(Institution.class)) {
                return CreatorType.INSTITUTION;
            }
            return null;
        }

        public Class<? extends Creator<?>> getImplementedClass() {
            switch (this) {
                case INSTITUTION:
                    return Institution.class;
                case PERSON:
                    return Person.class;
            }
            return null;
        }
        
        public String getCode() {
            return this.code;
        }

        public boolean isPerson() {
            return this == PERSON;
        }

        public boolean isInstitution() {
            return this == INSTITUTION;
        }

    }

    @Column(nullable = false, name = "hidden", columnDefinition = "boolean default FALSE")
    private boolean hidden = false;

    @Column(name = Creator.OCCURRENCE)
    private Long occurrence = 0L;
    @Column(name = Creator.BROWSE_OCCURRENCE)
    private Long browseOccurrence = 0L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(JsonLookupFilter.class)
    private Long id = -1L;
    /*
     * @Boost(.5f)
     * 
     * //@IndexedEmbedded
     * 
     * @ManyToOne()
     * 
     * @JoinColumn(name = "updater_id")
     * private Person updatedBy;
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_updated", nullable = false)
    @NotNull
    private Date dateUpdated;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonView(JsonLookupFilter.class)
    @Column(name = "date_created", nullable=false)
    @NotNull
    private Date dateCreated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = FieldLength.FIELD_LENGTH_25)
    @JsonView(JsonLookupFilter.class)
    private Status status = Status.ACTIVE;

    @OneToMany(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, targetEntity=Creator.class, orphanRemoval=true)
    @JoinColumn(name = "merge_creator_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<T> synonyms = new HashSet<>();

    
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    public Creator() {
        setDateCreated(new Date());
        setDateUpdated(new Date());
    }

    @Column(length = FieldLength.FIELD_LENGTH_255)
    @Length(max = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private String url;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = true, name = "creator_id")
    @NotNull
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<Address> addresses = new LinkedHashSet<>();

    private transient Integer maxHeight;
    private transient Integer maxWidth;
    private transient VersionType maxSize;

    // @OneToMany(cascade = CascadeType.ALL, mappedBy = "creator", fetch = FetchType.LAZY, orphanRemoval = true)
    // private Set<ResourceCreator> resourceCreators = new LinkedHashSet<ResourceCreator>();

    @Override
    @JsonView(JsonLookupFilter.class)
    public abstract String getName();

    
    
    public String getLabel() {
        return getName();
    }
    @JsonView(JsonLookupFilter.class)
    public abstract String getProperName();

    @JsonView(JsonLookupFilter.class)
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

    public abstract CreatorType getCreatorType();

    @Override
    public boolean equals(Object candidate) {
        if (candidate == null || !(candidate instanceof Creator)) {
            return false;
        }
        try {
            return PersistableUtils.isEqual(this, Creator.class.cast(candidate));
        } catch (ClassCastException e) {
            logger.debug("cannot cast creator: ", e);
            return false;
        }
    }

    // private transient int hashCode = -1;

    /*
     * copied from PersistableUtils.hashCode() (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        Logger logger = LoggerFactory.getLogger(getClass());
        int hashCode = -1;
        if (PersistableUtils.isNullOrTransient(this)) {
            hashCode = super.hashCode();
        } else {
            hashCode = PersistableUtils.toHashCode(this);
        }

        Object[] obj = { hashCode, getClass().getSimpleName(), getId() };
        logger.trace("setting hashCode to {} ({}) {}", obj);
        return hashCode;
    }

    @Override
    @XmlTransient
    public List<?> getEqualityFields() {
        return Collections.emptyList();
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = StringUtils.trimToEmpty(description);
    }

    /**
     * @return the description
     */
    @Override
    public String getDescription() {
        return description;
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
    public void markUpdated(TdarUser p) {
        // setUpdatedBy(p);
        setDateUpdated(new Date());
    }

    @Override
    @XmlAttribute
    public Status getStatus() {
        return this.status;
    }

    @XmlElementWrapper(name = "synonyms")
    @XmlElement(name = "synonymRef")
    public Set<T> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<T> synonyms) {
        this.synonyms = synonyms;
    }


    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
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

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    @Override
    @XmlTransient
    public boolean isObfuscated() {
        return obfuscated;
    }

    @Override
    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    @Override
    @JsonView(JsonLookupFilter.class)
    public String getUrlNamespace() {
        return URLConstants.ENTITY_NAMESPACE;
    }

    public abstract boolean hasNoPersistableValues();

    public Set<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }

    public Long getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Long occurrence) {
        this.occurrence = occurrence;
    }

    @XmlTransient
    public Long getBrowseOccurrence() {
        return browseOccurrence;
    }

    public void setBrowseOccurrence(Long browse_occurrence) {
        this.browseOccurrence = browse_occurrence;
    }

    @XmlTransient
    public boolean isBrowsePageVisible() {
        if (hidden || getCreatorType() == null || getBrowseOccurrence() == null) {
            return false;
        }
        if (getCreatorType().isInstitution()) {
            return true;
        }
        return getBrowseOccurrence() > 1;
    }

    @XmlTransient
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @JsonView(JsonLookupFilter.class)
    public String getDetailUrl() {
        return String.format("/%s/%s/%s", getUrlNamespace(), getId(), getSlug());
    }

    @Override
    public String getSlug() {
        return UrlUtils.slugify(getProperName());
    }


    @Override
    public Integer getMaxHeight() {
        return maxHeight;
    }

    @Override
    public void setMaxHeight(Integer maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    public Integer getMaxWidth() {
        return maxWidth;
    }

    @Override
    public void setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
    }

    @Override
    public VersionType getMaxSize() {
        return maxSize;
    }

    @Override
    public void setMaxSize(VersionType maxSize) {
        this.maxSize = maxSize;
    }


    @Override
    @Transient
    @XmlTransient
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isDraft() {
        return status == Status.DRAFT;
    }

    @Override
    public boolean isDuplicate() {
        return status == Status.DUPLICATE;
    }

    @Override
    @Transient
    @XmlTransient
    public boolean isFlagged() {
        return status == Status.FLAGGED;
    }

}
