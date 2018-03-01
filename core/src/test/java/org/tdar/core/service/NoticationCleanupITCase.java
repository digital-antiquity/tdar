package org.tdar.core.service;

import static org.junit.Assert.assertFalse;

import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.notification.UserNotificationDisplayType;
import org.tdar.core.bean.notification.UserNotificationType;
import org.tdar.core.service.processes.weekly.WeeklyUserNotificationCleanup;

public class NoticationCleanupITCase extends AbstractIntegrationTestCase  {

    @Autowired
    WeeklyUserNotificationCleanup cleanup;
    
    @Test
    @Rollback
    public void testCleanupDefault() {
        cleanup.execute();
        logger.debug("       expired: {}", cleanup.getExpired());
        logger.debug("old broadcasts: {}", cleanup.getOldBroadcast());
        logger.debug("           old: {}", cleanup.getOld());
        assertNotEmpty("should have >1 broadcasts", cleanup.getOldBroadcast());
        assertEmpty("should have 0 old", cleanup.getOld());
        assertEmpty("should have 0 expired", cleanup.getExpired());
    }


    @Test
    @Rollback
    public void testCleanup() {
        UserNotification notification = new UserNotification("test", UserNotificationType.INFO, UserNotificationDisplayType.NORMAL, getBasicUser());
        notification.setExpirationDate(DateTime.now().minusDays(10).toDate());
        genericService.saveOrUpdate(notification);
        UserNotification notification2 = new UserNotification("test", UserNotificationType.INFO, UserNotificationDisplayType.NORMAL, getBasicUser());
        notification2.setDateCreated(DateTime.now().minusDays(5000).toDate());
        genericService.saveOrUpdate(notification2);
        cleanup.execute();
        logger.debug("       expired: {}", cleanup.getExpired());
        logger.debug("old broadcasts: {}", cleanup.getOldBroadcast());
        logger.debug("           old: {}", cleanup.getOld());
        assertNotEmpty("should have >1 broadcasts", cleanup.getOldBroadcast());
        assertNotEmpty("should have 1 old", cleanup.getOld());
        assertNotEmpty("should have 1 expired", cleanup.getExpired());
    }

    @Test
    @Rollback
    public void testCleanupRetainsInvoiceAndSystemBroadcast() {
        UserNotification notification = new UserNotification(WeeklyUserNotificationCleanup.PRE_TDAR_INVOICE, UserNotificationType.INFO, UserNotificationDisplayType.NORMAL, getBasicUser());
        notification.setDateCreated(DateTime.now().minusDays(1000).toDate());
        genericService.saveOrUpdate(notification);
        UserNotification broadcast = new UserNotification("test", UserNotificationType.SYSTEM_BROADCAST, UserNotificationDisplayType.NORMAL, getBasicUser());
        broadcast.setDateCreated(DateTime.now().minusDays(10).toDate());
        genericService.saveOrUpdate(broadcast);
        cleanup.execute();
        logger.debug("       expired: {}", cleanup.getExpired());
        logger.debug("old broadcasts: {}", cleanup.getOldBroadcast());
        logger.debug("           old: {}", cleanup.getOld());
        assertNotEmpty("should have 1 broadcasts", cleanup.getOldBroadcast());
        assertFalse("does not contain 'test'", cleanup.getOldBroadcast().contains(broadcast));
        assertEmpty("should not have 1 old", cleanup.getOld());
    }
}
