package org.tdar.struts.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;

/**
 * $Id$
 * 
 * Encapsulates tdar file management actions for adding new files, replacing existing files (creating a new version), deleting files, and adding derivatives.
 * 
 * @author $Author$
 * @version $Revision$
 */
public class FileProxy implements Serializable, Sequenceable<FileProxy> {

    private static final long serialVersionUID = 1390565134253286109L;

    private FileAction action = FileAction.NONE;
    private Long fileId = -1L;
    private Long originalFileVersionId = -1L;
    private InputStream inputStream;
    private Long size;
    private String filename = "";
    private VersionType versionType = VersionType.UPLOADED;
    private boolean confidential;
    private Integer sequenceNumber = 0;

    private List<FileProxy> additionalVersions = new ArrayList<FileProxy>();

    private transient final static Logger LOGGER = LoggerFactory.getLogger(FileProxy.class);

    public FileProxy() {
    }

    public FileProxy(InformationResourceFile file) {
        this.fileId = file.getId();
        this.confidential = file.isConfidential();
        this.sequenceNumber = file.getSequenceNumber();
        InformationResourceFileVersion latestVersion = file.getLatestUploadedVersion();
        if (latestVersion != null) {
            this.originalFileVersionId = latestVersion.getId();
            this.filename = latestVersion.getFilename();
            this.size = latestVersion.getSize();
        } else {
            LOGGER.warn("No version number available for file {}", file);
        }
    }

    public FileProxy(String filename, InputStream inputStream, VersionType versionType) {
        this(filename, inputStream, versionType, FileAction.ADD);
    }

    public FileProxy(String filename, InputStream inputStream, VersionType versionType, FileAction action) {
        this.filename = filename;
        this.inputStream = inputStream;
        this.versionType = versionType;
        this.action = action;

    }

    public FileProxy(String filename, VersionType versionType, boolean confidential) {
        this.filename = filename;
        this.versionType = versionType;
        this.action = FileAction.ADD;
        this.confidential = confidential;
    }

    /**
     * Constructs a FileProxy representing a new version of the given InformationResourceFileVersion, useful
     * for reprocessing an existing file.
     * 
     * @param version
     */
    public FileProxy(InformationResourceFileVersion version) throws IOException {
    	this(version.getFilename(), new FileInputStream(version.getFile()), VersionType.UPLOADED, FileAction.ADD);
    	setFileId(version.getInformationResourceFileId());
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        // strips out quotes
//        this.filename = filename.replaceAll("\"", "");
        this.filename = filename;
    }

    public Long getOriginalFileVersionId() {
        return originalFileVersionId;
    }

    public void setOriginalFileVersionId(Long originalFileVersionId) {
        this.originalFileVersionId = originalFileVersionId;
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

    /**
     * Returns the InputStream representation of this FileProxy. Currently, this could be from the uploaded file or
     * generated from text input in the case of CodingSheetS and OntologyS.
     * 
     * @return an InputStream representing the file contents of this FileProxy.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
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

    public String toString() {
        return String.format("%s %s (confidential: %s, size: %d, fileId: %d, InputStream: %s, sequence number: %d)", action, filename, confidential, size, fileId, inputStream, sequenceNumber);
    }

    public boolean isConfidential() {
        return confidential;
    }

    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    public boolean shouldProcess() {
        return action != FileAction.NONE;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public int compareTo(FileProxy other) {
        return sequenceNumber.compareTo(other.sequenceNumber);
    }

}
