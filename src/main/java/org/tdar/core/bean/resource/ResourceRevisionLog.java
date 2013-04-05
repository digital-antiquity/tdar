package org.tdar.core.bean.resource;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;

/**
 * $Id$
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name="resource_revision_log")
public class ResourceRevisionLog extends Persistable.Base {
    
    private static final long serialVersionUID = -6544867903833975781L;

    @ManyToOne(optional=true)
    @NotFound(action=NotFoundAction.IGNORE)
    @ForeignKey(name="none")
    private Resource resource;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable=false, name="timestamp")
    private Date timestamp;
    
    // the action taken 
    @Column(name="log_message", length=512)
    private String logMessage;
    
    @ManyToOne(optional=false)
    private Person person;
    
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name="payload", nullable=true)
    private String payload;
    
    @Override
    public java.util.List<?> getEqualityFields() {
        return Arrays.asList(resource, logMessage, person, payload, timestamp);
    };

    
    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date dateRevised) {
        this.timestamp = dateRevised;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String action) {
        this.logMessage = action;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person actor) {
        this.person = actor;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
