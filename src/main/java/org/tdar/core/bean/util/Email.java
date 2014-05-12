package org.tdar.core.bean.util;

import java.util.Date;

import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.Persistable;

@Table(name="email")
public class Email extends Persistable.Base {

    private static final String SEPARATOR_CHARS = ";";
    private static final long serialVersionUID = -5791173542997998092L;

    public enum Status {
        QUEUED,
        ERROR,
        SENT;
    }
    
    private Status status = Status.QUEUED;
    private String message;
    private String subject;
    private String from;
    private String to;
    private Date date = new Date();
    private Integer numberOfTries = 5;
    private String errorMessage;
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
                this.to  += SEPARATOR_CHARS;
            }
            this.to += email;
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
}
