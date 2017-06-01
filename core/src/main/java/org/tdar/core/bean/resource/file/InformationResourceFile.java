package org.tdar.core.bean.resource.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.SortNatural;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.AbstractSequenced;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.filestore.WorkflowContext;
import org.tdar.utils.jaxb.converters.JaxbPersistableConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * $Id$
 * 
 * Represents a 1:1 container for any file submitted to tDAR. All children (InformationResourceFileVersions represent different manifestations of this file, the
 * uploaded (SIP), archival (if different AIP), and any derivatives (DIP)s.
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
@Entity
@Table(name = "information_resource_file",
        indexes = {
                @Index(name = "information_resource_file_ir", columnList = "information_resource_id")
        })
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.InformationResourceFile")
@Cacheable
public class InformationResourceFile extends AbstractSequenced<InformationResourceFile> implements Viewable, Indexable {

    private static final long serialVersionUID = -6957336216505367012L;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private transient WorkflowContext workflowContext;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    // cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "information_resource_id", nullable = false, updatable = false)
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
    private InformationResource informationResource;

    private transient Long transientDownloadCount;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Column(name = "file_created_date")
    @Temporal(TemporalType.DATE)
    private Date fileCreatedDate;

    @Column(name = "part_of_composite", columnDefinition = "boolean default false")
    private Boolean partOfComposite = Boolean.FALSE;

    @Column(name = "deleted", columnDefinition = "boolean default false")
    private Boolean deleted = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "general_type", length = FieldLength.FIELD_LENGTH_255)
    private FileType informationResourceFileType;

    @Column(name = "latest_version")
    private Integer latestVersion = 0;

    @Column(name = "number_of_parts")
    private Integer numberOfParts = 0;

    @Column(name = "preservation_status", length = FieldLength.FIELD_LENGTH_25)
    @Enumerated(EnumType.STRING)
    private PreservationStatus preservationStatus;

    @Column(name = "preservation_note")
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String preservationNote;

    @Column(name = "filename", length = FieldLength.FIELD_LENGTH_255)
    private String filename;

    @OneToMany(mappedBy = "informationResourceFile", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH })
    @SortNatural
    @Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, region = "org.tdar.core.bean.resource.InformationResourceFile.informationResourceFileVersions")
    private SortedSet<InformationResourceFileVersion> informationResourceFileVersions = new TreeSet<InformationResourceFileVersion>();

    @Enumerated(EnumType.STRING)
    @Column(length = FieldLength.FIELD_LENGTH_50)
    private FileAccessRestriction restriction = FileAccessRestriction.PUBLIC;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "error_message")
    private String errorMessage;

    // a date in standard form that a resource will become public if availableToPublic was set to false.
    // This date may be extended by the publisher but will not extend past the publisher's death unless
    // special arrangements are made.
    @Column(name = "date_made_public")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateMadePublic;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private FileStatus status;

    @XmlElement(name = "informationResourceRef")
    @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
    public InformationResource getInformationResource() {
        return informationResource;
    }

    private transient boolean viewable = false;

    public InformationResourceFile() {
    }

    public InformationResourceFile(FileStatus status, Collection<InformationResourceFileVersion> versions) {
        setStatus(status);
        if (CollectionUtils.isNotEmpty(versions)) {
            getInformationResourceFileVersions().addAll(versions);
        }
    }

    public void setInformationResource(InformationResource informationResource) {
        this.informationResource = informationResource;
    }

    public FileType getInformationResourceFileType() {
        return informationResourceFileType;
    }

    public void setInformationResourceFileType(FileType informationResourceFileType) {
        this.informationResourceFileType = informationResourceFileType;
    }

    @XmlAttribute(name = "latestVersion")
    public Integer getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(Integer latestVersion) {
        this.latestVersion = latestVersion;
    }

    @XmlElementWrapper(name = "informationResourceFileVersions")
    @XmlElement(name = "informationResourceFileVersion")
    public SortedSet<InformationResourceFileVersion> getInformationResourceFileVersions() {
        return informationResourceFileVersions;
    }

    public void setInformationResourceFileVersions(
            SortedSet<InformationResourceFileVersion> informationResourceFileVersions) {
        this.informationResourceFileVersions = informationResourceFileVersions;
    }

    @Transient
    @XmlTransient
    public InformationResourceFileVersion getTranslatedFile() {
        return getVersion(getLatestVersion(), VersionType.TRANSLATED);
    }

    @Transient
    @XmlTransient
    public InformationResourceFileVersion getIndexableVersion() {
        return getVersion(getLatestVersion(), VersionType.INDEXABLE_TEXT);
    }

    public void addFileVersion(InformationResourceFileVersion version) {
        getInformationResourceFileVersions().add(version);
    }

    public void incrementVersionNumber() {
        if (latestVersion == null) {
            latestVersion = 0;
        }
        latestVersion++;
    }

    @Transient
    @XmlTransient
    public Collection<InformationResourceFileVersion> getLatestVersions() {
        return getVersions(getLatestVersion());
    }

    public Collection<InformationResourceFileVersion> getVersions(int version) {
        ArrayList<InformationResourceFileVersion> files = new ArrayList<InformationResourceFileVersion>();
        for (InformationResourceFileVersion irfv : getInformationResourceFileVersions()) {
            if (irfv.getVersion().equals(version)) {
                files.add(irfv);
            }
        }
        return files;
    }

    @Transient
    public InformationResourceFileVersion getLatestTranslatedVersion() {
        for (InformationResourceFileVersion version : getInformationResourceFileVersions()) {
            if (version.getVersion().equals(getLatestVersion()) && version.isTranslated()) {
                logger.trace("version: {}", version);
                return version;
            }
        }
        return null;
    }

    @Transient
    @XmlTransient
    public InformationResourceFileVersion getLatestPDF() {
        for (InformationResourceFileVersion version : getInformationResourceFileVersions()) {
            if (version.getVersion().equals(getLatestVersion()) && version.getExtension().equalsIgnoreCase("pdf")) {
                ;
            }
            return version;
        }
        return null;
    }

    @Transient
    @XmlTransient
    public InformationResourceFileVersion getLatestThumbnail() {
        for (InformationResourceFileVersion version : getInformationResourceFileVersions()) {
            if (version.getVersion().equals(latestVersion) && version.isThumbnail()) {
                return version;
            }
        }
        return null;
    }

    @Transient
    @XmlTransient
    public InformationResourceFileVersion getLatestArchival() {
        logger.debug("looking for latest archival version in {} with version number {}", getInformationResourceFileVersions(), latestVersion);
        for (InformationResourceFileVersion version : getInformationResourceFileVersions()) {
            if (version.getVersion().equals(latestVersion) && version.isArchival()) {
                return version;
            }
        }
        return null;
    }

    @JsonIgnore
    public InformationResourceFileVersion getLatestUploadedVersion() {
        return getUploadedVersion(getLatestVersion());
    }

    public InformationResourceFileVersion getUploadedVersion(Integer versionNumber) {
        return getVersion(versionNumber, VersionType.UPLOADED_ARCHIVAL, VersionType.UPLOADED_TEXT, VersionType.UPLOADED);
    }

    /* Use for Ontology (or perhaps coding sheet); will need to verify that this does not break things when we have true archival version */
    @JsonIgnore
    public InformationResourceFileVersion getLatestUploadedOrArchivalVersion() {
        return getVersion(getLatestVersion(), VersionType.UPLOADED, VersionType.UPLOADED_TEXT, VersionType.UPLOADED_ARCHIVAL, VersionType.ARCHIVAL);
    }

    // FIXME: improve efficiency
    public InformationResourceFileVersion getVersion(Integer versionNumber_, VersionType... types) {
        Integer versionNumber = versionNumber_;
        int currentVersionNumber = -1;
        Set<InformationResourceFileVersion> versions = getInformationResourceFileVersions();
        if ((versionNumber == null) || (versionNumber == -1)) {
            // FIXME: why not just set versionNumber = latestVersion?
            for (InformationResourceFileVersion file : versions) {
                if (file.getVersion().intValue() > currentVersionNumber) {
                    currentVersionNumber = file.getVersion().intValue();
                }
            }
            versionNumber = Integer.valueOf(currentVersionNumber);
            logger.debug("assuming current version is: {}", currentVersionNumber);
        }

        for (InformationResourceFileVersion file : versions) {
            if (file.getVersion().equals(versionNumber)) {
                for (VersionType type : types) {
                    if (file.getFileVersionType() == type) {
                        return file;
                    }
                }
            }
        }
        logger.trace("getVersion({}, {}) couldn't find an appropriate file version", versionNumber, Arrays.asList(types));
        return null;
    }

    /**
     * @param toDelete
     */
    public void removeAll(List<InformationResourceFileVersion> toDelete) {
        this.informationResourceFileVersions.removeAll(toDelete);
    }

    /**
     * Get the latest version of the specified file version type. If the specified type is WEB_LARGE, return the best approximation if no WEB_LARGE version
     * is available.
     * 
     * @param type
     * @return latest version of the specified type, or null if no version of the specified type is available
     */
    public InformationResourceFileVersion getCurrentVersion(VersionType type) {
        InformationResourceFileVersion currentVersion = null;
        for (InformationResourceFileVersion latestVersion : getLatestVersions()) {
            if (type == latestVersion.getFileVersionType()) {
                currentVersion = latestVersion;
                // if the type is exact, break and return out
                break;
            }
        }
        return currentVersion;
    }

    public InformationResourceFileVersion getZoomableVersion() {
        InformationResourceFileVersion currentVersion = null;
        for (InformationResourceFileVersion latestVersion : getLatestVersions()) {
            logger.trace("latest version {}", latestVersion);
            switch (latestVersion.getFileVersionType()) {
            // FIXME: if no WEB_MEDIUM is available we probably want to return a WEB_SMALL if possible.
                case WEB_LARGE:
                    currentVersion = latestVersion;
                    break;
                case WEB_MEDIUM:
                    if (currentVersion == null) {
                        currentVersion = latestVersion;
                    }
                    break;
                case WEB_SMALL:
                    break;
                default:
                    break;
            }
        }
        return currentVersion;
    }

    @Transient
    @XmlTransient
    public boolean isConfidential() {
        return restriction == FileAccessRestriction.CONFIDENTIAL;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    @Transient
    @XmlTransient
    public boolean isProcessed() {
        return status == FileStatus.PROCESSED;
    }

    public boolean isDeleted() {
        if (deleted == null) {
            return false;
        }
        return deleted;
    }

    @Transient
    @XmlTransient
    public boolean isErrored() {
        return status == FileStatus.PROCESSING_ERROR;
    }

    @Transient
    @XmlTransient
    public boolean isPublic() {
        // this had a "DELETED" check; but it really needs to just be "is public or not"
        if (restriction == FileAccessRestriction.PUBLIC) {
            return true;
        }
        return false;
    }

    public void clearStatus() {
        setStatus(null);
    }

    @Override
    public String toString() {
        return String.format("(%d, %s) v#:%s: %s (%s versions)", getId(), status, getLatestVersion(), restriction,
                CollectionUtils.size(informationResourceFileVersions));
    }

    public void clearQueuedStatus() {
        if (status == FileStatus.QUEUED) {
            status = null;
        }
    }

    public boolean isColumnarDataFileType() {
        return getInformationResourceFileType() == FileType.COLUMNAR_DATA;
    }

    @Override
    public boolean isViewable() {
        return viewable;
    }

    @Override
    public void setViewable(boolean accessible) {
        this.viewable = accessible;
    }

    public Long getTransientDownloadCount() {
        return transientDownloadCount;
    }

    public void setTransientDownloadCount(Long transientDownloadCount) {
        this.transientDownloadCount = transientDownloadCount;
    }

    public Integer getNumberOfParts() {
        return numberOfParts;
    }

    public void setNumberOfParts(Integer numberOfParts) {
        this.numberOfParts = numberOfParts;
    }

    @Transient
    @XmlTransient
    public WorkflowContext getWorkflowContext() {
        return workflowContext;
    }

    public void setWorkflowContext(WorkflowContext workflowContext) {
        this.workflowContext = workflowContext;
    }

    public Date getDateMadePublic() {
        return dateMadePublic;
    }

    public void setDateMadePublic(Date dateMadePublic) {
        this.dateMadePublic = dateMadePublic;
    }

    public FileAccessRestriction getRestriction() {
        return restriction;
    }

    public void setRestriction(FileAccessRestriction restriction) {
        this.restriction = restriction;
    }

    public boolean isEmbargoed() {
        return this.restriction != null && this.restriction.isEmbargoed();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isPartOfComposite() {
        if (partOfComposite == null) {
            return false;
        }
        return partOfComposite;
    }

    public void setPartOfComposite(boolean partOfComposite) {
        this.partOfComposite = partOfComposite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getFileCreatedDate() {
        return fileCreatedDate;
    }

    public void setFileCreatedDate(Date fileCreatedDate) {
        this.fileCreatedDate = fileCreatedDate;
    }

    public boolean isHasTranslatedVersion() {
        try {
            if ((getLatestTranslatedVersion() != null) && getInformationResource().getResourceType().isDataTableSupported()) {
                return true;
            }
        } catch (Exception e) {
            logger.error("cannot tell if file has translated version {}", e);
        }
        return false;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getPreservationNote() {
        return preservationNote;
    }

    public void setPreservationNote(String preservationNote) {
        this.preservationNote = preservationNote;
    }

    public void setPreservationStatus(PreservationStatus preservationStatus) {
        this.preservationStatus = preservationStatus;
    }
}
