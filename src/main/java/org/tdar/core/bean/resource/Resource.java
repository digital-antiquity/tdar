package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.search.Explanation;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.DynamicBoost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.annotations.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.JsonModel;
import org.tdar.core.bean.OaiDcProvider;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.SimpleSearch;
import org.tdar.core.bean.Updatable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.SuggestedKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.index.analyzer.AutocompleteAnalyzer;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.NonTokenizingLowercaseKeywordAnalyzer;
import org.tdar.search.index.analyzer.PatternTokenAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.index.boost.InformationResourceBoostStrategy;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

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
@Table(name = "resource")
@Indexed(index = "Resource")
@DynamicBoost(impl = InformationResourceBoostStrategy.class)
@XStreamAlias("resource")
@Inheritance(strategy = InheritanceType.JOINED)
@XmlRootElement
@XmlSeeAlso({ Document.class, InformationResource.class, Project.class, CodingSheet.class,
        Dataset.class, Ontology.class, Image.class, SensoryData.class })
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "resource")
public class Resource extends JsonModel.Base implements Persistable, Comparable<Resource>, HasName, Updatable, Indexable, Validatable, HasStatus,
        OaiDcProvider, Obfuscatable {

    private static final long serialVersionUID = -230400285817185637L;

    private transient boolean obfuscated;
    // TODO: anything that gets returned in a tdar search should be included in
    // json results
    @Transient
    private static final String[] JSON_PROPERTIES = { "id", "title", "resourceType", "dateCreated", "description", "status", "resourceTypeLabel",
            "urlNamespace" };
    // properties in resourceType
    // properties in submitter (Person)
    // "firstName", "lastName", "institution", "email","label","submitter",
    protected final static transient Logger logger = LoggerFactory.getLogger(Resource.class);

    public Resource() {
    }

    @Deprecated
    public Resource(Long id, String title) {
        setId(id);
        setTitle(title);
    }

    @Deprecated
    public Resource(Long id, String title, ResourceType type) {
        setId(id);
        setTitle(title);
        setResourceType(type);
    }

    @Id
    @DocumentId
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resource_sequence")
    @SequenceGenerator(name = "resource_sequence", allocationSize = 1, sequenceName = "resource_sequence")
    private Long id = -1L;

    @BulkImportField(label = "Title", required = true, order = -100, comment = BulkImportField.TITLE_DESCRIPTION)
    @Column(nullable = false, length = 512)
    private String title;

    @Lob
    @BulkImportField(label = "Description", required = true, order = -50, comment = BulkImportField.DESCRIPTION_DESCRIPTION)
    @Type(type = "org.hibernate.type.StringClobType")
    private String description;

    @Field(boost = @Boost(.5f), index = Index.UN_TOKENIZED, store = Store.YES)
    @Column(nullable = false, name = "date_registered")
    @DateBridge(resolution = Resolution.DAY)
    private Date dateCreated;

    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    private ResourceType resourceType;

    @Column(nullable = false, name = "access_counter")
    private Long accessCounter = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Field
    @Analyzer(impl = NonTokenizingLowercaseKeywordAnalyzer.class)
    private Status status = Status.ACTIVE;

    @Boost(.5f)
    @IndexedEmbedded
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false, name = "submitter_id")
    private Person submitter;

    @Boost(.5f)
    @IndexedEmbedded
    @ManyToOne()
    @JoinColumn(name = "updater_id")
    private Person updatedBy;

    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    @Column(nullable = true, name = "date_updated")
    @DateBridge(resolution = Resolution.MILLISECOND)
    private Date dateUpdated;

    @IndexedEmbedded
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    @BulkImportField
    private Set<ResourceCreator> resourceCreators = new LinkedHashSet<ResourceCreator>();

    @XStreamOmitField
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource")
    private Set<BookmarkedResource> bookmarks = new LinkedHashSet<BookmarkedResource>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("sequenceNumber ASC")
    @IndexedEmbedded
    @IndexColumn(name = "id")
    private Set<ResourceNote> resourceNotes = new LinkedHashSet<ResourceNote>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.LAZY, orphanRemoval = true)
    @IndexedEmbedded
    private Set<ResourceAnnotation> resourceAnnotations = new LinkedHashSet<ResourceAnnotation>();

    @IndexedEmbedded
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SourceCollection> sourceCollections = new LinkedHashSet<SourceCollection>();

    @IndexedEmbedded
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<RelatedComparativeCollection> relatedComparativeCollections = new LinkedHashSet<RelatedComparativeCollection>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<LatitudeLongitudeBox> latitudeLongitudeBoxes = new LinkedHashSet<LatitudeLongitudeBox>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_geographic_keyword", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "geographic_keyword_id") })
    private Set<GeographicKeyword> geographicKeywords = new LinkedHashSet<GeographicKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_managed_geographic_keyword", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "geographic_keyword_id") })
    private Set<GeographicKeyword> managedGeographicKeywords = new LinkedHashSet<GeographicKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_temporal_keyword", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "temporal_keyword_id") })
    private Set<TemporalKeyword> temporalKeywords = new LinkedHashSet<TemporalKeyword>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "resource", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CoverageDate> coverageDates = new LinkedHashSet<CoverageDate>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_culture_keyword", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "culture_keyword_id") })
    private Set<CultureKeyword> cultureKeywords = new LinkedHashSet<CultureKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_other_keyword", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "other_keyword_id") })
    private Set<OtherKeyword> otherKeywords = new LinkedHashSet<OtherKeyword>();

    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_site_name_keyword", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "site_name_keyword_id") })
    private Set<SiteNameKeyword> siteNameKeywords = new LinkedHashSet<SiteNameKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_material_keyword", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "material_keyword_id") })
    private Set<MaterialKeyword> materialKeywords = new LinkedHashSet<MaterialKeyword>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_investigation_type", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "investigation_type_id") })
    private Set<InvestigationType> investigationTypes = new LinkedHashSet<InvestigationType>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "resource_site_type_keyword", joinColumns = { @JoinColumn(name = "resource_id") }, inverseJoinColumns = { @JoinColumn(
            name = "site_type_keyword_id") })
    private Set<SiteTypeKeyword> siteTypeKeywords = new LinkedHashSet<SiteTypeKeyword>();

    @OneToMany(mappedBy = "resource")
    @ForeignKey(name = "none")
    @XmlTransient
    private Set<ResourceRevisionLog> resourceRevisionLog = new HashSet<ResourceRevisionLog>();

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "collection_resource", joinColumns = { @JoinColumn(name = "resource_id") },
            inverseJoinColumns = { @JoinColumn(name = "collection_id") })
    @XmlTransient
    @IndexedEmbedded(depth = 1)
    private Set<ResourceCollection> resourceCollections = new LinkedHashSet<ResourceCollection>();

    // used by the import service to determine whether a record has been "created" or updated
    // does not persist
    private transient boolean created = false;

    @Column(name = "external_id")
    private String externalId;

    private transient Float score = -1f;

    private transient Explanation explanation;

    @XmlElementWrapper(name = "cultureKeywords")
    @XmlElement(name = "cultureKeyword")
    public Set<CultureKeyword> getCultureKeywords() {
        if (cultureKeywords == null) {
            this.cultureKeywords = new LinkedHashSet<CultureKeyword>();
        }
        return cultureKeywords;
    }

    /*
     * this function should introduce into the index all of the people who can modify a record
     * which is useful for limiting things on the project page
     */
    @Field(name = QueryFieldNames.RESOURCE_USERS_WHO_CAN_MODIFY)
    @Analyzer(impl = PatternTokenAnalyzer.class)
    @Transient
    @JSONTransient
    public String getUsersWhoCanModify() {
        StringBuilder sb = new StringBuilder();
        HashSet<Person> writable = new HashSet<Person>();
        writable.add(getSubmitter());
        writable.add(getUpdatedBy());
        for (ResourceCollection collection : getResourceCollections()) {
            writable.addAll(collection.getUsersWhoCan(GeneralPermissions.MODIFY_RECORD, true));
        }
        for (Person p : writable) {
            if (p == null || p.getId() == null)
                continue;
            sb.append(p.getId()).append("|");
        }
        // FIXME: decide whether right should inherit from projects (1) of (2) change see authorizedUserDao
        // sb.append(getAdditionalUsersWhoCanModify());
        logger.trace("effectiveUsers:" + sb.toString());
        return sb.toString();
    }

    protected String getAdditionalUsersWhoCanModify() {
        return "";
    }

    @Field(name = QueryFieldNames.RESOURCE_COLLECTION_PUBLIC_IDS)
    @Analyzer(impl = PatternTokenAnalyzer.class)
    @Transient
    @JSONTransient
    public String getPublicCollectionsContaining() {
        StringBuilder sb = new StringBuilder();
        for (ResourceCollection collection : getResourceCollections()) {
            if (collection.isShared()) {
                sb.append(collection.getId()).append("|");
            }
        }
        if (sb.length() > 0) {
            logger.trace("partOfPublicResourceCollection:" + sb.toString());
        }
        return sb.toString();
    }

    @IndexedEmbedded
    public Set<CultureKeyword> getActiveCultureKeywords() {
        return getCultureKeywords();
    }

    @Transient
    public Set<CultureKeyword> getUncontrolledCultureKeywords() {
        return getUncontrolledSuggestedKeyword(getCultureKeywords());
    }

    @Transient
    public Set<CultureKeyword> getApprovedCultureKeywords() {
        return getApprovedSuggestedKeyword(getCultureKeywords());
    }

    public void setCultureKeywords(Set<CultureKeyword> cultureKeywords) {
        this.cultureKeywords = cultureKeywords;
    }

    @XmlElementWrapper(name = "siteTypeKeywords")
    @XmlElement(name = "siteTypeKeyword")
    public Set<SiteTypeKeyword> getSiteTypeKeywords() {
        if (siteTypeKeywords == null) {
            this.siteNameKeywords = new LinkedHashSet<SiteNameKeyword>();
        }
        return siteTypeKeywords;
    }

    @IndexedEmbedded
    public Set<SiteTypeKeyword> getActiveSiteTypeKeywords() {
        return getSiteTypeKeywords();
    }

    @Transient
    public Set<SiteTypeKeyword> getUncontrolledSiteTypeKeywords() {
        return getUncontrolledSuggestedKeyword(getSiteTypeKeywords());
    }

    @Transient
    public Set<SiteTypeKeyword> getApprovedSiteTypeKeywords() {
        return getApprovedSuggestedKeyword(getSiteTypeKeywords());
    }

    @Transient
    private <K extends SuggestedKeyword> Set<K> getUncontrolledSuggestedKeyword(Collection<K> keywords) {
        Set<K> uncontrolledKeys = new HashSet<K>();
        for (K key : keywords) {
            if (!key.isApproved())
                uncontrolledKeys.add(key);
        }
        return uncontrolledKeys;
    }

    @Transient
    private <K extends SuggestedKeyword> Set<K> getApprovedSuggestedKeyword(Collection<K> keywords) {
        Set<K> approvedKeys = new HashSet<K>();
        for (K key : keywords) {
            if (key.isApproved())
                approvedKeys.add(key);
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

    @IndexedEmbedded(targetElement = OtherKeyword.class)
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

    @IndexedEmbedded
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

    @IndexedEmbedded
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

    @IndexedEmbedded
    public Set<InvestigationType> getActiveInvestigationTypes() {
        return getInvestigationTypes();
    }

    public String getJoinedInvestigationTypes() {
        return join(getInvestigationTypes());
    }

    public void setInvestigationTypes(Set<InvestigationType> investigationTypes) {
        this.investigationTypes = investigationTypes;
    }

    @Field(store = Store.YES, analyzer = @Analyzer(impl = KeywordAnalyzer.class), name = QueryFieldNames.ID)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // @Field(store = Store.YES, analyzer = @Analyzer(impl = KeywordAnalyzer.class), name = QueryFieldNames.ID)
    // public Long getIndexedId() {
    // return getId();
    // }

    @Fields({ @Field(boost = @Boost(1.5f)),
            @Field(name = QueryFieldNames.TITLE_AUTO, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class)) })
    public String getTitle() {
        return title;
    }

    @Field(name = QueryFieldNames.TITLE_SORT, index = Index.UN_TOKENIZED, store = Store.YES)
    public String getTitleSort() {
        if (getTitle() == null)
            return "";
        return getTitle().replaceAll(SimpleSearch.TITLE_SORT_REGEX, "");
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateRegistered) {
        this.dateCreated = dateRegistered;
    }

    @XmlAttribute(name = "submitterRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Person getSubmitter() {
        return submitter;
    }

    public void setSubmitter(Person submitter) {
        this.submitter = submitter;
    }

    @Field(boost = @Boost(1.2f))
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
    public void setLatitudeLongitudeBox(LatitudeLongitudeBox latitudeLongitudeBox) {
        if (latitudeLongitudeBox == null || !latitudeLongitudeBox.isValid()) {
            getLatitudeLongitudeBoxes().clear();
            return;
        }
        LatitudeLongitudeBox currentLatitudeLongitudeBox = getFirstLatitudeLongitudeBox();
        if (currentLatitudeLongitudeBox == null) {
            latitudeLongitudeBox.setResource(this);
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

    @IndexedEmbedded
    public Set<LatitudeLongitudeBox> getActiveLatitudeLongitudeBoxes() {
        return getLatitudeLongitudeBoxes();
    }

    public LatitudeLongitudeBox getFirstLatitudeLongitudeBox() {
        if (CollectionUtils.isEmpty(latitudeLongitudeBoxes)) {
            return null;
        }
        return latitudeLongitudeBoxes.iterator().next();
    }

    public void setLatitudeLongitudeBoxes(Set<LatitudeLongitudeBox> latitudeLongitudeBoxes) {
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

    // @IndexedEmbedded
    public Set<GeographicKeyword> getActiveGeographicKeywords() {
        return getGeographicKeywords();
    }

    @IndexedEmbedded(prefix = "activeGeographicKeywords.")
    public Set<GeographicKeyword> getIndexedGeographicKeywords() {
        Set<GeographicKeyword> indexed = new HashSet<GeographicKeyword>(getGeographicKeywords());
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

    @IndexedEmbedded
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

    public void setResourceRevisionLog(Set<ResourceRevisionLog> resourceRevisionLog) {
        this.resourceRevisionLog = resourceRevisionLog;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    @Field(index = Index.UN_TOKENIZED, store = Store.YES, name = QueryFieldNames.RESOURCE_TYPE_SORT)
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    public String getResourceTypeSort() {
        return resourceType.getSortName();
    }

    @Transient
    public String getResourceTypeLabel() {
        return resourceType.getLabel();
    }

    public void setResourceType(ResourceType resourceType) {
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

    @XmlTransient
    public Set<BookmarkedResource> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(Set<BookmarkedResource> bookmarks) {
        this.bookmarks = bookmarks;
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

    public void setRelatedComparativeCollections(Set<RelatedComparativeCollection> relatedComparativeCollections) {
        this.relatedComparativeCollections = relatedComparativeCollections;
    }

    @XmlTransient
    public Long getAccessCounter() {
        return accessCounter;
    }

    public void setAccessCounter(Long accessCounter) {
        this.accessCounter = accessCounter;
    }

    public void incrementAccessCounter() {
        accessCounter++;
    }

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
        return (comparison == 0) ? getId().compareTo(resource.getId()) : comparison;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Transient
    public boolean isTransient() {
        return Persistable.Base.isTransient(this);
    }

    /**
     * Returns the appropriate url namespace where actions for this information
     * resource can be accessed (e.g., /<b>project</b>/add vs
     * /<b>document</b>/add vs /<b>dataset</b>/add).
     * 
     * @return
     */
    @Transient
    public String getUrlNamespace() {
        String urlToReturn = getResourceType().name();
        return urlToReturn.toLowerCase().replaceAll("_", "-");
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        return JSON_PROPERTIES;
    }

    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    @XmlAttribute(name = "updaterRef")
    public Person getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Person updatedBy) {
        this.updatedBy = updatedBy;
    }

    @XmlTransient
    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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
        return resourceCreators;
    }

    /**
     * @return the resourceCreators with the given ResourceCreatorRole
     */
    public Set<ResourceCreator> getResourceCreators(ResourceCreatorRole role) {
        Set<ResourceCreator> creators = new HashSet<ResourceCreator>();
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

    public Collection<ResourceCreator> getPrimaryCreators() {
        List<ResourceCreator> authors = new ArrayList<ResourceCreator>();

        // get the applicable resource roles for this resource type
        Set<ResourceCreatorRole> primaryRoles = ResourceCreatorRole.getPrimaryCreatorRoles(getResourceType());
        if (resourceCreators != null) {
            for (ResourceCreator creator : resourceCreators) {
                if (primaryRoles.contains(creator.getRole()))
                    authors.add(creator);
            }

        }
        Collections.sort(authors);
        return authors;
    }

    @Transient
    public Collection<ResourceCreator> getEditors() {
        List<ResourceCreator> editors = new ArrayList<ResourceCreator>(this.getResourceCreators(ResourceCreatorRole.EDITOR));
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

    public void markUpdated(Person p) {
        setUpdatedBy(p);
        setDateUpdated(new Date());
        if (dateCreated == null || submitter == null) {
            setDateCreated(new Date());
            setSubmitter(p);
        }
    }

    @Override
    public List<?> getEqualityFields() {
        return Arrays.asList(getId());
    }

    @Override
    public boolean equals(Object candidate) {
        if (this == candidate) {
            return true;
        }
        if (candidate == null) {
            return false;
        }
        try {
            return Persistable.Base.isEqual(this, Resource.class.cast(candidate));
        } catch (ClassCastException e) {
            logger.debug("{} <==> {} ", candidate.getClass(), getClass());
            logger.debug("{}", e);
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (isTransient()) {
            return super.hashCode();
        }
        return Persistable.Base.toHashCode(this);
    }

    public void setCoverageDates(Set<CoverageDate> coverageDates) {
        this.coverageDates = coverageDates;
    }

    @IndexedEmbedded
    public Set<CoverageDate> getActiveCoverageDates() {
        return getCoverageDates();
    }

    @XmlElementWrapper(name = "coverageDates")
    @XmlElement(name = "coverageDate")
    public Set<CoverageDate> getCoverageDates() {
        if (coverageDates == null) {
            coverageDates = new LinkedHashSet<CoverageDate>();
        }
        return coverageDates;
    }

    public String getAdditonalKeywords() {
        return "";
    }

    @JSONTransient
    @Fields({ @Field(name = QueryFieldNames.ALL_PHRASE, analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)),
            @Field(name = QueryFieldNames.ALL, analyzer = @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)) })
    public String getKeywords() {
        // note, consider using a transient field here, as the getter is called multiple items (once for each @Field annotation)
        logger.trace("get keyword contents: {}", getId());
        StringBuilder sb = new StringBuilder();
        sb.append(getTitle()).append(" ").append(getDescription()).append(" ").append(getAdditonalKeywords()).append(" ");

        for (Keyword kwd : getIndexedGeographicKeywords()) {
            sb.append(kwd.getLabel()).append(" ");
        }
        for (Keyword kwd : getActiveInvestigationTypes()) {
            sb.append(kwd.getLabel()).append(" ");
        }
        for (Keyword kwd : getActiveMaterialKeywords()) {
            sb.append(kwd.getLabel()).append(" ");
        }
        for (Keyword kwd : getActiveOtherKeywords()) {
            sb.append(kwd.getLabel()).append(" ");
        }
        for (Keyword kwd : getActiveSiteNameKeywords()) {
            sb.append(kwd.getLabel()).append(" ");
        }

        for (HierarchicalKeyword<CultureKeyword> kwd : getActiveCultureKeywords()) {
            sb.append(kwd.getLabel()).append(" ");
            for (String label : kwd.getParentLabelList()) {
                sb.append(label).append(" ");
            }
        }

        for (HierarchicalKeyword<SiteTypeKeyword> kwd : getActiveSiteTypeKeywords()) {
            sb.append(kwd.getLabel()).append(" ");
            for (String label : kwd.getParentLabelList()) {
                sb.append(label).append(" ");
            }
        }

        for (Keyword kwd : getActiveTemporalKeywords()) {
            sb.append(kwd.getLabel()).append(" ");
        }
        for (ResourceNote note : getResourceNotes()) {
            sb.append(note.getNote()).append(" ");
        }
        for (ResourceCreator creator : getResourceCreators()) {
            sb.append(creator.getCreator().getName()).append(" ");
            sb.append(creator.getCreator().getProperName()).append(" ");
        }
        for (ResourceAnnotation ann : getResourceAnnotations()) {
            sb.append(ann.getPairedValue()).append(" ");
        }

        for (RelatedComparativeCollection rcc : getRelatedComparativeCollections()) {
            sb.append(rcc.getText()).append(" ");
        }

        for (SourceCollection src : getSourceCollections()) {
            sb.append(src.getText()).append(" ");
        }
        return sb.toString();
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
    public void setResourceCollections(Set<ResourceCollection> resourceCollections) {
        this.resourceCollections = resourceCollections;
    }

    /**
     * @return the resourceCollections
     */
    @XmlElementWrapper(name = "resourceCollections")
    @XmlElement(name = "resourceCollection")
    public Set<ResourceCollection> getResourceCollections() {
        if (resourceCollections == null) {
            resourceCollections = new LinkedHashSet<ResourceCollection>();
        }
        return resourceCollections;
    }

    @Transient
    public ResourceCollection getInternalResourceCollection() {
        for (ResourceCollection collection : getResourceCollections()) {
            if (collection.getType() == CollectionType.INTERNAL) {
                return collection;
            }
        }
        return null;
    }

    @Transient
    @XmlTransient
    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    @Transient
    @XmlTransient
    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    @Transient
    @XmlTransient
    public boolean isDraft() {
        return status == Status.DRAFT;
    }

    @Transient
    @XmlTransient
    public boolean isFlagged() {
        return status == Status.FLAGGED;
    }

    @Transient
    @XmlTransient
    public String getName() {
        return getTitle();
    }

    @Transient
    @XmlTransient
    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    @JSONTransient
    public boolean isValid() {
        if (isValidForController() == true) {
            if (getSubmitter() == null) {
                throw new TdarRecoverableRuntimeException("A submitter is required for this " + getResourceType());
            }
            if (getDateCreated() == null) {
                throw new TdarRecoverableRuntimeException("The registered date is required for this " + getResourceType());
            }
            return true;
        }
        return false;
    }

    @JSONTransient
    public boolean isValidForController() {
        if (StringUtils.isEmpty(getTitle())) {
            throw new TdarRecoverableRuntimeException("A title is required for this " + getResourceType());
        }
        if (StringUtils.isEmpty(getDescription())) {
            throw new TdarRecoverableRuntimeException("A description is required for this " + getResourceType());
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.bean.Indexable#getExplanation()
     */
    @Override
    @XmlTransient
    public Explanation getExplanation() {
        return explanation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.bean.Indexable#setExplanation(org.apache.lucene.search.Explanation)
     */
    @Override
    public void setExplanation(Explanation ex) {
        this.explanation = ex;
    }

    @Transient
    public Set<ResourceCollection> getSharedResourceCollections() {
        Set<ResourceCollection> sharedCollections = new LinkedHashSet<ResourceCollection>();
        for (ResourceCollection collection : getResourceCollections()) {
            if (collection.isShared()) {
                sharedCollections.add(collection);
            }
        }
        return sharedCollections;
    }

    @Transient
    public Set<ResourceCollection> getSharedVisibleResourceCollections() {
        Set<ResourceCollection> sharedCollections = new LinkedHashSet<ResourceCollection>();
        for (ResourceCollection collection : getResourceCollections()) {
            if (collection.isShared() && collection.isVisible()) {
                sharedCollections.add(collection);
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
    @JSONTransient
    public boolean isObfuscated() {
        return obfuscated;
    }

    @Override
    public List<Obfuscatable> obfuscate() {
        List<Obfuscatable> toObfuscate = new ArrayList<Obfuscatable>();
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

    /*
     * This method is used to capture people who upload things without any ResourceCreators, this makes the resource show up
     * in the browse page for that creator
     */
    @Field(name = QueryFieldNames.RESOURCE_OWNER, store = Store.YES, analyzer = @Analyzer(impl = KeywordAnalyzer.class))
    @JSONTransient
    @XmlTransient
    public Long getResourceOwner() {
        if (CollectionUtils.isEmpty(getResourceCreators())) {
            return getSubmitter().getId();
        }
        return null;
    }

    // @Transient
    // @Field(index = Index.UN_TOKENIZED, store = Store.YES, analyzer=@Analyzer(impl = TdarStandardAnalyzer.class), name=QueryFieldNames.SEARCH_TYPE)
    // public SimpleSearchType getSimpleSearchType() {
    // return SimpleSearchType.RESOURCE;
    // }
    //
    // public Status getStatusForSearch() {
    // return getStatus();
    // }
}
