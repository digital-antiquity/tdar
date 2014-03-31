package org.tdar.filestore;

import java.io.File;

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


}