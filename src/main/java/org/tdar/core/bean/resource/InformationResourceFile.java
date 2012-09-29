package org.tdar.core.bean.resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;

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
@Table(name = "information_resource_file")
public class InformationResourceFile extends Persistable.Sequence<InformationResourceFile> {

    private static final long serialVersionUID = -6957336216505367012L;
    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    public enum FileAction {
        NONE, ADD, REPLACE, DELETE, MODIFY_METADATA, ADD_DERIVATIVE;
    }

    public enum FileType {
        IMAGE, DOCUMENT, COLUMNAR_DATA, FILE_ARCHIVE, OTHER
    }

    public enum FileStatus {
        // whether or not the file is in the middle of a queued process
        QUEUED,
        // whether or not this InformationResourceFile has been converted into postgres
        PROCESSED,
        DELETED;
    }

    @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinColumn(name = "information_resource_id")
    private InformationResource informationResource;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "general_type")
    private FileType informationResourceFileType;

    @Column(name = "latest_version")
    private Integer latestVersion = 0;

    @OneToMany(mappedBy = "informationResourceFile", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @Sort(type = SortType.NATURAL)
    private SortedSet<InformationResourceFileVersion> informationResourceFileVersions = new TreeSet<InformationResourceFileVersion>();

    private Boolean confidential = Boolean.FALSE;
    //
    // private boolean processed;
    // private boolean queued;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @XmlTransient
    public InformationResource getInformationResource() {
        return informationResource;
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

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    @Transient
    public InformationResourceFileVersion getTranslatedFile() {
        return getVersion(getLatestVersion(), VersionType.TRANSLATED);
    }

    @Transient
    public InformationResourceFileVersion getIndexableVersion() {
        return getVersion(getLatestVersion(), VersionType.INDEXABLE_TEXT);
    }

    @Transient
    public File getFile(VersionType type, int version) {
        for (InformationResourceFileVersion irfv : getVersions(version)) {
            if (irfv.getFileVersionType() == type)
                return irfv.getFile();
        }
        return null;
    }

    public void addFileVersion(InformationResourceFileVersion version) {
        getInformationResourceFileVersions().add(version);
    }

    public void incrementDownloadCount() {
        if (downloadCount == null) {
            downloadCount = 0;
        }
        downloadCount++;
    }

    public void incrementVersionNumber() {
        if (latestVersion == null) {
            latestVersion = 0;
        }
        latestVersion++;
    }

    @Transient
    public Collection<InformationResourceFileVersion> getLatestVersions() {
        return getVersions(getLatestVersion());
    }

    @Transient
    public Collection<InformationResourceFileVersion> getVersions(int version) {
        ArrayList<InformationResourceFileVersion> files = new ArrayList<InformationResourceFileVersion>();
        for (InformationResourceFileVersion irfv : getInformationResourceFileVersions()) {
            if (irfv.getVersion().equals(version))
                files.add(irfv);
        }
        return files;
    }

    @Transient
    public InformationResourceFileVersion getLatestTranslated() {
        for (InformationResourceFileVersion version : getInformationResourceFileVersions()) {
            if (version.getVersion().equals(getLatestVersion()) && version.isTranslated())
                return version;
        }
        return null;
    }

    @Transient
    public InformationResourceFileVersion getLatestPDF() {
        for (InformationResourceFileVersion version : getInformationResourceFileVersions()) {
            if (version.getVersion().equals(getLatestVersion()) && version.getExtension().equalsIgnoreCase("pdf"))
                ;
            return version;
        }
        return null;
    }

    @Transient
    public InformationResourceFileVersion getLatestThumbnail() {
        for (InformationResourceFileVersion version : getInformationResourceFileVersions()) {
            if (version.getVersion().equals(latestVersion) && version.isThumbnail())
                return version;
        }
        return null;
    }

    @Transient
    public InformationResourceFileVersion getLatestArchival() {
        logger.debug("looking for latest archival version in {} with version number {}", getInformationResourceFileVersions(), latestVersion);
        for (InformationResourceFileVersion version : getInformationResourceFileVersions()) {
            if (version.getVersion().equals(latestVersion) && version.isArchival())
                return version;
        }
        return null;
    }

    public InformationResourceFileVersion getLatestUploadedVersion() {
        return getUploadedVersion(getLatestVersion());
    }

    public InformationResourceFileVersion getUploadedVersion(Integer versionNumber) {
        return getVersion(versionNumber, VersionType.UPLOADED_ARCHIVAL, VersionType.UPLOADED);
    }

    // FIXME: improve efficiency
    public InformationResourceFileVersion getVersion(Integer versionNumber, VersionType... types) {
        int currentVersionNumber = -1;
        Set<InformationResourceFileVersion> versions = getInformationResourceFileVersions();
        if (versionNumber == null || versionNumber == -1) {
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
        logger.debug("getVersion({}, {}) couldn't find an appropriate file version", versionNumber, Arrays.asList(types));
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
            }
        }
        return currentVersion;
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    @Transient
    public boolean isProcessed() {
        return status == FileStatus.PROCESSED;
    }

    @Transient
    public boolean isDeleted() {
        return status == FileStatus.DELETED;
    }

    @Transient
    public boolean isPublic() {
        if (status != FileStatus.DELETED && !confidential) {
            return true;
        }
        return false;
    }

    @Transient
    public void clearStatus() {
        setStatus(null);
    }

    public void delete() {
        setStatus(FileStatus.DELETED);
    }

    public String toString() {
        return String.format("(%d, %s): %s", getId(), status, getInformationResourceFileVersions());
    }

    public void clearQueuedStatus() {
        if (status == FileStatus.QUEUED) {
            status = null;
        }
    }

    public boolean isColumnarDataFileType() {
        return getInformationResourceFileType() == FileType.COLUMNAR_DATA;
    }

}
