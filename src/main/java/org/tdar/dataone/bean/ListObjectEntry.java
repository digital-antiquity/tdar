package org.tdar.dataone.bean;

import java.io.Serializable;
import java.util.Date;

public class ListObjectEntry implements Serializable {

    private static final long serialVersionUID = 8894503838119896193L;

    public enum Type {
        D1, TDAR, FILE;
    }

    private String identifier;
    private Long persistableId;
    private Type type;
    private Long size;
    private Date dateUpdated;
    private String checksum;
    private Integer version;

    // externalId, 'file', irf.id , dateUpdated, fileLength, checksum, latestVersion
    public ListObjectEntry(String exId, String type, Long id, Date updated) {
        this.identifier = exId;
        this.type = Type.valueOf(type);
        this.persistableId = id;
        this.dateUpdated = updated;
    }

    public ListObjectEntry(String exId, String type, Long id, Date updated, Long size, String sum, Integer version) {
        this.identifier = exId;
        this.type = Type.valueOf(type);
        this.persistableId = id;
        this.dateUpdated = updated;
        this.size = size;
        this.checksum = sum;
        this.version = version;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Long getPersistableId() {
        return persistableId;
    }

    public void setPersistableId(Long persistableId) {
        this.persistableId = persistableId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFormattedIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

}
