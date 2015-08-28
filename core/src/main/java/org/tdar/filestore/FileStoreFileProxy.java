package org.tdar.filestore;

import java.io.File;
import java.util.Date;

import org.tdar.core.bean.resource.file.VersionType;

public interface FileStoreFileProxy {

    String getFilename();

    void setFilename(String filename);

    String getChecksum();

    void setChecksum(String checksum);

    String getChecksumType();

    void setChecksumType(String checksumType);

    File getTransientFile();

    void setTransientFile(File transientFile);

    Long getPersistableId();

    FilestoreObjectType getType();

    VersionType getVersionType();

    Long getInformationResourceFileId();

    Long getFileLength();

    void setMimeType(String mimeType);

    String getMimeType();

    String getPath();

    void setPath(String path);

    void setFileLength(Long length);

    void setUncompressedSizeOnDisk(Long length);

    Long getUncompressedSizeOnDisk();

    Date getDateCreated();

    void setDateCreated(Date date);

    String getExtension();

    void setExtension(String extension);

    Integer getVersion();

}