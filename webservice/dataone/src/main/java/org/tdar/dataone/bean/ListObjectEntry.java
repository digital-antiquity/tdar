package org.tdar.dataone.bean;

import java.io.Serializable;
import java.util.Date;

import org.tdar.dataone.service.IdentifierParser;

/**
 * Helper object for tracking an Object Response. 
 * @author abrin
 *
 */
public class ListObjectEntry implements Serializable {

    private static final long serialVersionUID = 8894503838119896193L;


    private String identifier;
    private Long persistableId;
    private EntryType type;
    private Long size;
    private Date dateUpdated;
    private String checksum;
    private Integer version;
    private String contentType;

    
    @Override
    public String toString() {
        return String.format("id: %s type: %s", identifier, type);
    }

    public ListObjectEntry(String exId, String type, Long id, Date updated) {
        this.identifier = exId;
        this.type = EntryType.valueOf(type);
        this.persistableId = id;
        this.dateUpdated = updated;
    }

    public ListObjectEntry(String exId, String type_, Long id, Date updated, Long size, String sum, Integer version, String contentType) {
        this.identifier = exId;
        this.type = EntryType.valueOf(type_);
        this.persistableId = id;
        this.dateUpdated = updated;
        this.size = size;
        this.checksum = sum;
        this.version = version;
        this.setContentType(contentType);
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

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
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
        return IdentifierParser.formatIdentifier(identifier, dateUpdated, type, null);
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
