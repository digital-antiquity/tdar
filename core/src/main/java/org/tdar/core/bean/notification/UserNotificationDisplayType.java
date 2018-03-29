package org.tdar.core.bean.notification;

/**
 * Enum for user notification message types:
 * 
 * SYSTEM_BROADCAST messages are sent to every user and can be dismissed via TdarUser.updateDismissedNotificationsDate()
 * ERROR and WARNING messages are targeted to specific users and cannot be dismissed
 * 
 * INFO messages can be dismissed.
 * 
 */
public enum UserNotificationDisplayType {

    NORMAL, FREEMARKER;

    public boolean isNormal() {
        return this == NORMAL;
    }
}
