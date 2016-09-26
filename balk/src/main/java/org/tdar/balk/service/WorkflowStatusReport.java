package org.tdar.balk.service;

import org.tdar.balk.bean.DropboxFile;

public class WorkflowStatusReport {

    private DropboxFile first;
    private DropboxFile toPdf;
    private DropboxFile doneOcr;
    private DropboxFile toUpload;
    
    public boolean isUsingWorkflow() {
        if (toPdf == null && doneOcr == null && toUpload == null) {
            return false;
        }
        return true;
    }
    
    public Phases getNextPhase() {
        if (doneOcr != null && toUpload == null) {
            return Phases.UPLOAD_TDAR;
        }
        return null;
    }
    
    public Phases getCurrentPhase() {
        if (toUpload == null && doneOcr != null) {
            return Phases.DONE_PDFA;
        }

        if (doneOcr == null && toPdf != null) {
            return Phases.TO_PDFA;
        }
        return null;
}
    
    public DropboxFile getToPdf() {
        return toPdf;
    }

    public void setToPdf(DropboxFile toPdf) {
        this.toPdf = toPdf;
    }

    public DropboxFile getDoneOcr() {
        return doneOcr;
    }

    public void setDoneOcr(DropboxFile doneOcr) {
        this.doneOcr = doneOcr;
    }

    public DropboxFile getToUpload() {
        return toUpload;
    }

    public void setToUpload(DropboxFile toUpload) {
        this.toUpload = toUpload;
    }

    public DropboxFile getFirst() {
        return first;
    }

    public void setFirst(DropboxFile first) {
        this.first = first;
    }

}
