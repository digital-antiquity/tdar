package org.tdar.core.service.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a file to be downloaded
 * 
 * @author abrin
 *
 */
public class DownloadFile implements Serializable {

    private static final long serialVersionUID = -2391952058966945679L;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private File file;
    private String originalFilename;
    private Long informationResourceId;

    public DownloadFile(File file, String string, Long id) {
        this.file = file;
        this.originalFilename = string;
        this.informationResourceId = id;
    }

    public InputStream getInputStream() throws Exception {
        return new FileInputStream(file);
    }

    public String getFileName() {
        return originalFilename;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) ", file.getName(), file.length());
    }

    public Long getFileLength() {
        return this.file.length();

    }

    public Long getInformationResourceId() {
        return informationResourceId;
    }

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }
}
