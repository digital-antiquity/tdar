package org.tdar.core.bean.resource;

import java.net.URI;
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
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.DynamicBoost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.Store;
import org.tdar.core.bean.BulkImportField;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.configuration.JSONTransient;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.search.index.analyzer.LowercaseWhiteSpaceStandardAnalyzer;
import org.tdar.search.index.analyzer.TdarCaseSensitiveStandardAnalyzer;
import org.tdar.search.index.boost.InformationResourceBoostStrategy;
import org.tdar.search.index.bridge.PersistentReaderBridge;
import org.tdar.search.index.bridge.StringMapBridge;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

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
    private static final String[] JSON_PROPERTIES = { "inheritingCulturalInformation", "inheritingInvestigationInformation", "inheritingMaterialInformation",
            "inheritingOtherInformation", "inheritingSiteInformation", "inheritingSpatialInformation", "inheritingTemporalInformation",
    };
    public static final int EMBARGO_PERIOD_YEARS = 5;

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
    @XStreamOmitField
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

    @OneToMany(mappedBy = "informationResource", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @OrderBy("sequenceNumber asc")
    @JSONTransient
    private Set<InformationResourceFile> informationResourceFiles = new LinkedHashSet<InformationResourceFile>();

    @BulkImportField(label = "Metadata Language", comment = BulkImportField.METADATA_LANGUAGE_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_language")
    private Language metadataLanguage;

    @BulkImportField(label = "Resource Language", comment = BulkImportField.RESOURCE_LANGAGE_DESCRIPTION)
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_language")
    private Language resourceLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type")
    private LicenseType licenseType;

    @Column(name = "license_text")
    @Type(type = "org.hibernate.type.StringClobType")
    private String licenseText;

    @Column(name = "available_to_public")
    private boolean availableToPublic;

    @Column(name = "external_reference", nullable = true)
    private boolean externalReference;

    @BulkImportField(label = "Copy Located At", comment = BulkImportField.COPY_LOCATION_DESCRIPTION)
    @Column(name = "copy_location")
    private String copyLocation;

    @Column(name = "last_uploaded")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUploaded;

    // a date in standard form that a resource will become public if availableToPublic was set to false.
    // This date may be extended by the publisher but will not extend past the publisher's death unless
    // special arrangements are made.
    @Column(name = "date_made_public")
    private Date dateMadePublic;

    // currently just a 4 digit year.
    @Column(name = "date_created")
    @BulkImportField(label = "Date Created (Year)", required = true, order = -10, comment = BulkImportField.YEAR_DESCRIPTION)
    @NumericField
    @Field(index = Index.UN_TOKENIZED, store = Store.YES)
    // @FieldBridge(impl = TdarPaddedNumberBridge.class)
    private Integer date = -1;

    // The institution providing this InformationResource
    @ManyToOne
    @JoinColumn(name = "provider_institution_id")
    @IndexedEmbedded
    private Institution resourceProviderInstitution;

    @JoinColumn(name = "copyright_holder_id")
    @ManyToOne(optional = true)
    // @BulkImportField(implementedSubclasses = { Person.class, Institution.class }, label = "Primary Copyright Holder", order = 1)
    private Creator copyrightHolder;

    // downward inheritance sections
    @Column(name = "inheriting_investigation_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingInvestigationInformation;
    @Column(name = "inheriting_site_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingSiteInformation;
    @Column(name = "inheriting_material_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingMaterialInformation;
    @Column(name = "inheriting_other_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingOtherInformation;
    @Column(name = "inheriting_cultural_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingCulturalInformation;
    @Column(name = "inheriting_spatial_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingSpatialInformation;
    @Column(name = "inheriting_temporal_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingTemporalInformation;

    @ManyToOne(optional = true)
    private DataTableColumn mappedDataKeyColumn;

    @Column
    private String mappedDataKeyValue;

    @Transient
    private Map<DataTableColumn, String> relatedDatasetData = new HashMap<DataTableColumn, String>();

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

    public boolean isAvailableToPublic() {
        return availableToPublic;
    }

    public void setAvailableToPublic(boolean availableToPublic) {
        this.availableToPublic = availableToPublic;
    }

    public Date getDateMadePublic() {
        return dateMadePublic;
    }

    public void setDateMadePublic(Date dateMadePublic) {
        this.dateMadePublic = dateMadePublic;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer dateCreated) {
        this.date = dateCreated;
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

    @Transient
    @Field(name = QueryFieldNames.PROJECT_TITLE_SORT, index = Index.UN_TOKENIZED, store = Store.YES)
    public String getProjectTitle() {
        if (getProject() != null && getProject() != Project.NULL) {
            return getProject().getTitleSort();
        }
        return "";
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

    @Transient
    @Field(name = QueryFieldNames.PROJECT_ID)
    @Analyzer(impl = KeywordAnalyzer.class)
    public Long getProjectId() {
        if (projectId != null)
            return projectId;
        if (project == null || project == Project.NULL)
            return null;
        projectId = project.getId();
        return projectId;
    }

    @Deprecated
    public void setProjectId(Long projectId) {
        // FIXME: jtd - added this method to assist w/ sensoryData xml creation export. In any other scenario you should probably be using setProject() to
        // implicitly set projectId.
        this.projectId = projectId;
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
    public InformationResourceFile getFirstInformationResourceFile() {
        if (getInformationResourceFiles().isEmpty()) {
            return null;
        }
        return informationResourceFiles.iterator().next();
    }

    public void setInformationResourceFiles(Set<InformationResourceFile> informationResourceFiles) {
        this.informationResourceFiles = informationResourceFiles;
    }

    public void add(InformationResourceFile informationResourceFile) {
        getInformationResourceFiles().add(informationResourceFile);
        logger.debug("adding information resource file: {} ({})", informationResourceFile, informationResourceFiles.size());
    }

    @JSONTransient
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
    public InformationResourceFileVersion getLatestUploadedVersion() {
        Collection<InformationResourceFileVersion> latestUploadedVersions = getLatestUploadedVersions();
        if (CollectionUtils.isEmpty(latestUploadedVersions)) {
            logger.warn("No latest uploaded version for {}", this);
            return null;
        }
        return getLatestUploadedVersions().iterator().next();
    }

    @JSONTransient
    public Collection<InformationResourceFileVersion> getLatestUploadedVersions() {
        return getLatestVersions(VersionType.UPLOADED);
    }

    @Field(store = Store.NO)
    @FieldBridge(impl = PersistentReaderBridge.class)
    @Analyzer(impl = LowercaseWhiteSpaceStandardAnalyzer.class)
    @Transient
    @Boost(0.5f)
    @XmlTransient
    @JSONTransient
    public List<URI> getContent() {
        logger.trace("getContent");
        if (!isAvailableToPublic()) {
            return null;
        }
        // List<InputStream> streams = new ArrayList<InputStream>();
        List<URI> fileURIs = new ArrayList<URI>();
        for (InformationResourceFile irFile : getPublicFiles()) {
            try {
                InformationResourceFileVersion indexableVersion = irFile.getIndexableVersion();
                if (indexableVersion.getFile().exists()) {
                    fileURIs.add(indexableVersion.getFile().toURI());
                    logger.debug("getting indexed content for " + getId() + ": length:" + ("" + indexableVersion.getIndexableContent()).length());
                }
            } catch (Exception e) {
                logger.trace("an exception occured while reading file: {} ", e);
            }
        }
        return fileURIs;
    }

    @Field(index = Index.UN_TOKENIZED, store = Store.YES, name = QueryFieldNames.RESOURCE_ACCESS_TYPE)
    @Analyzer(impl = TdarCaseSensitiveStandardAnalyzer.class)
    @Transient
    // FIXME: This should work properly with the analyzer above without the toLowerCase()
    public ResourceAccessType getResourceAccessType() {
        int totalFiles = getInformationResourceFiles().size();
        int publicFiles = getPublicFiles().size();
        if (totalFiles > 0) {
            if (publicFiles == 0 || !isAvailableToPublic()) {
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
        return isProjectActive() && isInheritingInvestigationInformation() ? project.getInvestigationTypes() : getInvestigationTypes();
    }

    @Transient
    @XmlTransient
    private boolean isProjectActive() {
        // FIXME: indexing was dying when project below was replaced with getProject()
        if (project != null && project != Project.NULL && project.isActive()) {
            return true;
        }
        return false;
    }

    @IndexedEmbedded
    @Override
    public Set<SiteNameKeyword> getActiveSiteNameKeywords() {
        return isProjectActive() && isInheritingSiteInformation() ? project.getSiteNameKeywords() : getSiteNameKeywords();
    }

    @IndexedEmbedded
    @Override
    public Set<SiteTypeKeyword> getActiveSiteTypeKeywords() {
        return isProjectActive() && isInheritingSiteInformation() ? project.getSiteTypeKeywords() : getSiteTypeKeywords();
    }

    public Set<SiteTypeKeyword> getActiveApprovedSiteTypeKeywords() {
        return isProjectActive() && isInheritingSiteInformation() ? project.getApprovedSiteTypeKeywords() : getApprovedSiteTypeKeywords();
    }

    public Set<SiteTypeKeyword> getActiveUncontrolledSiteTypeKeywords() {
        return isProjectActive() && isInheritingSiteInformation() ? project.getUncontrolledSiteTypeKeywords() : getUncontrolledSiteTypeKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<MaterialKeyword> getActiveMaterialKeywords() {
        return isProjectActive() && isInheritingMaterialInformation() ? project.getMaterialKeywords() : getMaterialKeywords();
    }

    @Override
    @IndexedEmbedded(targetElement = OtherKeyword.class)
    public Set<OtherKeyword> getActiveOtherKeywords() {
        return isProjectActive() && isInheritingOtherInformation() ? project.getOtherKeywords() : getOtherKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<CultureKeyword> getActiveCultureKeywords() {
        return isProjectActive() && isInheritingCulturalInformation() ? project.getCultureKeywords() : getCultureKeywords();
    }

    public Set<CultureKeyword> getActiveApprovedCultureKeywords() {
        return isProjectActive() && isInheritingCulturalInformation() ? project.getApprovedCultureKeywords() : getApprovedCultureKeywords();
    }

    public Set<CultureKeyword> getActiveUncontrolledCultureKeywords() {
        return isProjectActive() && isInheritingCulturalInformation() ? project.getUncontrolledCultureKeywords() : getUncontrolledCultureKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<GeographicKeyword> getActiveGeographicKeywords() {
        return isProjectActive() && isInheritingSpatialInformation() ? project.getGeographicKeywords() : getGeographicKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<LatitudeLongitudeBox> getActiveLatitudeLongitudeBoxes() {
        return isProjectActive() && isInheritingSpatialInformation() ? project.getLatitudeLongitudeBoxes() : getLatitudeLongitudeBoxes();
    }

    @Override
    @IndexedEmbedded
    public Set<TemporalKeyword> getActiveTemporalKeywords() {
        return isProjectActive() && isInheritingTemporalInformation() ? project.getTemporalKeywords() : getTemporalKeywords();
    }

    @Override
    @IndexedEmbedded
    public Set<CoverageDate> getActiveCoverageDates() {
        return isProjectActive() && isInheritingTemporalInformation() ? project.getCoverageDates() : getCoverageDates();
    }

    @Override
    protected String[] getIncludedJsonProperties() {
        ArrayList<String> allProperties = new ArrayList<String>(Arrays.asList(super.getIncludedJsonProperties()));
        allProperties.addAll(Arrays.asList(JSON_PROPERTIES));
        return allProperties.toArray(new String[allProperties.size()]);
    }

    @Transient
    @JSONTransient
    public boolean hasConfidentialFiles() {
        return !getConfidentialFiles().isEmpty();
    }

    @Transient
    public List<InformationResourceFile> getFilesWithRestrictions(boolean confidential) {
        List<InformationResourceFile> confidentialFiles = new ArrayList<InformationResourceFile>();
        List<InformationResourceFile> publicFiles = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile irFile : getInformationResourceFiles()) {
            if (irFile.isConfidential()) {
                confidentialFiles.add(irFile);
            } else {
                publicFiles.add(irFile);
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
            throw new TdarRecoverableRuntimeException("Specifying a \"Created Date\" is required for this " + getResourceType());
        }
        return super.isValidForController();
    }

    @Override
    @Transient
    @JSONTransient
    public String getAdditionalUsersWhoCanModify() {
        if (getProject() != null) {
            return getProject().getAdditionalUsersWhoCanModify();
        }
        return "";
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

    @Field(index = Index.TOKENIZED, store = Store.NO)
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
                inheritingSiteInformation || inheritingSpatialInformation || inheritingTemporalInformation);
    }

    @Transient
    @JSONTransient
    public List<Obfuscatable> obfuscate() {
        // don't claim to inherit data from Projects which are inactive
        if (!isProjectActive()) {
            setProject(Project.NULL);
            // setting the project to null should be enough...
            setInheritingCulturalInformation(false);
            setInheritingInvestigationInformation(false);
            setInheritingMaterialInformation(false);
            setInheritingOtherInformation(false);
            setInheritingSiteInformation(false);
            setInheritingSpatialInformation(false);
            setInheritingTemporalInformation(false);
        }
        List<Obfuscatable> toObfuscate = super.obfuscate();
        return toObfuscate;
    }
}
