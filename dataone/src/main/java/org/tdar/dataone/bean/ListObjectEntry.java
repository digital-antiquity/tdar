package org.tdar.dataone.bean;

import java.io.Serializable;
import java.util.Date;

import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.dataone.service.DataOneService;

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
        return ListObjectEntry.formatIdentifier(identifier, dateUpdated, type, null);
    }

    public static String formatIdentifier(String identifier, Date dateUpdated, EntryType type, InformationResourceFile irf) {
        if (irf == null) {
            return formatIdentifier(identifier, dateUpdated, type, null, null);
        }
        return formatIdentifier(identifier, dateUpdated, type, irf.getId(), irf.getLatestVersion());
    }
    
    public static String webSafeDoi(String identfier) {
        // switching back DOIs to have : in them instead of /.
        return identfier.replace("/", ":");
    }

    /**
     * The Identifier is constructed by combining the type with additional information. As we do not track every version of a
     * record in tDAR, we use the "date" in the identifier name to differentiate within D1.
     * @param identifier
     * @param dateUpdated
     * @param type
     * @param irfId
     * @param version
     * @return
     */
    public static String formatIdentifier(String identifier, Date dateUpdated, EntryType type, Long irfId, Integer version) {
        StringBuilder sb = new StringBuilder();
        sb.append(webSafeDoi(identifier)).append(DataOneService.D1_SEP);
        switch (type) {
            case D1:
                sb.append(DataOneService.D1_FORMAT);
                sb.append(dateUpdated.getTime());
                break;
            case FILE:
                sb.append(irfId);
                sb.append(DataOneService.D1_VERS_SEP);
                sb.append(version);
                break;
            case TDAR:
                sb.append(DataOneService.META);
                sb.append(DataOneService.D1_VERS_SEP);
                sb.append(dateUpdated.getTime());
                break;
            default:
                break;
        }
        return sb.toString().replace(" ", "%20");

    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
