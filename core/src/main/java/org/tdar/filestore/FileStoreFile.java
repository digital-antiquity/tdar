package org.tdar.filestore;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import org.tdar.core.bean.resource.file.VersionType;

public class FileStoreFile implements Serializable, FileStoreFileProxy {

    private static final long serialVersionUID = -3168636719632062521L;

    private String filename;
    private String checksum;
    private String checksumType;
    private File transientFile;
    private Long persistableId;
    private Long informationResourceFileId;
    private Long fileLength;
    private String mimeType;
    private String path;
    private Long uncompressedSizeOnDisk;
    private Date dateCreated;
    private String extension;
    private Integer version;

    @Override
    public Long getInformationResourceFileId() {
        return informationResourceFileId;
    }

    public void setInformationResourceFileId(Long informationResourceFileId) {
        this.informationResourceFileId = informationResourceFileId;
    }

    @Override
    public Long getFileLength() {
        return fileLength;
    }

    @Override
    public void setFileLength(Long fileLength) {
        this.fileLength = fileLength;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public Long getUncompressedSizeOnDisk() {
        return uncompressedSizeOnDisk;
    }

    @Override
    public void setUncompressedSizeOnDisk(Long uncompressedSizeOnDisk) {
        this.uncompressedSizeOnDisk = uncompressedSizeOnDisk;
    }

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    @Override
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    @Override
    public void setExtension(String extension) {
        this.extension = extension;
    }

    private FilestoreObjectType type;
    private VersionType versionType;

    public FileStoreFile() {

    }

    public FileStoreFile(FilestoreObjectType type, VersionType versionType, Long id, String filename) {
        this.persistableId = id;
        this.filename = filename;
        this.type = type;
        this.versionType = versionType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#getFilename()
     */
    @Override
    public String getFilename() {
        return filename;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#setFilename(java.lang.String)
     */
    @Override
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#getChecksum()
     */
    @Override
    public String getChecksum() {
        return checksum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#setChecksum(java.lang.String)
     */
    @Override
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#getChecksumType()
     */
    @Override
    public String getChecksumType() {
        return checksumType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#setChecksumType(java.lang.String)
     */
    @Override
    public void setChecksumType(String checksumType) {
        this.checksumType = checksumType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#getTransientFile()
     */
    @Override
    public File getTransientFile() {
        return transientFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#setTransientFile(java.io.File)
     */
    @Override
    public void setTransientFile(File transientFile) {
        this.transientFile = transientFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#getPersistableId()
     */
    @Override
    public Long getPersistableId() {
        return persistableId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.filestore.FileStoreFileProxy#setPersistableId(java.lang.Long)
     */
    public void setPersistableId(Long persistableId) {
        this.persistableId = persistableId;
    }

    @Override
    public FilestoreObjectType getType() {
        return type;
    }

    public void setType(FilestoreObjectType type) {
        this.type = type;
    }

    @Override
    public VersionType getVersionType() {
        return versionType;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}
