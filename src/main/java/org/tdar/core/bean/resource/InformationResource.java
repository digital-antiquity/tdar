package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DynamicBoost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.InformationResourceFile.FileStatus;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.search.index.analyzer.AutocompleteAnalyzer;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.index.boost.InformationResourceBoostStrategy;
import org.tdar.search.index.bridge.PersistentReaderBridge;
import org.tdar.search.index.bridge.StringMapBridge;
import org.tdar.search.index.bridge.TdarPaddedNumberBridge;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

/**
 * $Id$
 * <p>
 * Represents a Resource with a file payload and additional metadata that can be one of the following:
 * </p>
 * <ol>
 * <li>Image
 * <li>Dataset file (Access, Excel)
 * <li>Document (PDF)
 * </ol>
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Entity
@Table(name = "information_resource")
@DynamicBoost(impl = InformationResourceBoostStrategy.class)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class InformationResource extends Resource {

    private static final long serialVersionUID = -1534799746444826257L;
    public static final String[] JSON_PROPERTIES = { "inheritingCulturalInformation", "inheritingInvestigationInformation", "inheritingMaterialInformation",
            "inheritingOtherInformation", "inheritingSiteInformation", "inheritingSpatialInformation", "inheritingTemporalInformation",
            "inheritingIdentifierInformation", "inheritingNoteInformation", "inheritingCollectionInformation"
    };

    public InformationResource() {

    }

    @Deprecated
    public InformationResource(Long id, String title) {
        setId(id);
        setTitle(title);
    }

    @Deprecated
    public InformationResource(Long id, String title, ResourceType type) {
        setId(id);
        setTitle(title);
        setResourceType(type);
    }

    @ManyToOne(optional = true)
    // @ContainedIn /* DISABLED TO MANAGE PERFORMANCE ISSUES*/
    private Project project;

    @Transient
    private Long projectId;

    // @ManyToMany
    // @JoinTable(name = "information_resource_related_citation", joinColumns = @JoinColumn(name = "information_resource_id"), inverseJoinColumns = @JoinColumn(
    // name = "document_id"))
    // private Set<Document> relatedCitations = new HashSet<Document>();
    //
    // @ManyToMany(cascade = CascadeType.ALL)
    // @XStreamOmitField
    // @JoinTable(name = "information_resource_source_citation", joinColumns = @JoinColumn(name = "information_resource_id"), inverseJoinColumns = @JoinColumn(
    // name = "document_id"))
    // private Set<Document> sourceCitations = new HashSet<Document>();

    // FIXME: cascade "delete" ?
    @OneToMany(mappedBy = "informationResource", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @OrderBy("sequenceNumber asc")
    @JSONTransient
    @IndexedEmbedded
    private Set<InformationResourceFile> informationResourceFiles = new LinkedHashSet<>();

    @BulkImportField(label = "Metadata Language", comment = BulkImportField.METADATA_LANGUAGE_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    @Field(norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class))
    @Column(name = "metadata_language", length = 100)
    private Language metadataLanguage;

    @BulkImportField(label = "Resource Language", comment = BulkImportField.RESOURCE_LANGAGE_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    @Field(norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class))
    @Column(name = "resource_language", length = 100)
    private Language resourceLanguage;

    @Enumerated(EnumType.STRING)
    @Field(norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class))
    @Column(name = "license_type", length = 128)
    @BulkImportField(label = BulkImportField.LICENSE_TYPE, required = true)
    private LicenseType licenseType;

    @Column(name = "license_text")
    @BulkImportField(label = BulkImportField.LICENSE_TEXT)
    @Type(type = "org.hibernate.type.StringClobType")
    @Lob
    private String licenseText;

    @Column(name = "external_reference", nullable = true)
    @XmlTransient
    private boolean externalReference;

    @BulkImportField(label = "Copy Located At", comment = BulkImportField.COPY_LOCATION_DESCRIPTION)
    @Column(name = "copy_location")
    @Length(max = 255)
    private String copyLocation;

    @Column(name = "last_uploaded")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUploaded;

    // currently just a 4 digit year.
    @Column(name = "date_created")
    @BulkImportField(label = BulkImportField.YEAR_LABEL, required = true, order = -10, comment = BulkImportField.YEAR_DESCRIPTION)
    @FieldBridge(impl = TdarPaddedNumberBridge.class)
    @Field(norms = Norms.NO, store = Store.YES, analyze = Analyze.NO)
    private Integer date = -1;

    @Column(name = "date_created_normalized")
    @FieldBridge(impl = TdarPaddedNumberBridge.class)
    @Field(norms = Norms.NO, store = Store.YES, name = QueryFieldNames.DATE_CREATED_DECADE, analyze = Analyze.NO)
    @XmlTransient
    private Integer dateNormalized = -1;

    // The institution providing this InformationResource
    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @JoinColumn(name = "provider_institution_id")
    @IndexedEmbedded
    private Institution resourceProviderInstitution;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @BulkImportField(label = "Publisher")
    @JoinColumn(name = "publisher_id")
    @IndexedEmbedded
    private Institution publisher;

    @BulkImportField(label = "Publisher Location")
    @Column(name = "publisher_location")
    @Length(max = 255)
    private String publisherLocation;

    @JoinColumn(name = "copyright_holder_id")
    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE })
    @BulkImportField(label = BulkImportField.COPYRIGHT_HOLDER, required = true, implementedSubclasses = { Person.class, Institution.class }, order = 1)
    private Creator copyrightHolder;

    // downward inheritance sections
    @Column(name = InvestigationType.INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingInvestigationInformation = false;
    @Column(name = SiteNameKeyword.INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingSiteInformation = false;
    @Column(name = MaterialKeyword.INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingMaterialInformation = false;
    @Column(name = OtherKeyword.INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingOtherInformation = false;
    @Column(name = CultureKeyword.INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingCulturalInformation = false;
    @Column(name = GeographicKeyword.INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingSpatialInformation = false;
    @Column(name = TemporalKeyword.INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingTemporalInformation = false;
    @Column(name = "inheriting_note_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingNoteInformation = false;
    @Column(name = "inheriting_identifier_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingIdentifierInformation = false;
    @Column(name = "inheriting_collection_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingCollectionInformation = false;

    @ManyToOne(optional = true)
    private DataTableColumn mappedDataKeyColumn;

    @Column
    @Length(max = 255)
    private String mappedDataKeyValue;

    @Transient
    @XmlTransient
    private Map<DataTableColumn, String> relatedDatasetData = new HashMap<>();

    public Language getMetadataLanguage() {
        return metadataLanguage;
    }

    public void setMetadataLanguage(Language metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
    }

    public Language getResourceLanguage() {
        return resourceLanguage;
    }

    public void setResourceLanguage(Language resourceLanguage) {
        this.resourceLanguage = resourceLanguage;
    }

    @XmlElement(name = "copyrightHolderRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Creator getCopyrightHolder() {
        return copyrightHolder;
    }

    public void setCopyrightHolder(Creator copyrightHolder) {
        this.copyrightHolder = copyrightHolder;
    }

    @XmlElement(name = "licenseType")
    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType;
        if (licenseType != LicenseType.OTHER) {
            setLicenseText(null);
        }
    }

    @XmlElement(name = "licenseText")
    public String getLicenseText() {
        if (licenseType == LicenseType.OTHER) {
            return licenseText;
        } else {
            return null;
        }
    }

    public void setLicenseText(String licenseText) {
        this.licenseText = licenseText;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer dateCreated) {
        this.date = dateCreated;
        if (dateCreated != null) {
            this.dateNormalized = Math.round(dateCreated / 10) * 10;
        } else {
            this.dateNormalized = null;
        }
    }

    public Integer getDateNormalized() {
        return dateNormalized;
    }

    @Deprecated
    public void setDateNormalized(Integer dateCreatedNormalized) {
        this.dateNormalized = dateCreatedNormalized;
    }

    public Institution getResourceProviderInstitution() {
        return resourceProviderInstitution;
    }

    public void setResourceProviderInstitution(Institution resourceProviderInstitution) {
        this.resourceProviderInstitution = resourceProviderInstitution;
    }

    @XmlElement(name = "projectRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public Project getProject() {
        if (project == null) {
            return Project.NULL;
        }
        return project;
    }

    @Field(name = QueryFieldNames.PROJECT_ID)
    @Analyzer(impl = KeywordAnalyzer.class)
    public Long getProjectId() {
        if (projectId == null) {
            projectId = getProject().getId();
        }
        return Long.valueOf(-1L).equals(projectId) ? null : projectId;
    }

    @Deprecated
    public void setProjectId(Long projectId) {
        // FIXME: jtd - added this method to assist w/ sensoryData xml creation export. In any other scenario you should probably be using setProject() to
        // implicitly set projectId.
        this.projectId = projectId;
    }

    @Transient
    // @Boost(1.5f)
    @Fields({
            @Field(name = QueryFieldNames.PROJECT_TITLE),
            @Field(name = QueryFieldNames.PROJECT_TITLE_AUTO, norms = Norms.NO, store = Store.YES, analyzer = @Analyzer(impl = AutocompleteAnalyzer.class))
    })
    public String getProjectTitle() {
        return getProject().getTitleSort();
    }

    @Transient
    @Field(name = QueryFieldNames.PROJECT_TITLE_SORT, norms = Norms.NO, store = Store.YES, analyze = Analyze.NO)
    public String getProjectTitleSort() {
        return getProject().getTitleSort() + " - " + getTitle();
    }

    public void setProject(Project project) {
        if (project == Project.NULL) {
            this.project = null;
        } else {
            this.project = project;
        }
    }

    /**
     * Returns true if this resource is an externally referenced resource,
     * signifying that there is no uploaded file. The URL should then
     * 
     * @return
     */
    public boolean isExternalReference() {
        return externalReference;
    }

    public void setExternalReference(boolean externalReference) {
        this.externalReference = externalReference;
    }

    public Date getLastUploaded() {
        return lastUploaded;
    }

    public void setLastUploaded(Date lastUploaded) {
        this.lastUploaded = lastUploaded;
    }

    // public Set<Document> getRelatedCitations() {
    // return relatedCitations;
    // }
    //
    // public void setRelatedCitations(Set<Document> relatedCitations) {
    // this.relatedCitations = relatedCitations;
    // }
    //
    // public Set<Document> getSourceCitations() {
    // return sourceCitations;
    // }
    //
    // public void setSourceCitations(Set<Document> sourceCitations) {
    // this.sourceCitations = sourceCitations;
    // }

    public int getTotalNumberOfFiles() {
        return informationResourceFiles.size();
    }

    public int getTotalNumberOfActiveFiles() {
        int count = 0;
        for (InformationResourceFile file : informationResourceFiles) {
            if (file.isDeleted())
                continue;
            count++;
        }
        return count;
    }

    @Override
    @JSONTransient
    public String getFormattedSourceInformation() {
        StringBuilder sb = new StringBuilder();

        appendIfNotBlank(sb, getPublisherLocation(), ".", "");
        appendIfNotBlank(sb, getPublisherName(), ":", "");

        if (getDate() != null && getDate().intValue() != -1) {
            appendIfNotBlank(sb, getDate().toString(), ".", "");
        }
        appendIfNotBlank(sb, getCopyLocation(), ".", "");
        return sb.toString();
    }

    @XmlElementWrapper(name = "informationResourceFiles")
    @XmlElement(name = "informationResourceFile")
    @JSONTransient
    public Set<InformationResourceFile> getInformationResourceFiles() {
        if (informationResourceFiles == null) {
            informationResourceFiles = new HashSet<InformationResourceFile>();
        }
        return informationResourceFiles;
    }

    @JSONTransient
    @XmlTransient
    public InformationResourceFile getFirstInformationResourceFile() {
        if (getInformationResourceFiles().isEmpty()) {
            return null;
        }
        return informationResourceFiles.iterator().next();
    }

    @JSONTransient
    @XmlTransient
    public Set<InformationResourceFile> getActiveInformationResourceFiles() {
        HashSet<InformationResourceFile> files = new HashSet<>();
        for (InformationResourceFile file : informationResourceFiles) {
            if (!file.isDeleted()) {
                files.add(file);
            }
        }
        return files;
    }

    public void setInformationResourceFiles(Set<InformationResourceFile> informationResourceFiles) {
        this.informationResourceFiles = informationResourceFiles;
    }

    public void add(InformationResourceFile informationResourceFile) {
        getInformationResourceFiles().add(informationResourceFile);
        logger.debug("adding information resource file: {} ({})", informationResourceFile, informationResourceFiles.size());
    }

    @JSONTransient
    @XmlTransient
    public Collection<InformationResourceFileVersion> getLatestVersions() {
        // FIXME: this method will become increasingly expensive as the number of files increases
        ArrayList<InformationResourceFileVersion> latest = new ArrayList<InformationResourceFileVersion>();
        for (InformationResourceFile irfile : getInformationResourceFiles()) {
            latest.addAll(irfile.getLatestVersions());
        }
        return latest;
    }

    public Collection<InformationResourceFileVersion> getLatestVersions(String type) {
        return getLatestVersions(VersionType.valueOf(type));
    }

    public Collection<InformationResourceFileVersion> getLatestVersions(VersionType type) {
        ArrayList<InformationResourceFileVersion> latest = new ArrayList<InformationResourceFileVersion>();
        for (InformationResourceFile irfile : getInformationResourceFiles()) {
            InformationResourceFileVersion irfileVersion = irfile.getCurrentVersion(type);
            if (irfileVersion != null) {
                latest.add(irfileVersion);
            }
        }
        return latest;
    }

    @JSONTransient
    @XmlTransient
    public InformationResourceFileVersion getLatestUploadedVersion() {
        Collection<InformationResourceFileVersion> latestUploadedVersions = getLatestUploadedVersions();
        if (CollectionUtils.isEmpty(latestUploadedVersions)) {
            logger.warn("No latest uploaded version for {}", this);
            return null;
        }
        return getLatestUploadedVersions().iterator().next();
    }

    @JSONTransient
    @XmlTransient
    public Collection<InformationResourceFileVersion> getLatestUploadedVersions() {
        return getLatestVersions(VersionType.UPLOADED);
    }

    @Field(store = Store.NO)
    @FieldBridge(impl = PersistentReaderBridge.class)
    @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)
    @Transient
    // @Boost(0.5f)
    @XmlTransient
    @JSONTransient
    public List<InformationResourceFileVersion> getContent() {
        logger.trace("getContent");
        List<InformationResourceFile> files = getPublicFiles();
        if (CollectionUtils.isEmpty(files)) {
            return null;
        }
        // List<InputStream> streams = new ArrayList<InputStream>();
        List<InformationResourceFileVersion> fileURIs = new ArrayList<InformationResourceFileVersion>();
        for (InformationResourceFile irFile : files) {
            try {
                if (irFile.getRestriction().isRestricted())
                    continue;
                InformationResourceFileVersion indexableVersion = irFile.getIndexableVersion();
                fileURIs.add(indexableVersion);
            } catch (Exception e) {
                logger.trace("an exception occured while reading file: {} ", e);
            }
        }
        return fileURIs;
    }

    @Field(norms = Norms.NO, store = Store.YES, name = QueryFieldNames.RESOURCE_ACCESS_TYPE, analyzer = @Analyzer(
            impl = TdarCaseSensitiveStandardAnalyzer.class))
    @Transient
    public ResourceAccessType getResourceAccessType() {
        int totalFiles = getNonDeletedFiles().size();
        int publicFiles = getPublicFiles().size();
        if (totalFiles > 0) {
            if (publicFiles == 0) {
                return ResourceAccessType.RESTRICTED;
            }
            if (publicFiles == totalFiles) {
                return ResourceAccessType.PUBLICALLY_ACCESSIBLE;
            }
            return ResourceAccessType.PARTIALLY_RESTRICTED;
        }
        return ResourceAccessType.CITATION;
    }

    @Transient
    @XmlTransient
    @JSONTransient
    public boolean isPublicallyAccessible() {
        return getResourceAccessType() == ResourceAccessType.PUBLICALLY_ACCESSIBLE;
    }

    @Transient
    public boolean getContainsFiles() {
        return hasFiles();
    }

    @Transient
    public boolean hasFiles() {
        return getInformationResourceFiles().size() > 0;
    }

    public boolean isInheritingInvestigationInformation() {
        return inheritingInvestigationInformation;
    }

    public void setInheritingInvestigationInformation(boolean inheritingInvestigationInformation) {
        this.inheritingInvestigationInformation = inheritingInvestigationInformation;
    }

    public boolean isInheritingSiteInformation() {
        return inheritingSiteInformation;
    }

    public void setInheritingSiteInformation(boolean inheritingSiteInformation) {
        this.inheritingSiteInformation = inheritingSiteInformation;
    }

    public boolean isInheritingMaterialInformation() {
        return inheritingMaterialInformation;
    }

    public void setInheritingMaterialInformation(boolean inheritingMaterialInformation) {
        this.inheritingMaterialInformation = inheritingMaterialInformation;
    }

    public boolean isInheritingOtherInformation() {
        return inheritingOtherInformation;
    }

    public void setInheritingOtherInformation(boolean inheritingOtherInformation) {
        this.inheritingOtherInformation = inheritingOtherInformation;
    }

    public boolean isInheritingCulturalInformation() {
        return inheritingCulturalInformation;
    }

    public void setInheritingCulturalInformation(boolean inheritingCulturalInformation) {
        this.inheritingCulturalInformation = inheritingCulturalInformation;
    }

    public boolean isInheritingSpatialInformation() {
        return inheritingSpatialInformation;
    }

    public void setInheritingSpatialInformation(boolean inheritingSpatialInformation) {
        this.inheritingSpatialInformation = inheritingSpatialInformation;
    }

    public boolean isInheritingTemporalInformation() {
        return inheritingTemporalInformation;
    }

    public void setInheritingTemporalInformation(boolean inheritingTemporalInformation) {
        this.inheritingTemporalInformation = inheritingTemporalInformation;
    }

    public String getCopyLocation() {
        return copyLocation;
    }

    public void setCopyLocation(String copyLocation) {
        this.copyLocation = copyLocation;
    }

    @IndexedEmbedded
    @Override
    public Set<InvestigationType> getActiveInvestigationTypes() {
        return isProjectVisible() && isInheritingInvestigationInformation() ? project.getInvestigationTypes() : getInvestigationTypes();
    }

    @Transient
    @XmlTransient
    public boolean isProjectVisible() {
        // FIXME: indexing was dying when project below was replaced with getProject()
        return getProject().isActive() || getProject().isDraft();
    }

    @IndexedEmbedded
    @Override
    public Set<SiteNameKeyword> getActiveSiteNameKeywords() {
        return isProjectVisible() && isInheritingSiteInformation() ? project.getSiteNameKeywords() : getSiteNameKeywords();
    }

    @IndexedEmbedded
    @Override
    public Set<SourceCollection> getActiveSourceCollections() {
        return isProjectVisible() && isInheritingCollectionInformation() ? project.getSourceCollections() : getSourceCollections();
    }

    @IndexedEmbedded
    @Override
    public Set<RelatedComparativeCollection> getActiveRelatedComparativeCollections() {
        return isProjectVisible() && isInheritingCollectionInformation() ? project.getRelatedComparativeCollections() : getRelatedComparativeCollections();
    }

    @IndexedEmbedded
    @Override
    public Set<SiteTypeKeyword> getActiveSiteTypeKeywords() {
        return isProjectVisible() && isInheritingSiteInformation() ? project.getSiteTypeKeywords() : getSiteTypeKeywords();
    }

    public Set<SiteTypeKeyword> getActiveApprovedSiteTypeKeywords() {
        return isProjectVisible() && isInheritingSiteInformation() ? project.getApprovedSiteTypeKeywords() : getApprovedSiteTypeKeywords();
    }

    public Set<SiteTypeKeyword> getActiveUncontrolledSiteTypeKeywords() {
        return isProjectVisible() && isInheritingSiteInformation() ? project.getUncontrolledSiteTypeKeywords() : getUncontrolledSiteTypeKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<MaterialKeyword> getActiveMaterialKeywords() {
        return isProjectVisible() && isInheritingMaterialInformation() ? project.getMaterialKeywords() : getMaterialKeywords();
    }

    @Override
    @IndexedEmbedded(targetElement = OtherKeyword.class)
    public Set<OtherKeyword> getActiveOtherKeywords() {
        return isProjectVisible() && isInheritingOtherInformation() ? project.getOtherKeywords() : getOtherKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<CultureKeyword> getActiveCultureKeywords() {
        return isProjectVisible() && isInheritingCulturalInformation() ? project.getCultureKeywords() : getCultureKeywords();
    }

    public Set<CultureKeyword> getActiveApprovedCultureKeywords() {
        return isProjectVisible() && isInheritingCulturalInformation() ? project.getApprovedCultureKeywords() : getApprovedCultureKeywords();
    }

    public Set<ResourceNote> getActiveResourceNotes() {
        return isProjectVisible() && isInheritingNoteInformation() ? project.getResourceNotes() : getResourceNotes();
    }

    public Set<ResourceAnnotation> getActiveResourceAnnotations() {
        return isProjectVisible() && isInheritingIdentifierInformation() ? project.getResourceAnnotations() : getResourceAnnotations();
    }

    public Set<CultureKeyword> getActiveUncontrolledCultureKeywords() {
        return isProjectVisible() && isInheritingCulturalInformation() ? project.getUncontrolledCultureKeywords() : getUncontrolledCultureKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<GeographicKeyword> getActiveGeographicKeywords() {
        return isProjectVisible() && isInheritingSpatialInformation() ? project.getGeographicKeywords() : getGeographicKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<LatitudeLongitudeBox> getActiveLatitudeLongitudeBoxes() {
        return isProjectVisible() && isInheritingSpatialInformation() ? project.getLatitudeLongitudeBoxes() : getLatitudeLongitudeBoxes();
    }

    @Override
    @IndexedEmbedded
    public Set<TemporalKeyword> getActiveTemporalKeywords() {
        return isProjectVisible() && isInheritingTemporalInformation() ? project.getTemporalKeywords() : getTemporalKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<CoverageDate> getActiveCoverageDates() {
        return isProjectVisible() && isInheritingTemporalInformation() ? project.getCoverageDates() : getCoverageDates();
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        ArrayList<String> allProperties = new ArrayList<String>(Arrays.asList(super.getIncludedJsonProperties()));
        allProperties.addAll(Arrays.asList(JSON_PROPERTIES));
        return allProperties.toArray(new String[allProperties.size()]);
    }

    @Transient
    @JSONTransient
    @Override
    public boolean hasConfidentialFiles() {
        return !getConfidentialFiles().isEmpty();
    }

    @Transient
    @JSONTransient
    @Override
    public boolean hasEmbargoedFiles() {
        for (InformationResourceFile file : getConfidentialFiles()) {
            if (file.isEmbargoed())
                return true;
        }
        return false;
    }

    @Transient
    public List<InformationResourceFile> getFilesWithRestrictions(boolean confidential) {
        List<InformationResourceFile> confidentialFiles = new ArrayList<InformationResourceFile>();
        List<InformationResourceFile> publicFiles = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile irFile : getNonDeletedFiles()) {
            if (irFile.isPublic()) {
                publicFiles.add(irFile);
            } else {
                confidentialFiles.add(irFile);
            }
        }
        if (confidential) {
            return confidentialFiles;
        } else {
            return publicFiles;
        }
    }

    @Transient
    @JSONTransient
    public List<InformationResourceFile> getConfidentialFiles() {
        return getFilesWithRestrictions(true);
    }

    @Override
    @XmlTransient
    @JSONTransient
    public String getAdditonalKeywords() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCopyLocation()).append(" ").append(date);
        if (getResourceProviderInstitution() != null) {
            sb.append(" ").append(getResourceProviderInstitution().getName());
        }
        sb.append(" ").append(getPublisherName());

        if (MapUtils.isNotEmpty(relatedDatasetData)) {
            for (String v : relatedDatasetData.values()) {
                sb.append(v);
                sb.append(" ");
            }
        }

        // if (getProject() != null) {
        // getProject().getTitle();
        // }
        return sb.toString();
    }

    @Transient
    @XmlTransient
    @JSONTransient
    public List<InformationResourceFile> getPublicFiles() {
        return getFilesWithRestrictions(false);
    }

    @Override
    @JSONTransient
    public boolean isValidForController() {
        if (date == null) {
            throw new TdarValidationException("Specifying a \"Created Date\" is required for this " + getResourceType());
        }
        return super.isValidForController();
    }

    public DataTableColumn getMappedDataKeyColumn() {
        return mappedDataKeyColumn;
    }

    public void setMappedDataKeyColumn(DataTableColumn mappedDataKeyColumn) {
        this.mappedDataKeyColumn = mappedDataKeyColumn;
    }

    public String getMappedDataKeyValue() {
        return mappedDataKeyValue;
    }

    public void setMappedDataKeyValue(String mappedDataKeyValue) {
        this.mappedDataKeyValue = mappedDataKeyValue;
    }

    @Field(norms = Norms.YES, store = Store.NO)
    @FieldBridge(impl = StringMapBridge.class)
    public Map<DataTableColumn, String> getRelatedDatasetData() {
        return relatedDatasetData;
    }

    public void setRelatedDatasetData(Map<DataTableColumn, String> relatedDatasetData) {
        this.relatedDatasetData = relatedDatasetData;
    }

    @Transient
    @XmlTransient
    @JSONTransient
    public boolean isInheritingSomeMetadata() {
        return (inheritingCulturalInformation || inheritingInvestigationInformation || inheritingMaterialInformation || inheritingOtherInformation ||
                inheritingSiteInformation || inheritingSpatialInformation || inheritingTemporalInformation || inheritingIdentifierInformation || inheritingNoteInformation);
    }

    @Transient
    @JSONTransient
    public List<Obfuscatable> obfuscate() {
        // don't claim to inherit data from Projects which are inactive
        if (!isProjectVisible()) {
            setProject(Project.NULL);
            // setting the project to null should be enough...
            setInheritingCulturalInformation(false);
            setInheritingInvestigationInformation(false);
            setInheritingMaterialInformation(false);
            setInheritingOtherInformation(false);
            setInheritingSiteInformation(false);
            setInheritingSpatialInformation(false);
            setInheritingTemporalInformation(false);
            setInheritingIdentifierInformation(false);
            setInheritingNoteInformation(false);
        }
        List<Obfuscatable> toObfuscate = super.obfuscate();
        return toObfuscate;
    }

    @Override
    @JSONTransient
    @XmlTransient
    public List<String> getCreatorRoleIdentifiers() {
        List<String> list = super.getCreatorRoleIdentifiers();
        list.add(ResourceCreator.getCreatorRoleIdentifier(getResourceProviderInstitution(), ResourceCreatorRole.RESOURCE_PROVIDER));
        list.add(ResourceCreator.getCreatorRoleIdentifier(getPublisher(), ResourceCreatorRole.PUBLISHER));
        return list;
    }

    @Override
    @XmlTransient
    public List<Creator> getRelatedCreators() {
        List<Creator> creators = super.getRelatedCreators();
        creators.add(getResourceProviderInstitution());
        creators.add(getPublisher());
        return creators;
    }

    public boolean isInheritingNoteInformation() {
        return inheritingNoteInformation;
    }

    public void setInheritingNoteInformation(boolean inheritingNoteInformation) {
        this.inheritingNoteInformation = inheritingNoteInformation;
    }

    public boolean isInheritingIdentifierInformation() {
        return inheritingIdentifierInformation;
    }

    public void setInheritingIdentifierInformation(boolean inheritingIdentifierInformation) {
        this.inheritingIdentifierInformation = inheritingIdentifierInformation;
    }

    public boolean isInheritingCollectionInformation() {
        return inheritingCollectionInformation;
    }

    public void setInheritingCollectionInformation(boolean inheritingCollectionInformation) {
        this.inheritingCollectionInformation = inheritingCollectionInformation;
    }

    // shortcut for non-deleted, visible files
    @Transient
    @JSONTransient
    @XmlTransient
    @IndexedEmbedded
    public List<InformationResourceFile> getVisibleFilesWithThumbnails() {
        ArrayList<InformationResourceFile> visibleFiles = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile irfile : getVisibleFiles()) {
            if (irfile.getLatestThumbnail() != null) {
                visibleFiles.add(irfile);
            }
        }
        return visibleFiles;
    }

    // shortcut for non-deleted, visible files
    @Transient
    @JSONTransient
    @XmlTransient
    public List<InformationResourceFile> getVisibleFiles() {
        ArrayList<InformationResourceFile> visibleFiles = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile irfile : getInformationResourceFiles()) {
            if (irfile.isViewable() && !irfile.isDeleted()) {
                visibleFiles.add(irfile);
            }
        }
        return visibleFiles;
    }

    private transient InformationResourceFileVersion primaryThumbnail = null;
    private transient Boolean hasPrimaryThumbnail = null;

    // get the latest version of the first non-deleted thumbnail (or null)
    @Transient
    @JSONTransient
    @XmlTransient
    public InformationResourceFileVersion getPrimaryThumbnail() {
        if (hasPrimaryThumbnail != null) {
            return primaryThumbnail;
        }
        hasPrimaryThumbnail = Boolean.FALSE;
        for (InformationResourceFile firstVisible : getVisibleFilesWithThumbnails()) {
            hasPrimaryThumbnail = Boolean.TRUE;
            primaryThumbnail = firstVisible.getLatestThumbnail();
        }
        return primaryThumbnail;
    }

    @Transient
    @XmlTransient
    @JSONTransient
    public List<InformationResourceFile> getNonDeletedFiles() {
        List<InformationResourceFile> files = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile irf : getInformationResourceFiles()) {
            if (!irf.isDeleted()) {
                files.add(irf);
            }
        }
        return files;
    }

    @Transient
    @Override
    // we consider a record to be citation record if it doesn't have any file attachments.
    public boolean isCitationRecord() {
        return getResourceAccessType() == ResourceAccessType.CITATION;
    }

    public Institution getPublisher() {
        return publisher;
    }

    public void setPublisher(Institution publisher) {
        this.publisher = publisher;
    }

    public String getPublisherLocation() {
        return publisherLocation;
    }

    public void setPublisherLocation(String publisherLocation) {
        this.publisherLocation = publisherLocation;
    }

    public String getPublisherName() {
        if (publisher != null) {
            return publisher.getName();
        }
        return null;
    }

    @Override
    public <R extends Resource> void copyImmutableFieldsFrom(R resource_) {
        super.copyImmutableFieldsFrom(resource_);
        InformationResource resource = (InformationResource) resource_;
        this.getInformationResourceFiles().addAll(new HashSet<InformationResourceFile>(resource.getInformationResourceFiles()));
        this.setPublisher(resource.getPublisher());
        this.setResourceProviderInstitution(resource.getResourceProviderInstitution());

    };

    @XmlTransient
    @JSONTransient
    public List<InformationResourceFile> getFilesWithProcessingErrors() {
        List<InformationResourceFile> files = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile file : getInformationResourceFiles()) {
            if (file.getStatus() == FileStatus.PROCESSING_ERROR || file.getStatus() == FileStatus.PROCESSING_WARNING) {
                files.add(file);
            }
        }
        return files;
    }

    @XmlTransient
    @JSONTransient
    public List<InformationResourceFile> getFilesWithFatalProcessingErrors() {
        List<InformationResourceFile> files = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile file : getInformationResourceFiles()) {
            if (file.getStatus() == FileStatus.PROCESSING_ERROR) {
                files.add(file);
            }
        }
        return files;
    }

    @Transient
    @XmlTransient
    public Set<ResourceCreator> getContacts() {
        return getResourceCreators(ResourceCreatorRole.CONTACT);
    }
    
    /**
     * Override this if you need to pass resource specific information on to the work flow process.
     * Make a new instance of the resource, and then copy the fields across that will be needed by the work flow process
     * @see Archive#getTransientCopyForWorkflow() for an implementation
     * @return <b>The default is null!</b> A copy of the information resource that will be serialised and sent to the work flow.
     */
    @SuppressWarnings("static-method")
    @Transient
    @XmlTransient
    public InformationResource getTransientCopyForWorkflow() {
        return null;
    }
    
    /**
     * Override this method to write back the fields that may have been changed in the transient copy
     * @param transientCopy
     */
    public void updateFromTransientResource(InformationResource transientCopy) {
        // Should we throw an exception if we are here ?
    }
}
