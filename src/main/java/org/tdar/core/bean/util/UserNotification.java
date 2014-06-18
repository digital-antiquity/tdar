package org.tdar.core.bean.util;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;

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

    @Temporal(TemporalType.DATE)
    @Column(name = "date_created")
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

    @Column(name = "message_key", nullable = false)
    private String messageKey;

    @Override
    public int compareTo(UserNotification other) {
        return new CompareToBuilder().append(messageType, other.messageType)
                .append(dateCreated, other.dateCreated)
                .toComparison();
    }
    
    @Override
    public String toString() {
        return String.format("%s for %s: %s", getMessageType(), getTdarUser(), getMessageKey());
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

}
