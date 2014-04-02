package org.tdar.filestore;

import java.io.File;
import java.io.Serializable;

public class FileStoreFile implements Serializable, FileStoreFileProxy {

    private static final long serialVersionUID = -3168636719632062521L;

    private String filename;
    private String checksum;
    private String checksumType;
    private File transientFile;
    private Long persistableId;
    private DirectoryType type;

    public FileStoreFile() {

    }

    public enum DirectoryType {
        IMAGE,
        SUPPORT;
    }

    public FileStoreFile(DirectoryType type, Long id, String filename) {
        this.persistableId = id;
        this.filename = filename;
        this.type = type;
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

    public DirectoryType getType() {
        return type;
    }

    public void setType(DirectoryType type) {
        this.type = type;
    }

}
