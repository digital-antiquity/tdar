package org.tdar.utils;

import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.filestore.FileStoreFile;

public class FileStoreFileUtils {

    public static void copyFileStoreFileIntoVersion(FileStoreFile orig_, InformationResourceFileVersion orig) {
        orig.setInformationResourceFileId(orig_.getInformationResourceFileId());
        orig.setFileLength(orig_.getFileLength());
        orig.setMimeType(orig_.getMimeType());
        orig.setPath(orig_.getPath());
        orig.setUncompressedSizeOnDisk(orig_.getUncompressedSizeOnDisk());
        orig.setDateCreated(orig_.getDateCreated());
        orig.setExtension(orig_.getExtension());
        orig.setFilename(orig_.getFilename());
        orig.setChecksum(orig_.getChecksum());
        orig.setChecksumType(orig_.getChecksumType());
        orig.setTransientFile(orig_.getTransientFile());
        orig.setFileVersionType(orig_.getVersionType());
        orig.setVersion(orig_.getVersion());
        orig.setId(orig_.getId());
        orig.setPrimaryFile(orig_.getPrimary());
        orig.setHeight(orig_.getHeight());
        orig.setWidth(orig_.getWidth());
    }

    public static FileStoreFile copyVersionToFilestoreFile(InformationResourceFileVersion orig) {
        FileStoreFile orig_ = new FileStoreFile();
        orig_.setInformationResourceFileId(orig.getInformationResourceFileId());
        orig_.setFileLength(orig.getFileLength());
        orig_.setMimeType(orig.getMimeType());
        orig_.setPath(orig.getPath());
        orig_.setUncompressedSizeOnDisk(orig.getUncompressedSizeOnDisk());
        orig_.setDateCreated(orig.getDateCreated());
        orig_.setExtension(orig.getExtension());
        orig_.setFilename(orig.getFilename());
        orig_.setChecksum(orig.getChecksum());
        orig_.setChecksumType(orig.getChecksumType());
        orig_.setTransientFile(orig.getTransientFile());
        orig_.setPersistableId(orig.getPersistableId());
        orig_.setType(orig.getType());
        orig_.setVersionType(orig.getVersionType());
        orig_.setVersion(orig.getVersion());
        orig_.setId(orig.getId());
        orig_.setPrimary(orig.isPrimaryFile());
        orig_.setHeight(orig.getHeight());
        orig_.setWidth(orig.getWidth());
        return orig_;
    }
}
