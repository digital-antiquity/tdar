package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.OrderColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Slugable;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.XmlLoggable;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.ResourceCreatorRoleType;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.SuggestedKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.util.UrlUtils;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.utils.MathUtils;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.converters.JAXBPersistableRef;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.jaxb.converters.JaxbResourceCollectionRefConverter;
import org.tdar.utils.json.JsonIdNameFilter;
import org.tdar.utils.json.JsonIntegrationFilter;
import org.tdar.utils.json.JsonIntegrationSearchResultFilter;
import org.tdar.utils.json.JsonLookupFilter;
import org.tdar.utils.json.JsonProjectLookupFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * $Id$
 * 
 * Contains metadata common to all Resources.
 * 
 * Projects, Datasets, Documents, CodingSheets, Ontologies, SensoryData
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource")
@Table(name = "resource", indexes = {
        @Index(name = "resource_active", columnList = "id, submitter_id, status"),
        @Index(name = "resource_title_index", columnList = "title"),
        @Index(name = "resource_active_draft", columnList = "submitter_id, status, id"),
        @Index(name = "resource_status", columnList = "id, status"),
        @Index(name = "resource_status2", columnList = "status, id"),

        // can't use @Index on entity fields - they have to go here
        @Index(name = "res_submitterid", columnList = "submitter_id"),
        @Index(name = "res_uploaderid", columnList = "uploader_id"),
        @Index(name = "res_updaterid", columnList = "updater_id"),
        @Index(name = "resource_type_index", columnList = "resource_type"),
        @Index(name = "idx_created", columnList = "date_created")
})
@Inheritance(strategy = InheritanceType.JOINED)
@XmlRootElement
@XmlSeeAlso({ Document.class, InformationResource.class, Project.class, CodingSheet.class, Dataset.class, Ontology.class,
        Image.class, SensoryData.class, Video.class, Geospatial.class, Archive.class, Audio.class })
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "resource", propOrder = {})
@XmlTransient
public class Resource implements Persistable,
        Comparable<Resource>, HasName, Updatable, Indexable, Validatable, 
        HasStatus, HasSubmitter, OaiDcProvider, Obfuscatable, ConfidentialViewable, Addressable,
        DeHydratable, XmlLoggable, Slugable {

    private static final long serialVersionUID = -230400285817185637L;

    @Transient
    private transient boolean obfuscated = false;
    @Transient
    private transient boolean bookmarked = false;

    @Transient
    private transient Boolean obfuscatedObjectDifferent = false;

    @Transient
    private transient Boolean statusChanged = Boolean.FALSE;

    @Transient
    private transient boolean viewable;
    @Transient
    private transient boolean viewConfidential;
    @Transient
    private transient Long transientAccessCount;
    protected final static transient Logger logger = LoggerFactory.getLogger(Resource.class);

    public Resource() {
    }

    @Column(name = "total_space_in_bytes")
    private Long spaceInBytesUsed = 0L;

    @Column(name = "total_files")
    private Long filesUsed = 0L;

    private transient Long previousSpaceInBytesUsed = 0L;
    private transient Long previousFilesUsed = 0L;
    private transient boolean countedInBillingEvaluation = true;

    @Deprecated
    public Resource(Long id, String title) {
        setId(id);
        setTitle(title);
    }

    @Deprecated
    public Resource(Long id, String title, ResourceType type) {
        this(id, title);
        setResourceType(type);
    }

    public Resource(Long id, String title, ResourceType resourceType, String description, Status status) {
        this(id, title, resourceType);
        setDescription(description);
        setStatus(status);
    }

    /**
     * Instantiate a "sparse" resource object instance that has a very limited number of populated fields. This is
     * useful in the context of displaying summary information about a collection of resources. You should not
     * attempt to persist objects created using this constructor.
     * 
     * @param id
     * @param title
     * @param resourceType
     * @param status
     * @param submitterId
     */
    public Resource(Long id, String title, ResourceType resourceType, Status status, Long submitterId) {
        this(id, title, resourceType);
        this.status = status;
        TdarUser submitter = new TdarUser();
        submitter.setId(submitterId);
        this.submitter = submitter;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resource_sequence")
    @SequenceGenerator(name = "resource_sequence", allocationSize = 1, sequenceName = "resource_sequence")
    @JsonView(JsonLookupFilter.class)
    private Long id = -1L;

    @BulkImportField(key = "TITLE", required = true, order = -100)
    @NotNull
    @Column(length = 512)
    @JsonView(JsonIdNameFilter.class)
    @Length(max = 512)
    private String title;

    @BulkImportField(required = true, order = -50, key = "DESCRIPTION")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @JsonView(JsonLookupFilter.class)
    private String description;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description_formatted")
    private String formattedDescription;

    
    
    @NotNull
    @Column(name = "date_created", nullable=false)
    @JsonView({ JsonLookupFilter.class, JsonIntegrationSearchResultFilter.class })
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = FieldLength.FIELD_LENGTH_255)
    @JsonView(JsonLookupFilter.class)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = FieldLength.FIELD_LENGTH_50)
    @JsonView(JsonLookupFilter.class)
    private Status status = Status.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = FieldLength.FIELD_LENGTH_50)
    private Status previousStatus = Status.ACTIVE;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = false, name = "submitter_id")
    @NotNull
    private TdarUser submitter;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = false, name = "uploader_id")
    @NotNull
    private TdarUser uploader;

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "updater_id")
    @NotNull
    private TdarUser updatedBy;

    @NotNull
    @Column(name = "date_updated", nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateUpdated;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    @JoinColumn(nullable = false, updatable = false, name = "resource_id")
    @BulkImportField
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.resourceCreators")
    private Set<ResourceCreator> resourceCreators = new LinkedHashSet<ResourceCreator>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    @JoinColumn(nullable = false, updatable = false, name = "resource_id")
    @OrderColumn(name = "id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<ResourceNote> resourceNotes = new LinkedHashSet<ResourceNote>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "resource_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<ResourceAnnotation> resourceAnnotations = new LinkedHashSet<ResourceAnnotation>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "resource_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<SourceCollection> sourceCollections = new LinkedHashSet<SourceCollection>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "resource_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<RelatedComparativeCollection> relatedComparativeCollections = new LinkedHashSet<RelatedComparativeCollection>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "resource_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.latitudeLongitudeBoxes")
    private Set<LatitudeLongitudeBox> latitudeLongitudeBoxes = new LinkedHashSet<LatitudeLongitudeBox>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_geographic_keyword", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false,
            name = "geographic_keyword_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "all", region = "org.tdar.core.bean.resource.Resource.geographicKeywords")
    private Set<GeographicKeyword> geographicKeywords = new LinkedHashSet<GeographicKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_managed_geographic_keyword", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") },
            inverseJoinColumns = { @JoinColumn(nullable = false,
                    name = "geographic_keyword_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "all", region = "org.tdar.core.bean.resource.Resource.managedGeographicKeywords")
    private Set<GeographicKeyword> managedGeographicKeywords = new LinkedHashSet<GeographicKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_temporal_keyword", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false,
            name = "temporal_keyword_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<TemporalKeyword> temporalKeywords = new LinkedHashSet<TemporalKeyword>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(nullable = false, updatable = false, name = "resource_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<CoverageDate> coverageDates = new LinkedHashSet<CoverageDate>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_culture_keyword", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false,
            name = "culture_keyword_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.cultureKeywords")
    private Set<CultureKeyword> cultureKeywords = new LinkedHashSet<CultureKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_other_keyword", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false,
            name = "other_keyword_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<OtherKeyword> otherKeywords = new LinkedHashSet<OtherKeyword>();

    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_site_name_keyword", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false,
            name = "site_name_keyword_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<SiteNameKeyword> siteNameKeywords = new LinkedHashSet<SiteNameKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_material_keyword", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false,
            name = "material_keyword_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<MaterialKeyword> materialKeywords = new LinkedHashSet<MaterialKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_investigation_type", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false,
            name = "investigation_type_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<InvestigationType> investigationTypes = new LinkedHashSet<InvestigationType>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_site_type_keyword", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false,
            name = "site_type_keyword_id") })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<SiteTypeKeyword> siteTypeKeywords = new LinkedHashSet<SiteTypeKeyword>();

    @OneToMany()
    @JoinColumn(name = "resource_id", foreignKey = @javax.persistence.ForeignKey(value = ConstraintMode.NO_CONSTRAINT),nullable=true)
    // see https://hibernate.atlassian.net/browse/HHH-8805 can be removed with Hibernate 5,
