package org.tdar.dataone.service;

import java.io.Reader;
import java.util.Date;

import org.tdar.core.bean.resource.InformationResource;
import org.tdar.dataone.bean.EntryType;

public class ObjectResponseContainer {

    private String objectFormat = "eml://ecoinformatics.org/eml-2.0.1";
    private Date lastModified = new Date();
    private String contentType = "binary/binray";
    private String checksum = "SHA-1,2e01e17467891f7c933dbaa00e1459d23db3fe4f";
    private String identifier;
    private int size;
    private Reader reader;
    private InformationResource tdarResource;
    private EntryType type;
    
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

    public Long getSerialVersionId() {
        return getLastModified().getTime();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int length) {
        this.size = length;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public void setTdarResource(InformationResource ir) {
        this.tdarResource = ir;
    }

    public InformationResource getTdarResource() {
        return this.tdarResource;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

}
