package org.tdar.core.service;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    
    @Autowired
    private XmlService xmlService;

    private TdarUser user;

    @Before
    public void setUp() {
        user = createAndSaveNewPerson("user-notification-test@mailinator.com", "un");
    }

    @Test
    @Rollback
    public void testJsonSerialization() {
        List<UserNotification> notifications = Arrays.asList(
                userNotificationService.info(user, "1st info"),
                userNotificationService.info(user, "2nd info"),
                userNotificationService.error(user, "1st error"),
                userNotificationService.warning(user, "1st warning"),
                userNotificationService.broadcast("broadcast message")
                );
        String json = xmlService.convertFilteredJsonForStream(notifications, null, null);
        getLogger().debug("json: {}", json);
    }

    @Test
    @Rollback
    public void testDismissNotifications() {
        UserNotification infoNotification = userNotificationService.info(user, "info");
        UserNotification broadcastNotification = userNotificationService.broadcast("broadcast");
        List<UserNotification> notifications = Arrays.asList(broadcastNotification, infoNotification);
        assertEquals(notifications, userNotificationService.getCurrentNotifications(user));
        List<UserNotification> allNotifications = userNotificationService.findAll();
        Collections.sort(allNotifications);
        assertEquals(notifications, allNotifications);
        assertNull(user.getDismissedNotificationsDate());
        userNotificationService.dismiss(user, infoNotification);
        assertNull(user.getDismissedNotificationsDate());
        assertEquals(1, userNotificationService.findAll().size());
        userNotificationService.dismiss(user, broadcastNotification);
        assertNotNull(user.getDismissedNotificationsDate());
        assertEquals(1, user.getDismissedNotificationsDate().compareTo(broadcastNotification.getDateCreated()));
        assertTrue("Test user dismissed notification, notification set should be empty",
                userNotificationService.getCurrentNotifications(user).isEmpty());
    }

    @Test
    @Rollback
    public void testMultipleUserCurrentNotifications() {
        List<TdarUser> otherUsers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TdarUser anotherUser = createAndSaveNewPerson(i + "usern@mailinator.com", "n" + i);
            otherUsers.add(anotherUser);
            userNotificationService.info(anotherUser, "1st info for " + i);
            userNotificationService.info(anotherUser, "2nd info for " + i);
            userNotificationService.error(anotherUser, "1st error for " + i);
            userNotificationService.warning(anotherUser, "1st warning for " + i);
        }
        for (TdarUser user : otherUsers) {
            List<UserNotification> currentNotifications = userNotificationService.getCurrentNotifications(user);
            assertEquals(4, currentNotifications.size());
            int infos = 0;
            int errors = 0;
            int warnings = 0;
            int broadcasts = 0;
            for (UserNotification notification : currentNotifications) {
                switch (notification.getMessageType()) {
                    case SYSTEM_BROADCAST:
                        fail("No broadcasts available yet.");
                        break;
                    case INFO:
                        infos++;
                        break;
                    case ERROR:
                        errors++;
                        break;
                    case WARNING:
                        warnings++;
                        break;
                }
            }
            assertEquals(2, infos);
            assertEquals(1, errors);
            assertEquals(1, warnings);
            assertEquals(0, broadcasts);
        }
        assertTrue("The original user shouldn't have any notifications", userNotificationService.getCurrentNotifications(user).isEmpty());
        userNotificationService.broadcast("this is a test of the emergency broadcast system");
        for (TdarUser user : otherUsers) {
            List<UserNotification> currentNotifications = userNotificationService.getCurrentNotifications(user);
            assertEquals(5, currentNotifications.size());
            int infos = 0;
            int errors = 0;
            int warnings = 0;
            int broadcasts = 0;
            for (UserNotification notification : currentNotifications) {
                switch (notification.getMessageType()) {
                    case SYSTEM_BROADCAST:
                        broadcasts++;
                        break;
                    case INFO:
                        infos++;
                        break;
                    case ERROR:
                        errors++;
                        break;
                    case WARNING:
                        warnings++;
                        break;
                }
            }
            assertEquals(2, infos);
            assertEquals(1, errors);
            assertEquals(1, warnings);
            assertEquals(1, broadcasts);
        }
        assertEquals(1, userNotificationService.getCurrentNotifications(user).size());
        assertEquals(21, userNotificationService.findAll().size());
    }

}
