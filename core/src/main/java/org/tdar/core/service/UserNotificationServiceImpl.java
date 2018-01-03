package org.tdar.core.service;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.notification.UserNotificationDisplayType;
import org.tdar.core.bean.notification.UserNotificationType;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * Handles requests to create or dismiss user notifications.
 */
@Service
public class UserNotificationServiceImpl implements UserNotificationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#find(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public UserNotification find(Long id) {
        return genericDao.find(UserNotification.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#findAll()
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserNotification> findAll() {
        return genericDao.findAll(UserNotification.class);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#findAll(org.tdar.core.bean.notification.UserNotificationType)
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<UserNotification> findAll(UserNotificationType userNotificationType) {
        Query query = genericDao.getNamedQuery(TdarNamedQueries.QUERY_USER_NOTIFICATIONS_BY_TYPE);
        query.setParameter("messageType", userNotificationType);
        return query.getResultList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#findAll(com.opensymphony.xwork2.TextProvider)
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserNotification> findAll(TextProvider provider) {
        List<UserNotification> allNotifications = findAll();
        Collections.sort(allNotifications);
        for (UserNotification notification : allNotifications) {
            notification.setMessage(provider);
        }
        return allNotifications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#getCurrentNotifications(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<UserNotification> getCurrentNotifications(final TdarUser user) {
        if (PersistableUtils.isNullOrTransient(user)) {
            return Collections.emptyList();
        }
        List<UserNotification> notifications = genericDao.getNamedQuery(TdarNamedQueries.QUERY_CURRENT_USER_NOTIFICATIONS).setParameter("userId", user.getId())
                .getResultList();
        Date dismissedNotificationsDate = user.getDismissedNotificationsDate();
        if (dismissedNotificationsDate != null) {
            for (Iterator<UserNotification> iter = notifications.iterator(); iter.hasNext();) {
                if (dismissedNotificationsDate.after(iter.next().getDateCreated())) {
                    iter.remove();
                }
            }
        }
        return notifications;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#broadcast(java.lang.String, org.tdar.core.bean.notification.UserNotificationDisplayType)
     */
    @Override
    @Transactional
    public UserNotification broadcast(String messageKey, UserNotificationDisplayType displayType) {
        return createUserNotification(null, messageKey, UserNotificationType.SYSTEM_BROADCAST, displayType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#info(org.tdar.core.bean.entity.TdarUser, java.lang.String)
     */
    @Override
    @Transactional
    public UserNotification info(TdarUser user, String messageKey) {
        return info(user, messageKey, UserNotificationDisplayType.NORMAL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#info(org.tdar.core.bean.entity.TdarUser, java.lang.String,
     * org.tdar.core.bean.notification.UserNotificationDisplayType)
     */
    @Override
    @Transactional
    public UserNotification info(TdarUser user, String messageKey, UserNotificationDisplayType displayType) {
        return createUserNotification(user, messageKey, UserNotificationType.INFO, displayType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#error(org.tdar.core.bean.entity.TdarUser, java.lang.String)
     */
    @Override
    @Transactional
    public UserNotification error(TdarUser user, String messageKey) {
        return error(user, messageKey, UserNotificationDisplayType.NORMAL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#error(org.tdar.core.bean.entity.TdarUser, java.lang.String,
     * org.tdar.core.bean.notification.UserNotificationDisplayType)
     */
    @Override
    @Transactional
    public UserNotification error(TdarUser user, String messageKey, UserNotificationDisplayType displayType) {
        return createUserNotification(user, messageKey, UserNotificationType.ERROR, displayType);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#warning(org.tdar.core.bean.entity.TdarUser, java.lang.String)
     */
    @Override
    @Transactional
    public UserNotification warning(TdarUser user, String messageKey) {
        return warning(user, messageKey, UserNotificationDisplayType.NORMAL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#warning(org.tdar.core.bean.entity.TdarUser, java.lang.String,
     * org.tdar.core.bean.notification.UserNotificationDisplayType)
     */
    @Override
    @Transactional
    public UserNotification warning(TdarUser user, String messageKey, UserNotificationDisplayType displayType) {
        return createUserNotification(user, messageKey, UserNotificationType.WARNING, displayType);
    }

    private UserNotification createUserNotification(TdarUser user, String messageKey, UserNotificationType messageType,
            UserNotificationDisplayType displayType) {
        UserNotification notification = new UserNotification(messageKey, messageType, displayType, user);
        genericDao.save(notification);
        return notification;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.UserNotificationService#dismiss(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.notification.UserNotification)
     */
    @Override
    @Transactional(readOnly = false)
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
