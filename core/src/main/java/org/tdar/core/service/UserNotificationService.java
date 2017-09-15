package org.tdar.core.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.notification.UserNotificationDisplayType;
import org.tdar.core.bean.notification.UserNotificationType;

import com.opensymphony.xwork2.TextProvider;

public interface UserNotificationService {

    UserNotification find(Long id);

    List<UserNotification> findAll();

    List<UserNotification> findAll(UserNotificationType userNotificationType);

    /**
     * Returns all UserNotifications with initialized message fields. Messages are looked up their messageKey via the given TextProvider.
     * 
     * @param provider
     */
    List<UserNotification> findAll(TextProvider provider);

    List<UserNotification> getCurrentNotifications(TdarUser user);

    UserNotification broadcast(String messageKey, UserNotificationDisplayType displayType);

    UserNotification info(TdarUser user, String messageKey);

    UserNotification info(TdarUser user, String messageKey, UserNotificationDisplayType displayType);

    UserNotification error(TdarUser user, String messageKey);

    UserNotification error(TdarUser user, String messageKey, UserNotificationDisplayType displayType);

    UserNotification warning(TdarUser user, String messageKey);

    UserNotification warning(TdarUser user, String messageKey, UserNotificationDisplayType displayType);

    void dismiss(TdarUser user, UserNotification notification);

}