//    @ForeignKey(name = "none")
    @XmlTransient
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Set<ResourceRevisionLog> resourceRevisionLog = new HashSet<ResourceRevisionLog>();

    // FIXME: do we really want cascade all here? even delete?
    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinTable(name = "collection_resource", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "collection_id") })
    @XmlTransient
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.resourceCollections")
    private Set<RightsBasedResourceCollection> resourceCollections = new LinkedHashSet<>();

    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinTable(name = "unmanaged_collection_resource", joinColumns = { @JoinColumn(nullable = false, name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            nullable = false, name = "collection_id") })
    @XmlTransient
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.Resource.unmanagedResourceCollections")
    private Set<ListCollection> unmanagedResourceCollections = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource")
    private Set<BookmarkedResource> bookmarkedResources = new LinkedHashSet<>();

    private transient BillingAccount account;

    // used by the import service to determine whether a record has been
    // "created" or updated
    // does not persist
    private transient boolean created = false;
    private transient boolean updated = false;

    @Column(name = "external_id")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String externalId;

    private transient Float score = -1f;

    @XmlElementWrapper(name = "cultureKeywords")
    @XmlElement(name = "cultureKeyword")
    public Set<CultureKeyword> getCultureKeywords() {
        if (cultureKeywords == null) {
            this.cultureKeywords = new LinkedHashSet<CultureKeyword>();
        }
        return cultureKeywords;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<CultureKeyword> getActiveCultureKeywords() {
        return getCultureKeywords();
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<ResourceCreator> getActiveResourceCreators() {
        return getResourceCreators();
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<CultureKeyword> getUncontrolledCultureKeywords() {
        return getUncontrolledSuggestedKeyword(getCultureKeywords());
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<MaterialKeyword> getUncontrolledMaterialKeywords() {
        return getUncontrolledSuggestedKeyword(getMaterialKeywords());
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<CultureKeyword> getApprovedCultureKeywords() {
        return getApprovedSuggestedKeyword(getCultureKeywords());
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<MaterialKeyword> getApprovedMaterialKeywords() {
        return getApprovedSuggestedKeyword(getMaterialKeywords());
    }

    public void setCultureKeywords(Set<CultureKeyword> cultureKeywords) {
        this.cultureKeywords = cultureKeywords;
    }

    @XmlElementWrapper(name = "siteTypeKeywords")
    @XmlElement(name = "siteTypeKeyword")
    public Set<SiteTypeKeyword> getSiteTypeKeywords() {
        if (siteTypeKeywords == null) {
            this.siteTypeKeywords = new LinkedHashSet<SiteTypeKeyword>();
        }
        return siteTypeKeywords;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<SiteTypeKeyword> getActiveSiteTypeKeywords() {
        return getSiteTypeKeywords();
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<SiteTypeKeyword> getUncontrolledSiteTypeKeywords() {
        return getUncontrolledSuggestedKeyword(getSiteTypeKeywords());
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<SiteTypeKeyword> getApprovedSiteTypeKeywords() {
        return getApprovedSuggestedKeyword(getSiteTypeKeywords());
    }

    @Transient
    private <K extends SuggestedKeyword> Set<K> getUncontrolledSuggestedKeyword(
            Collection<K> keywords) {
        Set<K> uncontrolledKeys = new HashSet<K>();
        for (K key : keywords) {
            if (!key.isApproved()) {
                uncontrolledKeys.add(key);
            }
        }
        return uncontrolledKeys;
    }

    @Transient
    private <K extends SuggestedKeyword> Set<K> getApprovedSuggestedKeyword(
            Collection<K> keywords) {
        Set<K> approvedKeys = new HashSet<K>();
        for (K key : keywords) {
            if (key.isApproved()) {
                approvedKeys.add(key);
            }
        }
        return approvedKeys;
    }

    public void setSiteTypeKeywords(Set<SiteTypeKeyword> siteTypeKeywords) {
        this.siteTypeKeywords = siteTypeKeywords;
    }

    @XmlElementWrapper(name = "otherKeywords")
    @XmlElement(name = "otherKeyword")
    public Set<OtherKeyword> getOtherKeywords() {
        if (otherKeywords == null) {
            otherKeywords = new LinkedHashSet<OtherKeyword>();
        }
        return otherKeywords;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<OtherKeyword> getActiveOtherKeywords() {
        return getOtherKeywords();
    }

    public void setOtherKeywords(Set<OtherKeyword> otherKeywords) {
        this.otherKeywords = otherKeywords;
    }

    @XmlElementWrapper(name = "siteNameKeywords")
    @XmlElement(name = "siteNameKeyword")
    public Set<SiteNameKeyword> getSiteNameKeywords() {
        if (siteNameKeywords == null) {
            siteNameKeywords = new LinkedHashSet<SiteNameKeyword>();
        }
        return siteNameKeywords;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<SiteNameKeyword> getActiveSiteNameKeywords() {
        return getSiteNameKeywords();
    }

    public void setSiteNameKeywords(Set<SiteNameKeyword> siteNameKeywords) {
        this.siteNameKeywords = siteNameKeywords;
    }

    @XmlElementWrapper(name = "materialKeywords")
    @XmlElement(name = "materialKeyword")
    public Set<MaterialKeyword> getMaterialKeywords() {
        if (materialKeywords == null) {
            materialKeywords = new LinkedHashSet<MaterialKeyword>();
        }
        return materialKeywords;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<MaterialKeyword> getActiveMaterialKeywords() {
        return getMaterialKeywords();
    }

    public void setMaterialKeywords(Set<MaterialKeyword> materialKeywords) {
        this.materialKeywords = materialKeywords;
    }

    @XmlElementWrapper(name = "investigationTypes")
    @XmlElement(name = "investigationType")
    public Set<InvestigationType> getInvestigationTypes() {
        if (investigationTypes == null) {
            investigationTypes = new LinkedHashSet<InvestigationType>();
        }
        return investigationTypes;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<InvestigationType> getActiveInvestigationTypes() {
        return getInvestigationTypes();
    }

    public String getJoinedInvestigationTypes() {
        return join(getInvestigationTypes());
    }

    public void setInvestigationTypes(Set<InvestigationType> investigationTypes) {
        this.investigationTypes = investigationTypes;
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

    @Override
    public String getTitle() {
        return title;
    }

    public String getTitleSort() {
        if (getTitle() == null) {
            return "";
        }
        return getTitle().replaceAll(PersistableUtils.TITLE_SORT_REGEX, "").toLowerCase();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    @JsonView(JsonIntegrationFilter.class)
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateRegistered) {
        this.dateCreated = dateRegistered;
    }

    @Override
    @XmlAttribute(name = "submitterRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @NotNull
    public TdarUser getSubmitter() {
        return submitter;
    }

    public void setSubmitter(TdarUser submitter) {
        this.submitter = submitter;
    }

    @Override
    public String getDescription() {
        return description;
    }

    private String join(Collection<?> keywords) {
        return StringUtils.join(keywords, ", ");
    }

    public String getJoinedCultureKeywords() {
        return join(getCultureKeywords());
    }

    public String getJoinedMaterialKeywords() {
        return join(getMaterialKeywords());
    }

    public String getJoinedOtherKeywords() {
        return join(getOtherKeywords());
    }

    public String getJoinedSiteNameKeywords() {
        return join(getSiteNameKeywords());
    }

    public String getJoinedSiteTypeKeywords() {
        return join(getSiteTypeKeywords());
    }

    /**
     * If null or invalid, clears the existing lat-long box. Otherwise, replaces
     * its lat-long values with those from the incoming {@link LatitudeLongitudeBox}
     * 
     * @param latitudeLongitudeBox
     */
    public void setLatitudeLongitudeBox(
            LatitudeLongitudeBox latitudeLongitudeBox) {
        logger.debug("calling lat setter");
        if ((latitudeLongitudeBox == null) || !latitudeLongitudeBox.isValid()) {
            getLatitudeLongitudeBoxes().clear();
            return;
        }
        LatitudeLongitudeBox currentLatitudeLongitudeBox = getFirstLatitudeLongitudeBox();
        if (currentLatitudeLongitudeBox == null) {
            getLatitudeLongitudeBoxes().add(latitudeLongitudeBox);
        } else {
            currentLatitudeLongitudeBox.copyValuesFrom(latitudeLongitudeBox);
        }
    }

    @XmlElementWrapper(name = "latitudeLongitudeBoxes")
    @XmlElement(name = "latitudeLongitudeBox")
    public Set<LatitudeLongitudeBox> getLatitudeLongitudeBoxes() {
        if (latitudeLongitudeBoxes == null) {
            latitudeLongitudeBoxes = new LinkedHashSet<LatitudeLongitudeBox>();
        }
        return latitudeLongitudeBoxes;
    }

    @JsonView({JsonProjectLookupFilter.class,JsonLookupFilter.class})
    public Set<LatitudeLongitudeBox> getActiveLatitudeLongitudeBoxes() {
        return getLatitudeLongitudeBoxes();
    }

    @JsonView(JsonProjectLookupFilter.class)
    @XmlTransient
    @JsonIgnore
    public LatitudeLongitudeBox getFirstActiveLatitudeLongitudeBox() {
        if (CollectionUtils.isEmpty(getActiveLatitudeLongitudeBoxes())) {
            return null;
        }
        return getActiveLatitudeLongitudeBoxes().iterator().next();
    }

    @Transient
    @XmlTransient
    @JsonIgnore
    public boolean isLatLongVisible() {
        LatitudeLongitudeBox latLongBox = getFirstActiveLatitudeLongitudeBox();
        if (logger.isTraceEnabled()) {
            logger.trace("{} : hasConfidentialFiles:{}\t latLongBox:{}", getId(), hasConfidentialFiles(), latLongBox);
            if (latLongBox != null) {
                 logger.trace("valid: {}", latLongBox.isInitializedAndValid());
            }
        }
        if (hasConfidentialFiles() || (latLongBox == null)) {
            logger.trace("latLong for {} is confidential or null", getId());
            return Boolean.FALSE;
        }
        if (latLongBox.isInitializedAndValid()) {
            logger.trace("latLong for {} is initialized", getId());
            if ((latLongBox.getCenterLatitudeIfNotObfuscated() != null) && (latLongBox.getCenterLongitudeIfNotObfuscated() != null)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public LatitudeLongitudeBox getFirstLatitudeLongitudeBox() {
        if (CollectionUtils.isEmpty(latitudeLongitudeBoxes)) {
            return null;
        }
        return latitudeLongitudeBoxes.iterator().next();
    }

    public void setLatitudeLongitudeBoxes(
            Set<LatitudeLongitudeBox> latitudeLongitudeBoxes) {
        this.latitudeLongitudeBoxes = latitudeLongitudeBoxes;
    }

    @XmlElementWrapper(name = "geographicKeywords")
    @XmlElement(name = "geographicKeyword")
    public Set<GeographicKeyword> getGeographicKeywords() {
        if (geographicKeywords == null) {
            geographicKeywords = new LinkedHashSet<GeographicKeyword>();
        }
        return geographicKeywords;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<GeographicKeyword> getActiveGeographicKeywords() {
        return getGeographicKeywords();
    }

    public Set<GeographicKeyword> getIndexedGeographicKeywords() {
        Set<GeographicKeyword> indexed = new HashSet<GeographicKeyword>(
                getActiveGeographicKeywords());
        if (!CollectionUtils.isEmpty(managedGeographicKeywords)) {
            indexed.addAll(managedGeographicKeywords);
        }
        return indexed;
    }

    public void setGeographicKeywords(Set<GeographicKeyword> geographicKeywords) {
        this.geographicKeywords = geographicKeywords;
    }

    public String getJoinedGeographicKeywords() {
        return join(geographicKeywords);
    }

    public String getJoinedManagedGeographicKeywords() {
        return join(managedGeographicKeywords);
    }

    @XmlElementWrapper(name = "temporalKeywords")
    @XmlElement(name = "temporalKeyword")
    public Set<TemporalKeyword> getTemporalKeywords() {
        if (temporalKeywords == null) {
            temporalKeywords = new LinkedHashSet<TemporalKeyword>();
        }
        return temporalKeywords;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<TemporalKeyword> getActiveTemporalKeywords() {
        return getTemporalKeywords();
    }

    public void setTemporalKeywords(Set<TemporalKeyword> temporalKeywords) {
        this.temporalKeywords = temporalKeywords;
    }

    public String getJoinedTemporalKeywords() {
        return join(temporalKeywords);
    }

    @XmlTransient
    public Set<ResourceRevisionLog> getResourceRevisionLog() {
        return resourceRevisionLog;
    }

    public void setResourceRevisionLog(
            Set<ResourceRevisionLog> resourceRevisionLog) {
        this.resourceRevisionLog = resourceRevisionLog;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceTypeSort() {
        return resourceType.getSortName();
    }

    @Deprecated()
    @JsonView(JsonLookupFilter.class)
    // removing for localization
    public String getResourceTypeLabel() {
        return MessageHelper.getMessage(resourceType.getLocaleKey());
    }

    // marked as final because this is called from constructors.
    public final void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    @XmlElementWrapper(name = "sourceCollections")
    @XmlElement(name = "sourceCollection")
    public Set<SourceCollection> getSourceCollections() {
        if (sourceCollections == null) {
            sourceCollections = new LinkedHashSet<SourceCollection>();
        }
        return sourceCollections;
    }

    @XmlElementWrapper(name = "resourceNotes")
    @XmlElement(name = "resourceNote")
    public Set<ResourceNote> getResourceNotes() {
        if (resourceNotes == null) {
            resourceNotes = new LinkedHashSet<ResourceNote>();
        }
        return resourceNotes;
    }

    public void setSourceCollections(Set<SourceCollection> sourceCollections) {
        this.sourceCollections = sourceCollections;
    }

    public void setResourceNotes(Set<ResourceNote> resourceNotes) {
        this.resourceNotes = resourceNotes;
    }

    @XmlElementWrapper(name = "relatedComparativeCollections")
    @XmlElement(name = "relatedComparativeCollection")
    public Set<RelatedComparativeCollection> getRelatedComparativeCollections() {
        if (relatedComparativeCollections == null) {
            relatedComparativeCollections = new LinkedHashSet<RelatedComparativeCollection>();
        }
        return relatedComparativeCollections;
    }

    public void setRelatedComparativeCollections(
            Set<RelatedComparativeCollection> relatedComparativeCollections) {
        this.relatedComparativeCollections = relatedComparativeCollections;
    }

    @Override
    public String toString() {
        return String.format("%s (id: %d, %s)", title, getId(), resourceType);
    }

    /**
     * Returns the title field clamped to 200 characters.
     * 
     * @return
     */
    @Transient
    public String getShortenedTitle() {
        return StringUtils.abbreviate(title, 200);
    }

    /**
     * Returns the description field clamped to 500 characters.
     * 
     * @return
     */
    @Transient
    public String getShortenedDescription() {
        return StringUtils.abbreviate(description, 500);

    }

    /**
     * Returns the alphanumeric comparison of resource.title.
     */
    @Override
    public int compareTo(Resource resource) {
        int comparison = getTitle().compareTo(resource.getTitle());
        return (comparison == 0) ? getId().compareTo(resource.getId())
                : comparison;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Transient
    public boolean isTransient() {
        return PersistableUtils.isTransient(this);
    }

    /**
     * Returns the appropriate url namespace where actions for this information
     * resource can be accessed (e.g., /<b>project</b>/add vs
     * /<b>document</b>/add vs /<b>dataset</b>/add).
     * 
     * @return
     */
    @Override
    @JsonView(JsonLookupFilter.class)
    public String getUrlNamespace() {
        return getResourceType().getUrlNamespace();
    }

    @Transient
    public String getAbsoluteUrl() {
        return getUrlNamespace() + "/" + getId();
    }

    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @XmlAttribute(name = "updaterRef")
    public TdarUser getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(TdarUser updatedBy) {
        this.updatedBy = updatedBy;
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
    @XmlAttribute
    public Status getStatus() {
        return status;
    }

    @Override
    public void setStatus(Status status) {
        if (this.status != status) {
            setPreviousStatus(this.status);
            setStatusChanged(Boolean.TRUE);
        }
        this.status = status;
    }

    public void addResourceNote(ResourceNote note) {
        resourceNotes.add(note);
    }

    /**
     * @param resourceCreators
     *            the resourceCreators to set
     */
    public void setResourceCreators(Set<ResourceCreator> resourceCreators) {
        this.resourceCreators = resourceCreators;
    }

    /**
     * @return the set of all resourceCreators associated with this Resource
     */
    @XmlElementWrapper(name = "resourceCreators")
    @XmlElement(name = "resourceCreator")
    public Set<ResourceCreator> getResourceCreators() {
        if (resourceCreators == null) {
            resourceCreators = new LinkedHashSet<ResourceCreator>();
        }
        return resourceCreators;
    }

    /**
     * @return the resourceCreators with the given ResourceCreatorRole
     */
    public Set<ResourceCreator> getResourceCreators(ResourceCreatorRole role) {
        Set<ResourceCreator> creators = new LinkedHashSet<ResourceCreator>();
        for (ResourceCreator creator : this.getResourceCreators()) {
            if (creator.getRole() == role) {
                creators.add(creator);
            }
        }
        return creators;
    }

    /**
     * @param resourceAnnotations
     *            the resourceAnnotations to set
     */
    public void setResourceAnnotations(Set<ResourceAnnotation> resourceAnnotations) {
        this.resourceAnnotations = resourceAnnotations;
    }

    /**
     * @return the resourceAnnotations
     */
    @XmlElementWrapper(name = "resourceAnnotations")
    @XmlElement(name = "resourceAnnotation")
    public Set<ResourceAnnotation> getResourceAnnotations() {
        if (resourceAnnotations == null) {
            resourceAnnotations = new LinkedHashSet<ResourceAnnotation>();
        }
        return resourceAnnotations;
    }

    @Transient
    @XmlTransient
    public Collection<ResourceCreator> getContentOwners() {
        List<ResourceCreator> authors = new ArrayList<ResourceCreator>();

        // get the applicable resource roles for this resource type
        List<ResourceCreatorRole> primaryRoles = ResourceCreatorRole.getAuthorshipRoles();
        if (resourceCreators != null) {
            for (ResourceCreator creator : resourceCreators) {
                if (primaryRoles.contains(creator.getRole()) && !creator.getCreator().isDeleted()) {
                    authors.add(creator);
                }
            }

        }
        Collections.sort(authors);
        return authors;
    }

    public Collection<ResourceCreator> getPrimaryCreators() {
        List<ResourceCreator> authors = new ArrayList<ResourceCreator>();

        // get the applicable resource roles for this resource type
        Set<ResourceCreatorRole> primaryRoles = ResourceCreatorRole.getPrimaryCreatorRoles(getResourceType());
        if (resourceCreators != null) {
            for (ResourceCreator creator : resourceCreators) {
                if (primaryRoles.contains(creator.getRole()) && !creator.getCreator().isDeleted()) {
                    authors.add(creator);
                }
            }

        }
        Collections.sort(authors);
        return authors;
    }

    @Transient
    public Collection<ResourceCreator> getEditors() {
        List<ResourceCreator> editors = new ArrayList<ResourceCreator>(
                this.getResourceCreators(ResourceCreatorRole.EDITOR));
        Iterator<ResourceCreator> iterator = editors.iterator();
        while (iterator.hasNext()) {
            ResourceCreator rc = iterator.next();
            if (rc.getCreator().isDeleted()) {
                iterator.remove();
            }
        }
        Collections.sort(editors);
        return editors;
    }

    /**
     * @param managedGeographicKeywords
     *            the managedGeographicKeywords to set
     */
    public void setManagedGeographicKeywords(Set<GeographicKeyword> managedGeographicKeywords) {
        this.managedGeographicKeywords = managedGeographicKeywords;
    }

    /**
     * @return the managedGeographicKeywords
     */
    @XmlElementWrapper(name = "managedGeographicKeywords")
    @XmlElement(name = "managedGeographicKeyword")
    public Set<GeographicKeyword> getManagedGeographicKeywords() {
        return managedGeographicKeywords;
    }

    @Override
    public void markUpdated(TdarUser p) {
        setUpdatedBy(p);
        setUpdated(true);
        setDateUpdated(new Date());
        if ((dateCreated == null) || (submitter == null)) {
            setDateCreated(new Date());
            setSubmitter(p);
            setUploader(p);
        }
    }

    @Override
    public List<?> getEqualityFields() {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object candidate) {
        return PersistableUtils.isEqual(this, (Persistable) candidate);
    }

    @Override
    public int hashCode() {
        return PersistableUtils.toHashCode(this);
    }

    public void setCoverageDates(Set<CoverageDate> coverageDates) {
        this.coverageDates = coverageDates;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<CoverageDate> getActiveCoverageDates() {
        return getCoverageDates();
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<ResourceAnnotation> getActiveResourceAnnotations() {
        return getResourceAnnotations();
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<SourceCollection> getActiveSourceCollections() {
        return getSourceCollections();
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<RelatedComparativeCollection> getActiveRelatedComparativeCollections() {
        return getRelatedComparativeCollections();
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<ResourceNote> getActiveResourceNotes() {
        return getResourceNotes();
    }

    @XmlElementWrapper(name = "coverageDates")
    @XmlElement(name = "coverageDate")
    public Set<CoverageDate> getCoverageDates() {
        if (coverageDates == null) {
            coverageDates = new LinkedHashSet<CoverageDate>();
        }
        return coverageDates;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<Keyword> getAllActiveKeywords() {
        Collection<Keyword> kwds = new HashSet<Keyword>();
        kwds.addAll(getActiveCultureKeywords());
        kwds.addAll(getIndexedGeographicKeywords());
        kwds.addAll(getActiveSiteNameKeywords());
        kwds.addAll(getActiveInvestigationTypes());
        kwds.addAll(getActiveSiteTypeKeywords());
        kwds.addAll(getActiveMaterialKeywords());
        kwds.addAll(getActiveOtherKeywords());
        kwds.addAll(getActiveTemporalKeywords());
        return kwds;
    }

    /**
     * @param created
     *            the created to set
     */
    public void setCreated(boolean created) {
        this.created = created;
    }

    /**
     * @return the created
     */
    @XmlTransient
    @Transient
    public boolean isCreated() {
        return created;
    }

    /**
     * @param resourceCollections
     *            the resourceCollections to set
     */
    public void setResourceCollections(Set<RightsBasedResourceCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    /**
     * @return the resourceCollections
     */
    /**
     * This is a way to try and clean-up our XML serialization to provide better XML handling.  
     *  * If we're dealing with a SHARED ResourceCollection, this will return a resourceCollectionRef
     *  * If we're dealing with an INTERNAL ResourceCollection, this will return a ResourceCollection
     *  
     *  The latter behavior means that the internal collection will be fully logged to XML, but the reference will be logged otherwise.
     *  Note, this is both for terseness as well as performance (large collections). 
     * @return
     */
    @XmlElementWrapper(name = "resourceCollections")
    @XmlElementRefs({
        @XmlElementRef(name="resourceCollection", type=ResourceCollection.class,required=false),
        @XmlElementRef(name="resourceCollectionRef",type=JAXBPersistableRef.class, required=false)
    })
    @XmlJavaTypeAdapter(JaxbResourceCollectionRefConverter.class)
    public Set<RightsBasedResourceCollection> getResourceCollections() {
        if (resourceCollections == null) {
            resourceCollections = new LinkedHashSet<>();
        }
        return resourceCollections;
    }

    @Transient
    @XmlTransient
    public Set<SharedCollection> getSharedCollections() {
        HashSet<SharedCollection> shared = new HashSet<>();
        for (RightsBasedResourceCollection col : getRightsBasedResourceCollections()) {
            if (col instanceof SharedCollection) {
                shared.add((SharedCollection)col);
            }
        }
        return shared;
    }

    @Transient
    public Set<RightsBasedResourceCollection> getRightsBasedResourceCollections() {
        Set<RightsBasedResourceCollection> collections = new HashSet<>(getResourceCollections());
        Iterator<RightsBasedResourceCollection> iter = collections.iterator();
        while (iter.hasNext()) {
            ResourceCollection coll = iter.next();
            if (coll.isPublic()) {
                iter.remove();
            }
        }
        return collections;
    }

    @Transient
    public InternalCollection getInternalResourceCollection() {
        for (ResourceCollection collection : getResourceCollections()) {
            if (collection instanceof InternalCollection) {
                return (InternalCollection)collection;
            }
        }
        return null;
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

    @Override
    @Transient
    @XmlTransient
    public String getName() {
        return getTitle();
    }

    @Override
    @Transient
    @XmlTransient
    public Float getScore() {
        return score;
    }

    @Override
    public void setScore(Float score) {
        this.score = score;
    }

    @Override
    public boolean isValid() {
        if (isValidForController() == true) {
            if (getSubmitter() == null) {
                throw new TdarValidationException("resource.submitter_required", Arrays.asList(getResourceType()));
            }
            if (getDateCreated() == null) {
                throw new TdarValidationException("resource.date_required", Arrays.asList(getResourceType()));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isValidForController() {
        if (StringUtils.isEmpty(getTitle())) {
            throw new TdarValidationException("resource.title_required", Arrays.asList(getResourceType()));
        }
        if (StringUtils.isEmpty(getDescription())) {
            throw new TdarValidationException("resource.description_required", Arrays.asList(getResourceType()));
        }
        return true;
    }

    @Transient
    public Set<SharedCollection> getSharedResourceCollections() {
        Set<SharedCollection> sharedCollections = new LinkedHashSet<>();
        for (ResourceCollection collection : getResourceCollections()) {
            if (collection instanceof SharedCollection) {
                sharedCollections.add((SharedCollection)collection);
            }
        }
        return sharedCollections;
    }

    @Transient
    public Set<SharedCollection> getSharedVisibleResourceCollections() {
        Set<SharedCollection> sharedCollections = new LinkedHashSet<>();
        for (ResourceCollection collection : getResourceCollections()) {
            if (collection instanceof SharedCollection && !collection.isHidden()) {
                sharedCollections.add((SharedCollection)collection);
            }
        }
        return sharedCollections;
    }

    /**
     * @return the externalId
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * @param externalId
     *            the externalId to set
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    @Override
    @XmlTransient
    public boolean isObfuscated() {
        return obfuscated;
    }

    @Override
    public Set<Obfuscatable> obfuscate() {
        setObfuscatedObjectDifferent(false);
        setObfuscated(true);
        Set<Obfuscatable> toObfuscate = new HashSet<>();
        toObfuscate.addAll(getLatitudeLongitudeBoxes());
        toObfuscate.add(getSubmitter());
        toObfuscate.add(getUpdatedBy());
        for (ResourceCreator creator : getResourceCreators()) {
            toObfuscate.add(creator.getCreator());
        }
        return toObfuscate;
    }

    @Override
    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;

    }

    @Override
    @Transient
    @XmlTransient
    public boolean isViewable() {
        return viewable;
    }

    @Override
    public void setViewable(boolean viewable) {
        this.viewable = viewable;
    }

    /*
     * This method is used to capture people who upload things without any
     * ResourceCreators, this makes the resource show up in the browse page for
     * that creator
     */
    @XmlTransient
    public Long getResourceOwner() {
        if (CollectionUtils.isEmpty(getResourceCreators())) {
            return getSubmitter().getId();
        }
        return null;
    }


    @XmlTransient
    public List<Creator<?>> getRelatedCreators() {
        List<Creator<?>> creators = new ArrayList<>();
        for (ResourceCreator creator : resourceCreators) {
            creators.add(creator.getCreator());
        }
        creators.add(getSubmitter());
        return creators;
    }


    @JsonIgnore
    @XmlTransient
    public Long getTransientAccessCount() {
        return transientAccessCount;
    }

    public void setTransientAccessCount(Long l) {
        this.transientAccessCount = l;
    }

    @Transient
    public boolean isSupportsThumbnails() {
        return false;
    }

    @Transient
    public boolean isCitationRecord() {
        return true;
    }

    public <R extends Resource> void copyImmutableFieldsFrom(R resource) {
        this.setDateCreated(resource.getDateCreated());
        this.setStatus(resource.getStatus());
        this.setSubmitter(resource.getSubmitter());
        // set previous, then set current
        this.setSpaceInBytesUsed(resource.getPreviousSpaceInBytesUsed());
        this.setFilesUsed(resource.getPreviousFilesUsed());
        this.setSpaceInBytesUsed(resource.getSpaceInBytesUsed());
        this.setFilesUsed(resource.getFilesUsed());
        this.getResourceCollections().addAll(new ArrayList<>(resource.getResourceCollections()));

    }

    @XmlAttribute(name = "uploaderRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @NotNull
    public TdarUser getUploader() {
        return uploader;
    }

    public void setUploader(TdarUser uploader) {
        this.uploader = uploader;
    }

    @Transient
    @XmlTransient
    public boolean isLessThanDayOld() {
        return Days.daysBetween(new DateTime(new Date()), new DateTime(getDateCreated())).getDays() < 1;
    }

    public boolean isContainsActiveKeywords() {

        if (CollectionUtils.isNotEmpty(getActiveSiteNameKeywords()) || CollectionUtils.isNotEmpty(getActiveCultureKeywords()) ||
                CollectionUtils.isNotEmpty(getActiveSiteTypeKeywords()) || CollectionUtils.isNotEmpty(getActiveMaterialKeywords()) ||
                CollectionUtils.isNotEmpty(getActiveInvestigationTypes()) || CollectionUtils.isNotEmpty(getActiveOtherKeywords()) ||
                CollectionUtils.isNotEmpty(getActiveTemporalKeywords()) || CollectionUtils.isNotEmpty(getActiveGeographicKeywords())) {
            return true;
        }
        return false;
    }

    @Transient
    public List<String> getKeywordProperties() {
        List<String> toReturn = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(getActiveCultureKeywords())) {
            toReturn.add("activeCultureKeywords");
        }
        if (CollectionUtils.isNotEmpty(getActiveMaterialKeywords())) {
            toReturn.add("activeMaterialKeywords");
        }
        if (CollectionUtils.isNotEmpty(getActiveSiteNameKeywords())) {
            toReturn.add("activeSiteNameKeywords");
        }
        if (CollectionUtils.isNotEmpty(getActiveSiteTypeKeywords())) {
            toReturn.add("activeSiteTypeKeywords");
        }
        if (CollectionUtils.isNotEmpty(getActiveInvestigationTypes())) {
            toReturn.add("activeInvestigationTypes");
        }
        if (CollectionUtils.isNotEmpty(getActiveOtherKeywords())) {
            toReturn.add("activeOtherKeywords");
        }
        if (CollectionUtils.isNotEmpty(getActiveGeographicKeywords())) {
            toReturn.add("activeGeographicKeywords");
        }
        if (CollectionUtils.isNotEmpty(getActiveTemporalKeywords())) {
            toReturn.add("activeTemporalKeywords");
        }
        return toReturn;
    }

    @JsonIgnore
    @XmlTransient
    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    public Status getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Status previousStatus) {
        this.previousStatus = previousStatus;
    }

    public Long getSpaceInBytesUsed() {
        if (spaceInBytesUsed == null) {
            return 0L;
        }
        return spaceInBytesUsed;
    }

    public void setSpaceInBytesUsed(Long spaceInBytesUsed) {
        setPreviousSpaceInBytesUsed(this.spaceInBytesUsed);
        this.spaceInBytesUsed = spaceInBytesUsed;
    }

    public Long getFilesUsed() {
        if (filesUsed == null) {
            return 0L;
        }
        return filesUsed;
    }

    public void setFilesUsed(Long filesUsed) {
        setPreviousFilesUsed(this.filesUsed);
        this.filesUsed = filesUsed;
    }

    public Long getPreviousSpaceInBytesUsed() {
        if (previousSpaceInBytesUsed == null) {
            return 0L;
        }
        return previousSpaceInBytesUsed;
    }

    public void setPreviousSpaceInBytesUsed(Long previousSpaceInBytesUsed) {
        this.previousSpaceInBytesUsed = previousSpaceInBytesUsed;
    }

    public Long getPreviousFilesUsed() {
        if (previousFilesUsed == null) {
            return 0L;
        }
        return previousFilesUsed;
    }

    public void setPreviousFilesUsed(Long previousFilesUsed) {
        this.previousFilesUsed = previousFilesUsed;
    }

    @JsonIgnore
    @XmlTransient
    public Long getEffectiveSpaceUsed() {
        return getSpaceInBytesUsed() - getPreviousSpaceInBytesUsed();
    }

    @JsonIgnore
    @XmlTransient
    public Long getSpaceUsedInMb() {
        return MathUtils.divideByRoundUp(spaceInBytesUsed, MathUtils.ONE_MB);
    }

    @JsonIgnore
    @XmlTransient
    public Long getEffectiveFilesUsed() {
        return getFilesUsed() - getPreviousFilesUsed();
    }

    @JsonIgnore
    @XmlTransient
    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    @JsonIgnore
    @XmlTransient
    public boolean isCountedInBillingEvaluation() {
        return countedInBillingEvaluation;
    }

    public void setCountedInBillingEvaluation(boolean countedInBillingEvaluation) {
        this.countedInBillingEvaluation = countedInBillingEvaluation;
    }

    @Transient
    public boolean hasConfidentialFiles() {
        return false;
    }

    @Transient
    public boolean hasEmbargoedFiles() {
        return false;
    }

    @Transient
    public boolean isHasBrowsableImages() {
        return false;
    }

    @Override
    public Boolean getObfuscatedObjectDifferent() {
        return obfuscatedObjectDifferent;
    }

    @Override
    public void setObfuscatedObjectDifferent(Boolean value) {
        this.obfuscatedObjectDifferent = value;
    }

    public Set<ResourceCreator> getIndividualAndInstitutionalCredit() {
        Set<ResourceCreator> creators = new HashSet<>();
        for (ResourceCreator creator : this.getActiveResourceCreators()) {
            if (creator.getRole().getType() == ResourceCreatorRoleType.CREDIT) {
                creators.add(creator);
            }
        }
        return creators;
    }

    @JsonView(JsonProjectLookupFilter.class)
    public Set<ResourceCreator> getActiveIndividualAndInstitutionalCredit() {
        return getIndividualAndInstitutionalCredit();
    }

    @JsonIgnore
    @XmlTransient
    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    @JsonView(JsonLookupFilter.class)
    public String getDetailUrl() {
        return String.format("/%s/%s/%s", getUrlNamespace(), getId(), getSlug());
    }

    @Override
    public String getSlug() {
        return UrlUtils.slugify(getName());
    }

    @JsonIgnore
    @XmlTransient
    public Set<BookmarkedResource> getBookmarkedResources() {
        return bookmarkedResources;
    }

    public void setBookmarkedResources(Set<BookmarkedResource> bookmarkedResources) {
        this.bookmarkedResources = bookmarkedResources;
    }

    public Boolean getStatusChanged() {
        return statusChanged;
    }

    private void setStatusChanged(Boolean statusChanged) {
        this.statusChanged = statusChanged;
    }

    public String getFormattedDescription() {
        return formattedDescription;
    }

    public void setFormattedDescription(String formattedDescription) {
        this.formattedDescription = formattedDescription;
    }

    public Set<ListCollection> getUnmanagedResourceCollections() {
        return unmanagedResourceCollections;
    }

    public void setUnmanagedResourceCollections(Set<ListCollection> publicResourceCollections) {
        this.unmanagedResourceCollections = publicResourceCollections;
    }

    public Collection<? extends ResourceCollection> getVisibleUnmanagedResourceCollections() {
        Set<ResourceCollection> collections = new LinkedHashSet<ResourceCollection>();
        for (ResourceCollection collection : getUnmanagedResourceCollections()) {
            if (collection instanceof SharedCollection && !collection.isHidden()) {
                collections.add(collection);
            }
        }
        return collections;
    }

    @Transient
    @XmlTransient
    @Override
    public boolean isConfidentialViewable() {
        return viewConfidential;
    }

    @Override
    public void setConfidentialViewable(boolean editable) {
        this.viewConfidential = editable;
    }
}
