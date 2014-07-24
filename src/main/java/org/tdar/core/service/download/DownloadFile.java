package org.tdar.core.service.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a file to be downloaded
 * @author abrin
 *
 */
public class DownloadFile implements Serializable {

    private static final long serialVersionUID = -2391952058966945679L;
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private File file;
    public DownloadFile(File file) {
        this.file = file;
    }

    public InputStream getInputStream() throws Exception {
        return new FileInputStream(file);
    }

    public String getFileName() {
        return this.file.getName();
    }
    
    public Logger getLogger() {
        return logger;
    }
}
