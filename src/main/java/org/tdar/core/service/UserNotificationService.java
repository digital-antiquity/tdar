package org.tdar.core.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.util.UserNotification;
import org.tdar.core.bean.util.UserNotificationType;
import org.tdar.core.dao.GenericDao;

/**
 * Handles requests to create or dismiss user notifications.
 */
@Service
public class UserNotificationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    @Transactional
    public List<UserNotification> getNotificationsFor(TdarUser user) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public UserNotification broadcast(String messageKey) {
        UserNotification notification = new UserNotification();
        notification.setMessageKey(messageKey);
        notification.setMessageType(UserNotificationType.SYSTEM_BROADCAST);
        genericDao.save(notification);
        return notification;
    }

    public UserNotification info(TdarUser user, String messageKey) {
        return createUserNotification(user, messageKey, UserNotificationType.INFO);
    }

    public UserNotification error(TdarUser user, String messageKey) {
        return createUserNotification(user, messageKey, UserNotificationType.ERROR);
    }

    public UserNotification warning(TdarUser user, String messageKey) {
        return createUserNotification(user, messageKey, UserNotificationType.WARNING);
    }

    private UserNotification createUserNotification(TdarUser user, String messageKey, UserNotificationType messageType) {
        UserNotification notification = new UserNotification();
        notification.setMessageKey(messageKey);
        notification.setMessageType(UserNotificationType.SYSTEM_BROADCAST);
        notification.setTdarUser(user);
        genericDao.save(notification);
        return notification;
    }

    public void dismiss(TdarUser user, UserNotification notification) {
        switch (notification.getMessageType()) {
            case SYSTEM_BROADCAST:
                user.updateDismissedNotificationsDate();
                break;
            case INFO:
                logger.debug("user {} deleting {}", user, notification);
                genericDao.delete(notification);
                break;
            default:
                logger.warn("user {} trying to delete error/warning notification {} - ignoring", user, notification);
                break;
        }

    }

}
