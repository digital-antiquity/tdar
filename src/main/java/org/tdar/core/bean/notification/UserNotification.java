package org.tdar.core.bean.notification;

import java.util.Date;

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
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.hibernate.annotations.Type;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.opensymphony.xwork2.TextProvider;

/**
 * Provides targeted and system broadcast notifications for users.
 * SYSTEM_BROADCAST messages are sent to every user and can be dismissed via TdarUser.updateDismissedNotificationsDate()
 * ERROR and WARNING messages are targeted to specific users and cannot be dismissed.
 * 
 * INFO messages can be dismissed.
 */
@Entity
@Table(name = "user_notification")
public class UserNotification extends Persistable.Base implements Comparable<UserNotification> {

    private static final long serialVersionUID = -644485386619012665L;

    public UserNotification() {
    }

    public UserNotification(String key, UserNotificationType type, UserNotificationDisplayType displayType, TdarUser user) {
        this.messageKey = key;
        this.messageType = type;
        this.messageDisplayType = displayType;
        this.tdarUser = user;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_created", nullable = false)
    private Date dateCreated = new Date();

    @Temporal(TemporalType.DATE)
    @Column(name = "expiration_date")
    private Date expirationDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private TdarUser tdarUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 32, nullable = false)
    private UserNotificationType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_type", length = 32, nullable = false)
    private UserNotificationDisplayType messageDisplayType = UserNotificationDisplayType.NORMAL;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "message_key", nullable = false)
    private String messageKey;

    @JsonProperty
    @Transient
    private String message;

    @Override
    public int compareTo(UserNotification other) {
        return new CompareToBuilder().append(messageType, other.messageType)
                .append(other.dateCreated, dateCreated)
                .toComparison();
    }

    @Override
    public String toString() {
        return String.format("%s for %s: %s (%s)", getMessageType(), getTdarUser(), getMessageKey(), getDateCreated());
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public UserNotificationType getMessageType() {
        return messageType;
    }

    public void setMessageType(UserNotificationType messageType) {
        this.messageType = messageType;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public TdarUser getTdarUser() {
        return tdarUser;
    }

    public void setTdarUser(TdarUser tdarUser) {
        this.tdarUser = tdarUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(TextProvider textProvider) {
        String text = textProvider.getText(getMessageKey());
        if (StringUtils.isBlank(text)) {
            text = getMessageKey();
        }
        setMessage(text);
    }

    public UserNotificationDisplayType getMessageDisplayType() {
        return messageDisplayType;
    }

    public void setMessageDisplayType(UserNotificationDisplayType messageDisplayType) {
        this.messageDisplayType = messageDisplayType;
    }

}
