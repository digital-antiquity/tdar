package org.tdar.core.bean.resource;

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
import org.hibernate.validator.constraints.Length;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;

/**
 * Tracks administrative changes. When the UI changes a resource, a log entry should be added.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */

@Entity
@Table(name = "resource_revision_log")
public class ResourceRevisionLog extends Persistable.Base {

    private static final long serialVersionUID = -6544867903833975781L;

    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @ForeignKey(name = "none")
    private Resource resource;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, name = "timestamp")
    private Date timestamp;

    // the action taken
    @Column(name = "log_message", length = FieldLength.FIELD_LENGTH_512)
    @Length(max = FieldLength.FIELD_LENGTH_512)
    private String logMessage;

    @ManyToOne(optional = false)
    private TdarUser person;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(name = "payload", nullable = true)
    private String payload;

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

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser actor) {
        this.person = actor;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

}
