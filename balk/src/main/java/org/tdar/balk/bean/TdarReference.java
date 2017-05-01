package org.tdar.balk.bean;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.tdar.core.bean.AbstractPersistable;

@Entity(name = "dropbox_tdar_ref")
public class TdarReference extends AbstractPersistable {

    private static final long serialVersionUID = -5214936809901946473L;

    @Column(name = "tdar_id")
    private Long tdarId;

    @Column(name = "dropbox_id")
    private String dropboxId;

    public TdarReference(){}
    
    public TdarReference(String id, Long tdarId2) {
        this.dropboxId = id;
        this.tdarId = tdarId2;
    }

    public Long getTdarId() {
        return tdarId;
    }

    public void setTdarId(Long tdarId) {
        this.tdarId = tdarId;
    }

    public String getDropboxId() {
        return dropboxId;
    }

    public void setDropboxId(String dropboxId) {
        this.dropboxId = dropboxId;
    }


    
}
