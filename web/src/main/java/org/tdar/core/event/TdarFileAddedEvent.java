package org.tdar.core.event;

import java.io.Serializable;

public class TdarFileAddedEvent implements Serializable {

    private static final long serialVersionUID = -2500231190172776008L;
    private Long fileId;

    public TdarFileAddedEvent(Long fileId) {
        this.setFileId(fileId);
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }
}
