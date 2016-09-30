package org.tdar.balk.service;

import org.apache.commons.lang3.StringUtils;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.utils.dropbox.DropboxConstants;

public enum Phases {
    TO_PDFA, DONE_PDFA, UPLOAD_TDAR;

    public String getPath() {
        switch (this) {
            case TO_PDFA:
                return DropboxConstants.TO_PDFA_PATH;
            case DONE_PDFA:
                return DropboxConstants.DONE_PDFA_PATH;
            case UPLOAD_TDAR:
                return DropboxConstants.UPLOAD_PATH;
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
                if (StringUtils.containsIgnoreCase(file.getPath(), this.getPath())) {
                    status.setToPdf(file);
                }
                break;
            case DONE_PDFA:
                if (StringUtils.containsIgnoreCase(file.getPath(), this.getPath())) {
                    status.setDoneOcr(file);
                }
                break;
            case UPLOAD_TDAR:
                if (StringUtils.containsIgnoreCase(file.getPath(), this.getPath())) {
                    status.setToUpload(file);
                }
                break;
        }
    }

    public static String createKey(DropboxFile file) {
        String key = file.getPath().toLowerCase();
        key = StringUtils.replace(key, "/" + DropboxConstants.INPUT + "/", "/");
        key = StringUtils.replace(key, "/" + DropboxConstants.OUTPUT + "/", "/");
        key = StringUtils.remove(key, DropboxConstants.CLIENT_DATA.toLowerCase());
        key = StringUtils.substringAfter(key, "/");
        key = StringUtils.replace(key, "_ocr_pdfa.pdf", ".pdf");
        return key;
    }

    public String mutatePath(final String path) {
        String nPath = path;
        nPath = StringUtils.removeStartIgnoreCase(nPath, DropboxConstants.CLIENT_DATA.toLowerCase());
        for (Phases phase : Phases.values()) {
            nPath = StringUtils.removeStartIgnoreCase(nPath, phase.getPath());
        }
        return DropboxConstants.CLIENT_DATA + this.getPath() + nPath;
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
