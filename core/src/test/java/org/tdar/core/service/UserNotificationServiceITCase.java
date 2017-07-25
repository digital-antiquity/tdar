package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.notification.UserNotificationDisplayType;
import org.tdar.core.bean.notification.UserNotificationType;

public class UserNotificationServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    private UserNotificationService userNotificationService;

    private List<UserNotification> initialNotifications;

    @Before
    public void setUp() {
        // clean out "new" notifications 
        userNotificationService.findAll().forEach(note -> {
            if (note.getMessageType() == UserNotificationType.SYSTEM_BROADCAST && !note.getMessageKey().contains("lithic")) {
                logger.debug("deleting: {} {} ", note.getMessageKey(), note );
                genericService.delete(note);
                genericService.synchronize();
            }
        });
        initialNotifications = userNotificationService.findAll();
    }

    @Test
    @Rollback
    public void testDismissNotifications() {
        TdarUser user = createAndSaveNewPerson("user-notification-test@tdar.net", "un");
        getLogger().debug("created user with id: {}", user.getId());

        UserNotification infoNotification = userNotificationService.info(user, "some info message", UserNotificationDisplayType.NORMAL);
        UserNotification broadcastNotification = userNotificationService.broadcast("some broadcast message", UserNotificationDisplayType.NORMAL);
        initialNotifications.addAll(Arrays.asList(broadcastNotification, infoNotification));
        List<UserNotification> currentNotifications = userNotificationService.getCurrentNotifications(user);
        Collections.sort(currentNotifications);
        Collections.sort(initialNotifications);
        assertEquals(initialNotifications, currentNotifications);
        List<UserNotification> allNotifications = userNotificationService.findAll();
        Collections.sort(allNotifications);
        assertEquals(initialNotifications, allNotifications);
        assertNull(user.getDismissedNotificationsDate());
        userNotificationService.dismiss(user, infoNotification);
        assertNull("Dismissing a targeted message should not update dismissedNotificationsDate", user.getDismissedNotificationsDate());
        userNotificationService.dismiss(user, broadcastNotification);
        assertNotNull("Dismissing a broadcast message should update dismissedNotificationsDate", user.getDismissedNotificationsDate());
        assertEquals(1, user.getDismissedNotificationsDate().compareTo(broadcastNotification.getDateCreated()));
        assertTrue("Test user dismissed notification, notification set should be empty",
                userNotificationService.getCurrentNotifications(user).isEmpty());
        assertEquals("broadcast notifications should still exist even after dismissed", 2,
                userNotificationService.findAll(UserNotificationType.SYSTEM_BROADCAST).size());
    }

    @Test
    @Rollback
    public void testMultipleUserCurrentNotifications() {
        List<TdarUser> otherUsers = new ArrayList<>();
        TdarUser user = createAndSaveNewPerson("user-notification-test@tdar.net", "un");
        for (int i = 0; i < 5; i++) {
            TdarUser anotherUser = createAndSaveNewPerson(i + "usern@tdar.net", "n" + i);
            otherUsers.add(anotherUser);
            userNotificationService.info(anotherUser, "1st info for " + i, UserNotificationDisplayType.NORMAL);
            userNotificationService.info(anotherUser, "2nd info for " + i, UserNotificationDisplayType.NORMAL);
            userNotificationService.error(anotherUser, "1st error for " + i, UserNotificationDisplayType.NORMAL);
            userNotificationService.warning(anotherUser, "1st warning for " + i, UserNotificationDisplayType.NORMAL);
        }
        for (TdarUser otherUser : otherUsers) {
            List<UserNotification> currentNotifications = userNotificationService.getCurrentNotifications(otherUser);
            assertEquals(5 , currentNotifications.size());
            Map<UserNotificationType, Integer> counts = getNotificationTypeCounts(currentNotifications);
            assertEquals(2, counts.get(UserNotificationType.INFO).intValue());
            assertEquals(1, counts.get(UserNotificationType.ERROR).intValue());
            assertEquals(1, counts.get(UserNotificationType.WARNING).intValue());
            assertEquals(1 , counts.get(UserNotificationType.SYSTEM_BROADCAST).intValue());
        }
        assertEquals("The original user should only have lithic.announce broadcast",
                initialNotifications, userNotificationService.getCurrentNotifications(user));
        UserNotification broadcast = userNotificationService.broadcast("this is a test of the emergency broadcast system", UserNotificationDisplayType.NORMAL);
        initialNotifications.add(0, broadcast);
        assertEquals("original user should have 2 broadcast messages now", initialNotifications, userNotificationService.getCurrentNotifications(user));
        for (TdarUser otherUser : otherUsers) {
            List<UserNotification> currentNotifications = userNotificationService.getCurrentNotifications(otherUser);
            assertEquals(6, currentNotifications.size());
            Map<UserNotificationType, Integer> counts = getNotificationTypeCounts(currentNotifications);
            assertEquals(2, counts.get(UserNotificationType.INFO).intValue());
            assertEquals(1, counts.get(UserNotificationType.ERROR).intValue());
            assertEquals(1, counts.get(UserNotificationType.WARNING).intValue());
            assertEquals(2, counts.get(UserNotificationType.SYSTEM_BROADCAST).intValue());
        }
        assertEquals(2, userNotificationService.getCurrentNotifications(user).size());
        assertEquals(22, userNotificationService.findAll().size());
    }

    private Map<UserNotificationType, Integer> getNotificationTypeCounts(List<UserNotification> notifications) {
        Map<UserNotificationType, Integer> map = new HashMap<>();
        for (UserNotification notification : notifications) {
            Integer count = map.get(notification.getMessageType());
            if (count == null) {
                count = 0;
            }
            map.put(notification.getMessageType(), count + 1);
        }
        return map;
    }

}
