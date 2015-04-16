package org.tdar.dataone.service;

import java.util.Date;

public class ObjectResponseContainer {

    private String objectFormat = "eml://ecoinformatics.org/eml-2.0.1";
    private Date lastModified = new Date();
    private String contentType = "binary/binray";
    private String checksum = "SHA-1,2e01e17467891f7c933dbaa00e1459d23db3fe4f";
    private String identifier;
    private Long serialVersionId = 1234L;
    private Object object;
    
    public String getObjectFormat() {
        return objectFormat;
    }

    public void setObjectFormat(String objectFormat) {
        this.objectFormat = objectFormat;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Long getSerialVersionId() {
        return serialVersionId;
    }

    public void setSerialVersionId(Long serialVersionId) {
        this.serialVersionId = serialVersionId;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }


}
