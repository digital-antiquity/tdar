package org.tdar.balk.service;

import org.apache.commons.lang3.StringUtils;
import org.tdar.balk.bean.DropboxFile;
import org.tdar.utils.dropbox.DropboxConstants;

public enum Phases {
    TO_PDFA, DONE_PDFA, UPLOAD_TDAR;

    public String getPath() {
        switch (this) {
            case TO_PDFA:
                return "Create PDFA/input/";
            case DONE_PDFA:
                return "Create PDFA/output/";
            case UPLOAD_TDAR:
                return "Upload to tDAR/";
        }
        return null;
    }

    public void updateStatus(WorkflowStatusReport status, DropboxFile file) {
        switch (this) {
            case TO_PDFA:
                if (StringUtils.containsIgnoreCase(file.getPath(), this.getPath())) {
                    status.setToPdf(file);
                }
            case DONE_PDFA:
                if (StringUtils.containsIgnoreCase(file.getPath(), this.getPath())) {
                    status.setDoneOcr(file);
                }
            case UPLOAD_TDAR:
                if (StringUtils.containsIgnoreCase(file.getPath(), this.getPath())) {
                    status.setToUpload(file);
                }
        }
    }

    public static String createKey(DropboxFile file) {
        String key = file.getPath().toLowerCase();
        key = StringUtils.replace(key, "/input/", "/");
        key = StringUtils.replace(key, "/output/", "/");
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

}
