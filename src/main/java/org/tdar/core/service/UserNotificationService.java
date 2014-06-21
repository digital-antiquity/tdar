package org.tdar.core.service;


import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.util.UserNotification;
import org.tdar.core.bean.util.UserNotificationType;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.TdarNamedQueries;

/**
 * Handles requests to create or dismiss user notifications.
 */
@Service
public class UserNotificationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;
    
    @Transactional(readOnly = true)
    public UserNotification find(Long id) {
        return genericDao.find(UserNotification.class, id);
    }
    
    @Transactional(readOnly = true)
    public List<UserNotification> findAll() {
        return genericDao.findAll(UserNotification.class);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<UserNotification> getCurrentNotifications(final TdarUser user) {
        List<UserNotification> notifications = genericDao.getNamedQuery(TdarNamedQueries.QUERY_CURRENT_USER_NOTIFICATIONS).setParameter("userId", user.getId()).list();
        Date dismissedNotificationsDate = user.getDismissedNotificationsDate();
        if (dismissedNotificationsDate != null) {
            for (Iterator<UserNotification> iter = notifications.iterator(); iter.hasNext(); ) {
                if (dismissedNotificationsDate.after(iter.next().getDateCreated())) {
                    iter.remove();
                }
            }
        }
        return notifications;
    }

    @Transactional
    public UserNotification broadcast(String messageKey) {
        return createUserNotification(null, messageKey, UserNotificationType.SYSTEM_BROADCAST);
    }

    @Transactional
    public UserNotification info(TdarUser user, String messageKey) {
        return createUserNotification(user, messageKey, UserNotificationType.INFO);
    }

    @Transactional
    public UserNotification error(TdarUser user, String messageKey) {
        return createUserNotification(user, messageKey, UserNotificationType.ERROR);
    }

    @Transactional
    public UserNotification warning(TdarUser user, String messageKey) {
        return createUserNotification(user, messageKey, UserNotificationType.WARNING);
    }

    private UserNotification createUserNotification(TdarUser user, String messageKey, UserNotificationType messageType) {
        UserNotification notification = new UserNotification();
        notification.setMessageKey(messageKey);
        notification.setMessageType(messageType);
        notification.setTdarUser(user);
        genericDao.save(notification);
        return notification;
    }

    @Transactional(readOnly=false)
    public void dismiss(TdarUser user, UserNotification notification) {
        logger.debug("user {} dismissing {}", user, notification);
        switch (notification.getMessageType()) {
            case SYSTEM_BROADCAST:
                user.updateDismissedNotificationsDate();
                genericDao.update(user);
                break;
            case INFO:
                genericDao.delete(notification);
                break;
            default:
                logger.warn("user {} trying to delete error/warning notification {} - ignoring", user, notification);
                break;
        }
    }

}
