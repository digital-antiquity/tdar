package org.tdar.core.bean.notification;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;

@Entity
@Table(name = "email_queue")
public class Email extends AbstractPersistable {

    private static final String SEPARATOR_CHARS = ";";
    private static final long serialVersionUID = -5791173542997998092L;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = FieldLength.FIELD_LENGTH_25)
    private Status status = Status.QUEUED;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = FieldLength.FIELD_LENGTH_50)
    private EmailType type;

    @Column(name = "user_generated", nullable = false, columnDefinition = "boolean default TRUE")
    private boolean userGenerated = true;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "message")
    private String message;

    @Column(name = "subject", length = FieldLength.FIELD_LENGTH_1024)
    private String subject;

    @Column(name="message_uuid", length = FieldLength.FIELD_LENGTH_32)
    private String messageUuid;
    
    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = false, name = "from_user_id")
    private TdarUser fromUser;
    
    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH })
    @JoinColumn(nullable = false, name = "to_person_id")
    private Person toUser;
    
    @Column(name = "from_address", length = FieldLength.FIELD_LENGTH_255)
    private String from;

    @Column(name = "to_address", length = FieldLength.FIELD_LENGTH_1024)
    private String to;

    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    @Column(name = "date_sent")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSent;

    @Column(name = "number_of_tries")
    private Integer numberOfTries = 5;

    @Column(name = "error_message", length = FieldLength.FIELD_LENGTH_2048)
    private String errorMessage;

    @ManyToOne(optional=true)
    @JoinColumn(name="resource_id")
    private Resource resource;
    
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Integer getNumberOfTries() {
        return numberOfTries;
    }

    public void setNumberOfTries(Integer numberOfTries) {
        this.numberOfTries = numberOfTries;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void addToAddress(String email) {
        if (StringUtils.isNotBlank(email)) {
            if (StringUtils.isNotBlank(to)) {
                this.to += SEPARATOR_CHARS;
                this.to += email;
            } else {
                this.to = email;
            }

        }
    }

    public String[] getToAsArray() {
        return StringUtils.split(to, SEPARATOR_CHARS);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    @Override
    public String toString() {
        String fmt = "[id:%-5d from:%-20s to:%-20s sub:%-20s tries:%-3d status:%-6s]";
        String msg = String.format(fmt, getId(), from, to, StringUtils.left(subject, 20), numberOfTries, status);
        return msg;
    }

    public boolean isUserGenerated() {
        return userGenerated;
    }

    public void setUserGenerated(boolean userGenerated) {
        this.userGenerated = userGenerated;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public EmailType getType() {
        return type;
    }
    

    public void setType(EmailType type) {
        this.type = type;
    }

	public String getMessageUuid() {
		return messageUuid;
	}

	public void setMessageUuid(String messageUuid) {
		this.messageUuid = messageUuid;
	}

	public TdarUser getFromUser() {
		return fromUser;
	}

	public void setFromUser(TdarUser fromUser) {
		this.fromUser = fromUser;
	}

	public Person getToUser() {
		return toUser;
	}

	public void setToUser(Person toUser) {
		this.toUser = toUser;
	}
}
