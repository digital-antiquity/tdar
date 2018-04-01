package org.tdar.core.bean.notification;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.tdar.core.bean.AbstractPersistable;
import org.tdar.core.bean.FieldLength;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;

@Entity
@Table(name = "email_queue")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "aws_message_type", length = FieldLength.FIELD_LENGTH_50, discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("GENERIC")
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

    @Column(name = "message_uuid", length = FieldLength.FIELD_LENGTH_50)
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

    @ManyToOne(optional = true)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Transient
    private Map<String, Object> map = new HashMap<>();

    @Transient
    private List<File> attachments = new ArrayList<File>();

    @Transient
    private Map<String, File> inlineAttachments = new HashMap<String, File>();

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    /**
     * Sets a list of attachments that should be included in the email.
     * 
     * @param attachments
     */
    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }

    /**
     * Adds data to the parameters map for the template to consume.
     * 
     * @param key
     * @param value
     * @return
     */
    public Email addData(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

    public void addAttachment(File file) {
        this.attachments.add(file);
    }

    public void addInlineAttachment(String contentId, File file) {
        this.inlineAttachments.put(contentId, file);
    }

    public String createSubjectLine() {
        return "";
    }

    public Map<String, File> getInlineAttachments() {
        return inlineAttachments;
    }

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
