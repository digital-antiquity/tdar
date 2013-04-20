package org.tdar.core.bean.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Viewable;
import org.tdar.core.configuration.TdarConfiguration;

@Entity
// making the assumption formally that there can only be one version of any type
// of a given file at any moment.
// That is, only one archival version, translated version, etc...
@Table(name = "information_resource_file_version", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "information_resource_file_id", "file_version", "internal_type" })
})
public class InformationResourceFileVersion extends Persistable.Base implements Comparable<InformationResourceFileVersion>, Viewable {

    private static final long serialVersionUID = 3768354809654162949L;

    @ManyToOne()
    // optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "information_resource_file_id")
    private InformationResourceFile informationResourceFile;

    private String filename;

    @Column(name = "file_version")
    private Integer version;

    @Column(name = "mime_type")
    private String mimeType;

    private String format;

    private Boolean primaryFile = Boolean.FALSE;

    private String extension;

    private String premisId;

    @Column(name = "filestore_id")
    private String filestoreId;

    private String checksum;

    @Column(name = "checksum_type")
    private String checksumType;

    @Column(nullable = false, name = "date_created")
    private Date dateCreated;

    @Column(name = "file_type")
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "internal_type")
    private VersionType fileVersionType;

    private Integer width;

    private Integer height;

    @Column(name = "total_time")
    private Long totalTime;

    @Column(name = "size")
    private Long fileLength;

    @Column(name = "effective_size")
    private Long uncompressedSizeOnDisk;

    private String path;

    @Transient
    private Long informationResourceId;
    @Transient
    private Long informationResourceFileId;

    @Transient
    private File file;

    private transient boolean viewable = false;

    private transient List<InformationResourceFileVersion> supportingFiles = new ArrayList<InformationResourceFileVersion>();

    /*
     * This constructor exists only for Hibernate ... another constructor should
     * be used outside of Hibernate
     */
    @Deprecated
    public InformationResourceFileVersion() {
    }

    public InformationResourceFileVersion(VersionType type, String filename, Integer version, Long infoResId, Long irFileId) {
        setFileVersionType(type);
        setFilename(filename);
        setVersion(version);
        setExtension(FilenameUtils.getExtension(filename));
        setInformationResourceId(infoResId);
        setInformationResourceFileId(irFileId);
    }

    public InformationResourceFileVersion(VersionType type, String filename, InformationResourceFile irFile) {
        setFileVersionType(type);
        setFilename(filename);
        setExtension(FilenameUtils.getExtension(filename));
        setVersion(irFile.getLatestVersion());
        setInformationResourceFile(irFile);
        setInformationResourceFileId(irFile.getId());
        setInformationResourceId(irFile.getInformationResource().getId());
        setDateCreated(new Date());
    }

    @XmlTransient
    public InformationResourceFile getInformationResourceFile() {
        return informationResourceFile;
    }

    public void setInformationResourceFile(InformationResourceFile informationResourceFile) {
        this.informationResourceFile = informationResourceFile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @XmlAttribute(name = "version")
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getPremisId() {
        return premisId;
    }

    public void setPremisId(String premisId) {
        this.premisId = premisId;
    }

    public String getFilestoreId() {
        return filestoreId;
    }

    public void setFilestoreId(String filestoreId) {
        this.filestoreId = filestoreId;
    }

    public String getChecksum() {
        return checksum;
    }

    /*
     * Only set the checksum if it is not set
     */
    public void setChecksum(String checksum) {
        if (StringUtils.isEmpty(this.checksum)) {
            this.checksum = checksum;
        } else {
            logger.info("not setting checksum to :" + checksum + " b/c set to :" + this.checksum);
        }
    }

    public void overrideChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksumType() {
        return checksumType;
    }

    public void setChecksumType(String checksumType) {
        this.checksumType = checksumType;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public VersionType getFileVersionType() {
        return fileVersionType;
    }

    public void setFileVersionType(VersionType informationResourceFileVersionType) {
        this.fileVersionType = informationResourceFileVersionType;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public void setFileLength(Long size) {
        this.fileLength = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Transient
    // Ultimately, this may need to be converted into a URI, or something that can be converted directly
    // into a reader due to the indexing requirment.
    // FIXME: consider injecting this as a transient variable when loaded
    // instead of doing a lookup using the TdarConfiguration singleton's Filestore. Otherwise
    // we will have difficulty converting TdarConfiguration + Filestore into spring managed beans
    public File getFile() {
        if (file != null) {
            return file;
        }

        if (filestoreId == null && path == null)
            return null;
        try {
            file = TdarConfiguration.getInstance().getFilestore().retrieveFile(this);
        } catch (FileNotFoundException e) {
            logger.warn("No file found in store with ID: " + getFilename() + " and path:" + path + " (" + getId() + ")");
        }
        return file;
    }

    @Transient
    public boolean isTranslated() {
        return (getFileVersionType() == VersionType.TRANSLATED);
    }

    @Transient
    public boolean isUploaded() {
        return (getFileVersionType() == VersionType.UPLOADED || getFileVersionType() == VersionType.UPLOADED_ARCHIVAL);
    }

    @Transient
    public boolean isArchival() {
        // FIXME: change back later and update test
        return (getFileVersionType() == VersionType.ARCHIVAL || getFileVersionType() == VersionType.UPLOADED_ARCHIVAL);
    }

    @Transient
    public boolean isThumbnail() {
        return (getFileVersionType() == VersionType.WEB_SMALL);
    }

    /**
     * @return
     */
    public boolean isDerivative() {
        return getFileVersionType().isDerivative();
        // switch (getFileVersionType()) {
        // case INDEXABLE_TEXT:
        // case WEB_SMALL:
        // case WEB_MEDIUM:
        // case WEB_LARGE:
        // case METADATA:
        // case TRANSLATED:
        // return true;
        // default:
        // return false;
        // }
        // // return (getFileVersionType() == VersionType.INDEXABLE_TEXT
        // // || getFileVersionType() == VersionType.WEB_SMALL
        // // || getFileVersionType() == VersionType.WEB_MEDIUM || getFileVersionType() == VersionType.WEB_LARGE);
    }

    /**
     * @return
     */
    public boolean isIndexable() {
        return (getFileVersionType() == VersionType.INDEXABLE_TEXT);
    }

    /**
     * @return
     */
    @XmlTransient
    public String getIndexableContent() {
        String toReturn = "";
        if (!isIndexable()) {
            return "";
        }
        try {
            toReturn = FileUtils.readFileToString(getFile());
        } catch (FileNotFoundException e) {
            logger.error("Information resource File: " + getFilename() + " (" + getId() + ") does not exist");
        } catch (IOException io) {
            logger.error("an io exception occurred when trying to read file:" + getFilename(), io);
        }
        return toReturn;
    }

    /**
     * @param informationResourceFileId
     *            the informationResourceFileId to set
     */
    public void setInformationResourceFileId(Long informationResourceFileId) {
        this.informationResourceFileId = informationResourceFileId;
    }

    /**
     * Convenience Method to get at parent information. This will allow the bean
     * to be passed information in two different ways but the filestore to
     * access the raw information from the same core method.
     * 
     * @return the informationResourceFileId
     */
    @XmlAttribute(name = "informationResourceFileId")
    public Long getInformationResourceFileId() {
        if (informationResourceFile != null)
            return informationResourceFile.getId();
        return informationResourceFileId;
    }

    /**
     * @param informationResourceId
     *            the informationResourceId to set
     */
    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

    /**
     * Convenience Method to get at parent information. This will allow the bean
     * to be passed information in two different ways but the filestore to
     * access the raw information from the same core method.
     * 
     * @return the informationResourceId
     */
    @XmlAttribute(name = "informationResourceId")
    @Transient
    public Long getInformationResourceId() {
        if (informationResourceFile != null && informationResourceFile.getInformationResource() != null)
            return informationResourceFile.getInformationResource().getId();
        return informationResourceId;
    }

    public String toString() {
        return String.format("%s (%s, #%d | %d)", filename, fileVersionType, version, getInformationResourceFileId());
    }

    @Override
    public int compareTo(InformationResourceFileVersion other) {
        int comparison = -1;
        logger.trace("comparing: " + other + " to " + this);
        if (equals(other)) { // exactly equal
            comparison = 0;
        } else {
            comparison = getVersion().compareTo(other.getVersion());
            if (comparison == 0) {
                comparison = getFilename().compareTo(other.getFilename());
                if (comparison == 0) {
                    comparison = getFileVersionType().compareTo(other.getFileVersionType());
                }
            }
        }
        logger.trace("result: " + comparison);
        return comparison;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.bean.Persistable.Base#getEqualityFields()
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<?> getEqualityFields() {
        // ab probably okay as it includes Id
        return Arrays.asList(getInformationResourceFileId(), version, fileVersionType, getId());
    }

    public boolean hasValidFile() {
        return getFile() != null && getFile().exists();
    }

    public boolean isViewable() {
        return viewable;
    }

    public void setViewable(boolean viewable) {
        this.viewable = viewable;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }

    public Long getUncompressedSizeOnDisk() {
        return uncompressedSizeOnDisk;
    }

    public void setUncompressedSizeOnDisk(Long actualSizeOnDisk) {
        this.uncompressedSizeOnDisk = actualSizeOnDisk;
    }

    public List<InformationResourceFileVersion> getSupportingFiles() {
        return supportingFiles;
    }

    public void setSupportingFiles(List<InformationResourceFileVersion> supportingFiles) {
        this.supportingFiles = supportingFiles;
    }

    public boolean isPrimaryFile() {
        return primaryFile;
    }

    public void setPrimaryFile(boolean primaryFile) {
        this.primaryFile = primaryFile;
    }

}
