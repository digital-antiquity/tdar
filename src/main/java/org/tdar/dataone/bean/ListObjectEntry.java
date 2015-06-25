package org.tdar.dataone.bean;

import java.io.Serializable;
import java.util.Date;

import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.dataone.service.DataOneService;

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
    private String contentType;

    // externalId, 'file', irf.id , dateUpdated, fileLength, checksum, latestVersion
    public ListObjectEntry(String exId, String type, Long id, Date updated) {
        this.identifier = exId;
        this.type = Type.valueOf(type);
        this.persistableId = id;
        this.dateUpdated = updated;
    }

    public ListObjectEntry(String exId, String type, Long id, Date updated, Long size, String sum, Integer version, String contentType) {
        this.identifier = exId;
        this.type = Type.valueOf(type);
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
        return ListObjectEntry.formatIdentifier(identifier, dateUpdated, type, null);
    }

    public static String formatIdentifier(String identifier, Date dateUpdated, Type type, InformationResourceFile irf) {
        if (irf == null) {
            return formatIdentifier(identifier, dateUpdated, type, null, null);
        }
        return formatIdentifier(identifier, dateUpdated, type, irf.getId(), irf.getLatestVersion());
    }

    public static String formatIdentifier(String identifier, Date dateUpdated, Type type, Long irfId, Integer version) {
        StringBuilder sb = new StringBuilder();
        sb.append(identifier).append(DataOneService.D1_SEP);
        switch (type) {
            case D1:
                sb.append(DataOneService.D1_FORMAT);
                sb.append(DataOneService.D1_SEP);
                sb.append(dateUpdated);
                break;
            case FILE:
                sb.append(irfId);
                sb.append(DataOneService.D1_VERS_SEP);
                sb.append(version);
                break;
            case TDAR:
                sb.append(DataOneService.META);
                sb.append(DataOneService.D1_SEP);
                sb.append(dateUpdated);
                break;
            default:
                break;
        }
        return sb.toString();

    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
