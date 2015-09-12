package org.tdar.core.service.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;

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
    private Long informationResourceFileId;
    private boolean derivative = false;

    public DownloadFile(File file, String string, InformationResourceFileVersion irFileVersion) {
        this.setFile(file);
        this.originalFilename = string;
        this.informationResourceId = irFileVersion.getInformationResourceId();
        this.informationResourceFileId = irFileVersion.getInformationResourceFileId();
        if (irFileVersion.isDerivative()) {
            setDerivative(true);
        }
    }

    public InputStream getInputStream() throws Exception {
        return new FileInputStream(getFile());
    }

    public String getFileName() {
        return originalFilename;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) ", getFile().getName(), getFile().length());
    }

    public Long getFileLength() {
        return this.getFile().length();

    }

    public Long getInformationResourceId() {
        return informationResourceId;
    }

    public void setInformationResourceId(Long informationResourceId) {
        this.informationResourceId = informationResourceId;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Long getInformationResourceFileId() {
        return informationResourceFileId;
    }

    public void setInformationResourceFileId(Long informationResourceFileId) {
        this.informationResourceFileId = informationResourceFileId;
    }

    public boolean isDerivative() {
        return derivative;
    }

    public void setDerivative(boolean derivative) {
        this.derivative = derivative;
    }
}
