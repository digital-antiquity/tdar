package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.util.UserNotification;

public class UserNotificationServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private UserNotificationService userNotificationService;

    private TdarUser user;

    @Before
    public void setUp() {
        user = createAndSaveNewPerson("user-notification-test@mailinator.com", "un");
    }

    @Test
    @Rollback
    public void testCurrentNotifications() {
        UserNotification broadcastNotification = userNotificationService.broadcast("someMessageKey");
        List<UserNotification> notifications = Arrays.asList(broadcastNotification);
        assertEquals(notifications, userNotificationService.getCurrentNotifications(user));
        assertEquals(notifications, userNotificationService.findAll());
        assertNull(user.getDismissedNotificationsDate());
        userNotificationService.dismiss(user, broadcastNotification);
        assertNotNull(user.getDismissedNotificationsDate());
        assertEquals(1, user.getDismissedNotificationsDate().compareTo(broadcastNotification.getDateCreated()));
        // FIXME: failing at the moment, either due to invalid HQL or hibernate session flushing / synchronization
        // assertTrue("Test user dismissed notification, notification set should be empty",
        // userNotificationService.getCurrentNotifications(user).isEmpty());
    }

}
