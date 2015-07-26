package org.tdar.dataone.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.dataone.service.types.v1.Event;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.LogEntry;
import org.dataone.service.types.v1.NodeReference;
import org.tdar.core.bean.FieldLength;
import org.tdar.dataone.service.DataOneUtils;

@Entity()
@Table(name = "dataone_log")
public class LogEntryImpl implements Serializable {

    private static final String DATAONE_UNAUTHENTICATED_SUBJECT = "DC=dataone, DC=org";

    private static final long serialVersionUID = 851796258064667869L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = -1L;

    @Column(name = "identifier", length = FieldLength.FIELD_LENGTH_100)
    private String identifier;
    @Column(name = "user_agent", length = FieldLength.FIELD_LENGTH_1024)
    private String userAgent;
    @Column(name = "ip_address", length = FieldLength.FIELD_LENGTH_50)
    private String ipAddress;

    @Column(name = "event", length = FieldLength.FIELD_LENGTH_50)
    @Enumerated(EnumType.STRING)
    private Event event;
    
    @Column(name = "subject", length = FieldLength.FIELD_LENGTH_255)
    private String subject = DATAONE_UNAUTHENTICATED_SUBJECT;

    @Column(name = "date_logged", nullable =false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateLogged;

    @Column(name = "node_reference", length = FieldLength.FIELD_LENGTH_255)
    private String nodeReference;

    public LogEntryImpl() {
    }

    public LogEntryImpl(String id2, HttpServletRequest request, Event eventName) {
        this.identifier = id2;
        this.ipAddress = request.getRemoteAddr();
        this.userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        this.event = eventName;
        this.dateLogged = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDateLogged() {
        return dateLogged;
    }

    public void setDateLogged(Date dateLogged) {
        this.dateLogged = dateLogged;
    }

    public String getNodeReference() {
        return nodeReference;
    }

    public void setNodeReference(String nodeReference) {
        this.nodeReference = nodeReference;
    }

    /**
     * Convert an entry to a DataOne LogEntry
     * @return
     */
    
    public LogEntry toEntry() {
        LogEntry entry = new LogEntry();
        entry.setDateLogged(getDateLogged());
        entry.setEntryId(getId().toString());
        entry.setEvent(getEvent());
        Identifier identifier = new Identifier();
        identifier.setValue(getIdentifier());
        entry.setIdentifier(identifier);
        NodeReference nodeRef = new NodeReference();
        if (getNodeReference() == null) {
            nodeRef.setValue("");
        } else {
            nodeRef.setValue(getNodeReference());
        }
        entry.setNodeIdentifier(nodeRef);
        entry.setIpAddress(getIpAddress());
        entry.setUserAgent(getUserAgent());
        entry.setSubject(DataOneUtils.createSubject(getSubject()));
        return entry;
    }

}
