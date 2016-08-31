package org.tdar.balk.service;

import org.tdar.balk.bean.DropboxFile;

public class WorkflowStatusReport {

    private DropboxFile toPdf;
    private DropboxFile doneOcr;
    private DropboxFile toUpload;

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

}
