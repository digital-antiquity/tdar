package org.tdar.utils;

import java.io.File;

import javax.persistence.Column;

import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.bean.file.TdarFileVersion;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.filestore.FileStoreFile;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.FilestoreObjectType;

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

    public static FileStoreFile copyTdarFile(TdarFile orig) {
        FileStoreFile orig_ = new FileStoreFile();
        copyVersionToFilestoreFile(orig, orig_);
        orig_.setPersistableId(orig.getId());
        orig_.setId(orig.getId());

        return orig_;
    }

    public static TdarFileVersion copyToTdarFileVersion(FileStoreFile f) {
        TdarFileVersion v = new TdarFileVersion();
        v.setFileVersionType(f.getVersionType());
        v.setDateCreated(f.getDateCreated());
        v.setSize(f.getFileLength());
        v.setExtension(f.getExtension());
        v.setFilename(f.getFilename());
        v.setLocalPath(f.getPath());
        v.setMd5(f.getChecksum());
        v.setHeight(f.getHeight());
        v.setWidth(f.getWidth());
        return v;
    }

    public static void copyVersionToFilestoreFile(AbstractFile orig, FileStoreFileProxy vers_) {
        if (orig instanceof TdarFile) {
            TdarFile version = (TdarFile) orig;
            vers_.setFileLength(version.getSize());
            vers_.setExtension(version.getExtension());
            vers_.setChecksum(version.getMd5());
        }
        if (orig instanceof TdarFileVersion) {
            TdarFileVersion version = (TdarFileVersion) orig;
            vers_.setFileLength(version.getSize());
            vers_.setExtension(version.getExtension());
            vers_.setChecksum(version.getMd5());
        }
        vers_.setPath(orig.getLocalPath());
        vers_.setDateCreated(orig.getDateCreated());
        vers_.setFilename(orig.getFilename());
        vers_.setChecksumType("md5");
        vers_.setTransientFile(new File(orig.getLocalPath()));

    }
}
