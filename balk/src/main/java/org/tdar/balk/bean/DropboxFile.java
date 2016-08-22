package org.tdar.balk.bean;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class DropboxFile extends AbstractDropboxItem {

    private static final long serialVersionUID = 3849742039989754238L;
    
    private DropboxFile() {
        setType(ItemType.FILE);
    }

    @Column(name="extension", length=10)
    private String extension;

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
    
}
