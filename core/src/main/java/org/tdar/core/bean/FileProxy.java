package org.tdar.core.bean;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.FileAction;
import org.tdar.core.bean.resource.file.HasExtension;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.bean.resource.file.VersionType;
import org.tdar.core.configuration.TdarConfiguration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * $Id$
 * 
 * Encapsulates tdar file management actions for adding new files, replacing existing files (creating a new version), deleting files, and adding derivatives.
 * 
 * @author $Author$
 * @version $Revision$
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class FileProxy implements Serializable, Sequenceable<FileProxy>, HasExtension {

    private static final long serialVersionUID = 1390565134253286109L;

    private FileAction action = FileAction.NONE;
    private Long fileId = -1L;
    private Long originalFileVersionId = -1L;
    private File file;
    private Long size;
    private String name = "";
    private VersionType versionType = VersionType.UPLOADED;
    private FileAccessRestriction restriction = FileAccessRestriction.PUBLIC;
    private Integer sequenceNumber = 0;
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "MM/dd/yyyy")
    private Date fileCreatedDate;
    // used to help distinguish between user managed proxies and those that may have been created to work around an error
    // private boolean createdByServer = false;
    @JsonIgnore
    private InformationResourceFile informationResourceFile;
    @JsonIgnore
    private InformationResourceFileVersion informationResourceFileVersion;

    private List<FileProxy> additionalVersions = new ArrayList<FileProxy>();
    private List<FileProxy> supportingProxies = new ArrayList<FileProxy>();

    private transient final static Logger logger = LoggerFactory.getLogger(FileProxy.class);

    public FileProxy() {
    }

    public FileProxy(InformationResourceFile file) {
        this.fileId = file.getId();
        this.restriction = file.getRestriction();
        this.sequenceNumber = file.getSequenceNumber();
        this.description = file.getDescription();
        this.fileCreatedDate = file.getFileCreatedDate();
        InformationResourceFileVersion latestVersion = file.getLatestUploadedVersion();
        // this.informationResourceFile = file;
        if (latestVersion != null) {
            this.originalFileVersionId = latestVersion.getId();
            this.name = latestVersion.getFilename();
            this.size = latestVersion.getFileLength();
        } else {
            logger.warn("No version number available for file {}", file);
        }
    }

    public FileProxy(String filename, File file, VersionType versionType) {
        this(filename, file, versionType, FileAction.ADD);
    }

    public FileProxy(String filename, File file, VersionType versionType, FileAction action) {
        this.name = filename;
        this.file = file;
        this.versionType = versionType;
        this.action = action;

    }

    public FileProxy(String filename, VersionType versionType, FileAccessRestriction restriction) {
        this.name = filename;
        this.versionType = versionType;
        this.action = FileAction.ADD;
        this.restriction = restriction;
    }

    public FileProxy(TdarFile file2) {
        this.name = file2.getName();
        this.versionType = VersionType.UPLOADED;
        this.action = FileAction.ADD;
        this.restriction = FileAccessRestriction.PUBLIC;
    }

    public FileAction getAction() {
        return action;
    }

    public void setAction(FileAction action) {
        this.action = action;
    }

    /**
     * Returns the InformationResourceFile id for the given FileProxy
     * 
     * @return
     */
    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getName() {
        return name;
    }

    public void setName(String filename) {
        // strips out quotes
        // this.filename = filename.replaceAll("\"", "");
        this.name = filename;
    }

    public Long getOriginalFileVersionId() {
        return originalFileVersionId;
    }

    public void setOriginalFileVersionId(Long originalFileVersionId) {
        this.originalFileVersionId = originalFileVersionId;
    }

    public void setOriginalFileVersionId(String originalFileVersionId) {
        if (StringUtils.isEmpty(originalFileVersionId)) {
        } else {
            this.originalFileVersionId = Long.parseLong(originalFileVersionId);
        }
    }

    public Long getSize() {
        if (size == null) {
            return 0L;
        }
        return size;
    }

    public List<FileProxy> getAdditionalVersions() {
        return additionalVersions;
    }

    public VersionType getVersionType() {
        return versionType;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }

    public void addVersion(FileProxy additionalVersion) {
        additionalVersions.add(additionalVersion);
        additionalVersion.setFileId(fileId);
    }

    @Override
    public String toString() {
        return String.format("%s %s (confidential:%s size:%d fileId:%d InputStream:%s sequence:%d date:%s @desc:%h)",
                action, name, restriction, size, fileId, file, sequenceNumber, fileCreatedDate, description);
    }

    public FileAccessRestriction getRestriction() {
        return restriction;
    }

    public void setRestriction(FileAccessRestriction restriction) {
        this.restriction = restriction;
    }

    public boolean shouldProcess() {
        return action != FileAction.NONE;
    }

    @Override
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public int compareTo(FileProxy other) {
        return sequenceNumber.compareTo(other.sequenceNumber);
    }

    /**
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * @param file
     *            the file to set
     */
    public void setFile(File file) {
        this.file = file;
    }

    public static File createTempFileFromString(String fileTextInput) throws IOException {
        File tempFile = File.createTempFile("textInput", ".txt", TdarConfiguration.getInstance().getTempDirectory());
        FileUtils.writeStringToFile(tempFile, fileTextInput);
        return tempFile;
    }

    public InformationResourceFile getInformationResourceFile() {
        return informationResourceFile;
    }

    public void setInformationResourceFile(InformationResourceFile informationResourceFile) {
        this.informationResourceFile = informationResourceFile;
    }

    public InformationResourceFileVersion getInformationResourceFileVersion() {
        return informationResourceFileVersion;
    }

    public void setInformationResourceFileVersion(InformationResourceFileVersion informationResourceFileVersion) {
        this.informationResourceFileVersion = informationResourceFileVersion;
    }

    @Override
    public String getExtension() {
        if (StringUtils.isNotBlank(name) && name.contains(".")) {
            return FilenameUtils.getExtension(name);
        }
        return null;
    }

    public List<FileProxy> getSupportingProxies() {
        return supportingProxies;
    }

    public void setSupportingProxies(List<FileProxy> supportingProxies) {
        this.supportingProxies = supportingProxies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getFileCreatedDate() {
        if (fileCreatedDate == null) {
            return null;
        }
        // implicitly convert java.sql.Date to java.util.Date
        this.fileCreatedDate = new Date(fileCreatedDate.getTime());
        return fileCreatedDate;
    }

    public Integer getYear() {
        if (fileCreatedDate == null) {
            return null;
        }
        return new DateTime(fileCreatedDate.getTime()).getYear();
    }

    public Integer getMonth() {
        if (fileCreatedDate == null) {
            return null;
        }
        return new DateTime(fileCreatedDate.getTime()).getMonthOfYear();
    }

    public Integer getDay() {
        if (fileCreatedDate == null) {
            return null;
        }
        return new DateTime(fileCreatedDate.getTime()).getDayOfMonth();
    }

    public void setFileCreatedDate(Date fileCreatedDate) {
        this.fileCreatedDate = fileCreatedDate;
    }

    public boolean isDifferentFromFile(InformationResourceFile irfile) {
        if (ObjectUtils.notEqual(irfile.getRestriction(), restriction)) {
            return true;
        }
        if (ObjectUtils.notEqual(irfile.getDescription(), description)) {
            return true;
        }
        if (ObjectUtils.notEqual(irfile.getFileCreatedDate(), fileCreatedDate)) {
            return true;
        }
        return false;
    }
}
