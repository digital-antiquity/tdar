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
import javax.persistence.Index;
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

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.citation.RelatedComparativeCollection;
import org.tdar.core.bean.citation.SourceCollection;
import org.tdar.core.bean.coverage.CoverageDate;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Institution;
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
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.FileStatus;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;
import org.tdar.utils.json.JsonLookupFilter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

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
@Table(name = "information_resource", indexes = {
        @Index(name = "infores_projid", columnList = "project_id, id"),
        @Index(name = "infores_provid", columnList = "provider_institution_id"),
        @Index(name = "ires_copyright", columnList = "copyright_holder_id"),
        // FIXME: prod database has two indexes on these fields with different names. Remove if they are redundant(as well as the @Index annotation)
        // @Index(name = "ires_provicer", columnList={"provider_institution_id"}),
        // @Index(name = "infores_provid", columnList={"provider_institution_id"}),
        @Index(name = "ires_publisher", columnList = "publisher_id")
})
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class InformationResource extends Resource {

    public static final String LICENSE_TEXT = "LICENSE_TEXT";
    public static final String LICENSE_TYPE = "LICENSE_TYPE";
    public static final String COPYRIGHT_HOLDER = "COPYRIGHT_HOLDER";

    private static final long serialVersionUID = -1534799746444826257L;

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

    @ManyToOne(optional = true, cascade = { CascadeType.MERGE, CascadeType.DETACH })
    private Project project;

    // FIXME: cascade "delete" ?
    @OneToMany(mappedBy = "informationResource", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @OrderBy("sequenceNumber asc")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.InformationResource.informationResourceFiles")
    private Set<InformationResourceFile> informationResourceFiles = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "metadata_language", length = FieldLength.FIELD_LENGTH_100)
    private Language metadataLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_language", length = FieldLength.FIELD_LENGTH_100)
    private Language resourceLanguage;

    @Enumerated(EnumType.STRING)
    @Column(name = "license_type", length = FieldLength.FIELD_LENGTH_128)
    private LicenseType licenseType;

    @Column(name = "license_text")
    @Type(type = "org.hibernate.type.TextType")
    @Lob
    private String licenseText;

    @Length(max = FieldLength.FIELD_LENGTH_255)
    @Column(name = "external_doi")
    private String doi;

    @Column(name = "external_reference", nullable = true)
    @XmlTransient
    private boolean externalReference;

    @Column(name = "copy_location")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String copyLocation;

    @Column(name = "last_uploaded")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUploaded;

    // currently just a 4 digit year.
    @Column(name = "date_created")
    @JsonView(JsonLookupFilter.class)
    private Integer date = -1;

    @Column(name = "date_created_normalized")
    @XmlTransient
    private Integer dateNormalized = -1;

    // The institution providing this InformationResource
    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "provider_institution_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Institution resourceProviderInstitution;

    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(name = "publisher_id")
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Institution publisher;

    @Column(name = "publisher_location")
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String publisherLocation;

    @JoinColumn(name = "copyright_holder_id")
    @ManyToOne(optional = true, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private Creator<?> copyrightHolder;

    public static final String INVESTIGATION_TYPE_INHERITANCE_TOGGLE = "inheriting_investigation_information";
    public static final String SITE_NAME_INHERITANCE_TOGGLE = "inheriting_site_information";
    public static final String MATERIAL_TYPE_INHERITANCE_TOGGLE = "inheriting_material_information";
    public static final String OTHER_INHERITANCE_TOGGLE = "inheriting_other_information";
    public static final String GEOGRAPHIC_INHERITANCE_TOGGLE = "inheriting_spatial_information";
    public static final String CULTURE_INHERITANCE_TOGGLE = "inheriting_cultural_information";
    public static final String TEMPORAL_INHERITANCE_TOGGLE = "inheriting_temporal_information";

    private transient List<FileProxy> fileProxies = new ArrayList<>();
    @Transient
    private transient ResourceAccessType transientAccessType;

    // downward inheritance sections
    @Column(name = INVESTIGATION_TYPE_INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingInvestigationInformation = false;
    @Column(name = SITE_NAME_INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingSiteInformation = false;
    @Column(name = MATERIAL_TYPE_INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingMaterialInformation = false;
    @Column(name = OTHER_INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingOtherInformation = false;
    @Column(name = CULTURE_INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingCulturalInformation = false;

    @Column(name = GEOGRAPHIC_INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingSpatialInformation = false;

    @Column(name = TEMPORAL_INHERITANCE_TOGGLE, nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingTemporalInformation = false;

    @Column(name = "inheriting_note_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingNoteInformation = false;

    @Column(name = "inheriting_identifier_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingIdentifierInformation = false;

    @Column(name = "inheriting_collection_information", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingCollectionInformation = false;

    @Column(name = "inheriting_individual_institutional_credit", nullable = false, columnDefinition = "boolean default FALSE")
    private boolean inheritingIndividualAndInstitutionalCredit = false;

    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    @ManyToOne(optional = true)
    private DataTableColumn mappedDataKeyColumn;

    @Column
    @Length(max = FieldLength.FIELD_LENGTH_255)
    private String mappedDataKeyValue;

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
    public Creator<?> getCopyrightHolder() {
        return copyrightHolder;
    }

    public void setCopyrightHolder(Creator<?> copyrightHolder) {
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
            this.dateNormalized = (int) (Math.floor(dateCreated.floatValue() / 10f) * 10);
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

    @XmlTransient
    public Long getProjectId() {
        if (PersistableUtils.isNotNullOrTransient(getProject())) {
            return getProject().getId();
        }
        return null;
    }

    @Transient
    public String getProjectTitle() {
        return getProject().getTitle();
    }

    @Transient
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

    public int getTotalNumberOfFiles() {
        return informationResourceFiles.size();
    }

    public int getTotalNumberOfActiveFiles() {
        int count = 0;
        for (InformationResourceFile file : informationResourceFiles) {
            if (file.isDeleted()) {
                continue;
            }
            count++;
        }
        return count;
    }

    @XmlElementWrapper(name = "informationResourceFiles")
    @XmlElement(name = "informationResourceFile")
    public Set<InformationResourceFile> getInformationResourceFiles() {
        return informationResourceFiles;
    }

    @XmlTransient
    @JsonIgnore
    public InformationResourceFile getFirstInformationResourceFile() {
        if (getInformationResourceFiles().isEmpty()) {
            return null;
        }
        return informationResourceFiles.iterator().next();
    }

    @XmlTransient
    @JsonIgnore
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

    @XmlTransient
    @JsonIgnore
    public InformationResourceFileVersion getLatestUploadedVersion() {
        Collection<InformationResourceFileVersion> latestUploadedVersions = getLatestUploadedVersions();
        if (CollectionUtils.isEmpty(latestUploadedVersions)) {
            logger.warn("No latest uploaded version for {}", this);
            return null;
        }
        return getLatestUploadedVersions().iterator().next();
    }

    @XmlTransient
    @JsonIgnore
    public Collection<InformationResourceFileVersion> getLatestUploadedVersions() {
        return getLatestVersions(VersionType.UPLOADED);
    }

    @Transient
    @JsonIgnore
    @XmlTransient
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
                if (irFile.getRestriction().isRestricted()) {
                    continue;
                }
                InformationResourceFileVersion indexableVersion = irFile.getIndexableVersion();
                fileURIs.add(indexableVersion);
            } catch (Exception e) {
                logger.trace("an exception occurred while reading file: {} ", e);
            }
        }
        return fileURIs;
    }

    @Transient
    public ResourceAccessType getResourceAccessType() {
        if (transientAccessType != null) {
            return transientAccessType;
        }
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

    @Override
    public Set<InvestigationType> getActiveInvestigationTypes() {
        return isProjectVisible() && isInheritingInvestigationInformation() ? project.getInvestigationTypes() : getInvestigationTypes();
    }

    @Override
    public Set<ResourceCreator> getActiveIndividualAndInstitutionalCredit() {
        return isProjectVisible() && isInheritingIndividualAndInstitutionalCredit() ? project.getIndividualAndInstitutionalCredit()
                : getIndividualAndInstitutionalCredit();
    }

    @Override
    public Set<ResourceCreator> getActiveResourceCreators() {
        Set<ResourceCreator> local = new HashSet<ResourceCreator>(super.getResourceCreators());
        if (isProjectVisible() && isInheritingIndividualAndInstitutionalCredit()) {
            local.addAll(project.getIndividualAndInstitutionalCredit());
        }
        return local;
    }

    @Transient
    @XmlTransient
    public boolean isProjectVisible() {
        // FIXME: indexing was dying when project below was replaced with getProject()
        return getProject().isActive() || getProject().isDraft();
    }

    @Override
    public Set<SiteNameKeyword> getActiveSiteNameKeywords() {
        return isProjectVisible() && isInheritingSiteInformation() ? project.getSiteNameKeywords() : getSiteNameKeywords();
    }

    @Override
    public Set<SourceCollection> getActiveSourceCollections() {
        return isProjectVisible() && isInheritingCollectionInformation() ? project.getSourceCollections() : getSourceCollections();
    }

    @Override
    public Set<RelatedComparativeCollection> getActiveRelatedComparativeCollections() {
        return isProjectVisible() && isInheritingCollectionInformation() ? project.getRelatedComparativeCollections() : getRelatedComparativeCollections();
    }

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
    public Set<MaterialKeyword> getActiveMaterialKeywords() {
        return isProjectVisible() && isInheritingMaterialInformation() ? project.getMaterialKeywords() : getMaterialKeywords();
    }

    @Override
    public Set<OtherKeyword> getActiveOtherKeywords() {
        return isProjectVisible() && isInheritingOtherInformation() ? project.getOtherKeywords() : getOtherKeywords();
    }

    @Override
    public Set<CultureKeyword> getActiveCultureKeywords() {
        return isProjectVisible() && isInheritingCulturalInformation() ? project.getCultureKeywords() : getCultureKeywords();
    }

    public Set<CultureKeyword> getActiveApprovedCultureKeywords() {
        return isProjectVisible() && isInheritingCulturalInformation() ? project.getApprovedCultureKeywords() : getApprovedCultureKeywords();
    }

    @Override
    public Set<ResourceNote> getActiveResourceNotes() {
        return isProjectVisible() && isInheritingNoteInformation() ? project.getResourceNotes() : getResourceNotes();
    }

    @Override
    public Set<ResourceAnnotation> getActiveResourceAnnotations() {
        return isProjectVisible() && isInheritingIdentifierInformation() ? project.getResourceAnnotations() : getResourceAnnotations();
    }

    public Set<CultureKeyword> getActiveUncontrolledCultureKeywords() {
        return isProjectVisible() && isInheritingCulturalInformation() ? project.getUncontrolledCultureKeywords() : getUncontrolledCultureKeywords();
    }

    @Override
    public Set<GeographicKeyword> getActiveGeographicKeywords() {
        return isProjectVisible() && isInheritingSpatialInformation() ? project.getGeographicKeywords() : getGeographicKeywords();
    }
    
    @Override
    public Set<GeographicKeyword> getActiveManagedGeographicKeywords() {
        return isProjectVisible() && isInheritingSpatialInformation() ? project.getManagedGeographicKeywords() : getManagedGeographicKeywords();
    }
    
    @Override
    public Set<LatitudeLongitudeBox> getActiveLatitudeLongitudeBoxes() {
        return isProjectVisible() && isInheritingSpatialInformation() ? project.getLatitudeLongitudeBoxes() : getLatitudeLongitudeBoxes();
    }

    @Override
    public Set<TemporalKeyword> getActiveTemporalKeywords() {
        return isProjectVisible() && isInheritingTemporalInformation() ? project.getTemporalKeywords() : getTemporalKeywords();
    }

    @Override
    public Set<CoverageDate> getActiveCoverageDates() {
        return isProjectVisible() && isInheritingTemporalInformation() ? project.getCoverageDates() : getCoverageDates();
    }

    @Transient
    @Override
    public boolean hasConfidentialFiles() {
        return !getConfidentialFiles().isEmpty();
    }

    @Transient
    @Override
    public boolean hasEmbargoedFiles() {
        for (InformationResourceFile file : getConfidentialFiles()) {
            if (file.isEmbargoed()) {
                return true;
            }
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
    public List<InformationResourceFile> getConfidentialFiles() {
        return getFilesWithRestrictions(true);
    }

    @Transient
    @XmlTransient
    public List<InformationResourceFile> getPublicFiles() {
        return getFilesWithRestrictions(false);
    }

    @Override
    public boolean isValidForController() {
        if (date == null) {
            throw new TdarValidationException("informationResource.created_date_required", Arrays.asList(getResourceType()));
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

    @Transient
    @XmlTransient
    public boolean isInheritingSomeMetadata() {
        return (inheritingCulturalInformation || inheritingInvestigationInformation || inheritingMaterialInformation || inheritingOtherInformation ||
                inheritingSiteInformation || inheritingSpatialInformation || inheritingTemporalInformation || inheritingIdentifierInformation
                || inheritingNoteInformation || inheritingIndividualAndInstitutionalCredit);
    }

    @Transient
    @Override
    public Set<Obfuscatable> obfuscate() {
        // don't claim to inherit data from Projects which are inactive
        Set<Obfuscatable> toObfuscate = super.obfuscate();
        if (!isProjectVisible()) {
            setProject(Project.NULL);
            // setting the project to null should be enough...
            setInheritingCulturalInformation(false);
            setInheritingIndividualAndInstitutionalCredit(false);
            setInheritingInvestigationInformation(false);
            setInheritingMaterialInformation(false);
            setInheritingOtherInformation(false);
            setInheritingSiteInformation(false);
            setInheritingSpatialInformation(false);
            setInheritingTemporalInformation(false);
            setInheritingIdentifierInformation(false);
            setInheritingNoteInformation(false);
        } else {
            toObfuscate.add(getProject());
        }
        toObfuscate.add(resourceProviderInstitution);
        toObfuscate.add(publisher);
        return toObfuscate;
    }

    @Override
    @XmlTransient
    public List<Creator<?>> getRelatedCreators() {
        List<Creator<?>> creators = super.getRelatedCreators();
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
    @XmlTransient
    public List<InformationResourceFile> getVisibleFilesWithThumbnails() {
        ArrayList<InformationResourceFile> visibleFiles = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile irfile : getVisibleFiles()) {
            if (logger.isTraceEnabled()) {
                logger.debug("{}", irfile.getLatestThumbnail());
            }
            if (irfile.getLatestThumbnail() != null) {
                visibleFiles.add(irfile);
            }
        }
        return visibleFiles;
    }

    // shortcut for non-deleted, visible files
    @Transient
    @XmlTransient
    public List<InformationResourceFile> getVisibleFiles() {
        ArrayList<InformationResourceFile> visibleFiles = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile irfile : getInformationResourceFiles()) {
            if (logger.isTraceEnabled()) {
                logger.trace("{} ({} {} )", irfile, irfile.isViewable(), irfile.isDeleted());
            }
            if (irfile.isViewable() && !irfile.isDeleted()) {
                visibleFiles.add(irfile);
            }
        }
        Collections.sort(visibleFiles);
        return visibleFiles;
    }

    private transient InformationResourceFileVersion primaryThumbnail = null;
    private transient Boolean hasPrimaryThumbnail = null;

    // get the latest version of the first non-deleted thumbnail (or null)
    @Transient
    @XmlTransient
    public InformationResourceFileVersion getPrimaryThumbnail() {
        if (hasPrimaryThumbnail != null) {
            return primaryThumbnail;
        }
        hasPrimaryThumbnail = Boolean.FALSE;
        
        List<InformationResourceFile> visibleFilesWithThumbnails = getVisibleFilesWithThumbnails();
        if (CollectionUtils.isNotEmpty(visibleFilesWithThumbnails)) {
            hasPrimaryThumbnail = Boolean.TRUE;
            primaryThumbnail = visibleFilesWithThumbnails.get(0).getLatestThumbnail();
            return primaryThumbnail;
        } else {
            return null;
        }
    }

    @Transient
    @XmlTransient
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
    public List<InformationResourceFile> getFilesWithProcessingErrors() {
        List<InformationResourceFile> files = new ArrayList<InformationResourceFile>();
        for (InformationResourceFile file : getInformationResourceFiles()) {
            if ((file.getStatus() == FileStatus.PROCESSING_ERROR) || (file.getStatus() == FileStatus.PROCESSING_WARNING)) {
                files.add(file);
            }
        }
        return files;
    }

    @XmlTransient
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
     * 
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
     * 
     * @param transientCopy
     */
    public void updateFromTransientResource(InformationResource transientCopy) {
        // Should we throw an exception if we are here ?
    }

    public boolean isInheritingIndividualAndInstitutionalCredit() {
        return inheritingIndividualAndInstitutionalCredit;
    }

    public void setInheritingIndividualAndInstitutionalCredit(
            boolean inheritingIndividualAndInstitutionalCredit) {
        this.inheritingIndividualAndInstitutionalCredit = inheritingIndividualAndInstitutionalCredit;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    @XmlElementWrapper(name = "fileProxies")
    @XmlElement(name = "fileProxy")
    public List<FileProxy> getFileProxies() {
        return fileProxies;
    }

    public void setFileProxies(List<FileProxy> fileProxies) {
        this.fileProxies = fileProxies;
    }

    @XmlTransient
    @JsonIgnore
    public ResourceAccessType getTransientAccessType() {
        return transientAccessType;
    }

    public void setTransientAccessType(ResourceAccessType transientAccessType) {
        this.transientAccessType = transientAccessType;
    }
}
