package org.tdar.core.service.pdf;

import java.io.File;
import java.io.Serializable;

public class PDFMergeWrapper implements Serializable {

    private static final long serialVersionUID = 3411714473307807362L;

    private TdarPDFMergerUtility merger = new TdarPDFMergerUtility();
    private boolean successful = false;
    private String failureReason;
    private File document;

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public TdarPDFMergerUtility getMerger() {
        return merger;
    }

    public void setMerger(TdarPDFMergerUtility merger) {
        this.merger = merger;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public File getDocument() {
        return document;
    }

    public void setDocument(File document) {
        this.document = document;
    }
}
