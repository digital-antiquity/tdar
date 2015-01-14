package org.tdar.struts.data.dataOne;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

public class DataOneLogEntry implements Serializable {

    private static final long serialVersionUID = -1160217582231223023L;
    private String identifier;
    private String ipAddress;
    private String userAgent;
    private String subject;
    private String event;
    private String dateLogged;
    private long entryId;
    private String nodeIdentifier;
    private boolean shouldRecord;

    public DataOneLogEntry(HttpServletRequest servletRequest) {
        setIpAddress(servletRequest.getRemoteAddr());
        setUserAgent(servletRequest.getHeader("User-Agent"));
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }
    public String getDateLogged() {
        return dateLogged;
    }
    public void setDateLogged(String dateLogged) {
        this.dateLogged = dateLogged;
    }
    public long getEntryId() {
        return entryId;
    }
    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }
    public String getNodeIdentifier() {
        return nodeIdentifier;
    }
    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }
    public boolean isShouldRecord() {
        return shouldRecord;
    }
    public void setShouldRecord(boolean shouldRecord) {
        this.shouldRecord = shouldRecord;
    }
    
}
