package org.tdar.balk.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.utils.dropbox.DropboxConfig;
import org.tdar.utils.dropbox.DropboxConstants;

public enum Phases {
    TO_PDFA,
    DONE_PDFA,
    UPLOAD_TDAR;
    static DropboxConfig config = DropboxConfig.getInstance();

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    private final static transient Logger staticlogger = LoggerFactory.getLogger(Phases.class);

    public String getPath() {
        switch (this) {
            case TO_PDFA:
                return config.getToPdfaPath();
            case DONE_PDFA:
                return config.getDonePdfa();
            case UPLOAD_TDAR:
                return config.getUploadPath();
        }
        return null;
    }

    public Integer getOrd() {
        switch (this) {
            case TO_PDFA:
                return 1;
            case DONE_PDFA:
                return 2;
            case UPLOAD_TDAR:
                return 3;
        }
        return null;
    }

    public void updateStatus(WorkflowStatusReport status, DropboxFile file) {
        switch (this) {
            case TO_PDFA:
                if (StringUtils.containsIgnoreCase(file.getPath(), DropboxConstants.CREATE_PDFA)) {
                    logger.trace("setting status for {} to {}", file.getPath(), this);
                    status.setToPdf(file);
                }
                break;
            case DONE_PDFA:
                if (StringUtils.containsIgnoreCase(file.getPath(), DropboxConstants.DONE_OCR)) {
                    logger.trace("setting status for {} to {}", file.getPath(), this);
                    status.setDoneOcr(file);
                }
                break;
            case UPLOAD_TDAR:
                if (StringUtils.containsIgnoreCase(file.getPath(), DropboxConstants.UPLOAD_TO_TDAR)) {
                    logger.trace("setting status for {} to {}", file.getPath(), this);
                    status.setToUpload(file);
                }
                break;
        }
    }

    public static String createKey(DropboxFile file) {
        return createKey(file.getPath());
    }

    public static String createKey(String path) {
        String key = path.toLowerCase();
        for (Phases phase : Phases.values()) {

            key = StringUtils.remove(key, phase.getPath().toLowerCase());
        }
        key = StringUtils.remove(key, config.getBaseDropboxPath().toLowerCase());
        key = StringUtils.replace(key, "/" + DropboxConstants.INPUT + "/", "/");
        key = StringUtils.replace(key, "/" + DropboxConstants.OUTPUT + "/", "/");
        if (key.endsWith("/")) {
            key = StringUtils.substringAfter(key, "/");
        }
        key = StringUtils.replace(key, "_ocr_pdfa.pdf", ".pdf");
        return key;
    }

    public String mutatePath(final String path) {
        String nPath = path;
        for (Phases phase : Phases.values()) {
            nPath = StringUtils.removeStartIgnoreCase(nPath, phase.getPath());
        }
        return this.getPath() + nPath;
    }

    public Phases getNextPhase() {
        switch (this) {
            case TO_PDFA:
                return null;
            case DONE_PDFA:
                return Phases.UPLOAD_TDAR;
            case UPLOAD_TDAR:
                return null;
        }
        return null;
    }
}
