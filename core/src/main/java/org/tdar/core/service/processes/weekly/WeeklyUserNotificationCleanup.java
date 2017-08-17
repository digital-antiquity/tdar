package org.tdar.core.service.processes.weekly;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.notification.UserNotificationType;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;

@Component
@Scope("prototype")
public class WeeklyUserNotificationCleanup extends AbstractScheduledBatchProcess<UserNotification> {

    public static final String PRE_TDAR_INVOICE = "pre.tdar.invoice";
    private static final long serialVersionUID = 6906961880446184000L;
    DateTime sixMonths = DateTime.now().minusMonths(6);
    DateTime threeMonths = DateTime.now().minusMonths(3);
    private List<UserNotification> expired = new ArrayList<>();
    private List<UserNotification> old = new ArrayList<>();
    private List<UserNotification> oldBroadcast = new ArrayList<>();

    @Override
    public String getDisplayName() {
        return "User Notification Cleanup";
    }

    @Override
    public int getBatchSize() {
        return 10000;
    }

    @Override
    public Class<UserNotification> getPersistentClass() {
        return UserNotification.class;
    }

    @Override
    public void process(UserNotification note) throws Exception {
        // if we're after the expiration date...
        if (DateTime.now().isAfter(new DateTime(note.getExpirationDate()))) {
            getExpired().add(note);
            genericDao.delete(note);
            return;
        }

        
        // if it's been sitting there for 6 months and the user has logged in after
        if (sixMonths.isAfter(new DateTime(note.getDateCreated()))
                && note.getTdarUser() != null && note.getMessageType() == UserNotificationType.INFO
                && !note.getMessageKey().equalsIgnoreCase(PRE_TDAR_INVOICE)) {
            getOld().add(note);
            genericDao.delete(note);
            return;
        }
        
        // delete system broadcasts after 3 months
        if (threeMonths.isAfter(new DateTime(note.getDateCreated())) && note.getMessageType()== UserNotificationType.SYSTEM_BROADCAST) {
            getOldBroadcast().add(note);
            genericDao.delete(note);
            return;
            
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public List<UserNotification> getExpired() {
        return expired;
    }

    public void setExpired(List<UserNotification> expired) {
        this.expired = expired;
    }

    public List<UserNotification> getOldBroadcast() {
        return oldBroadcast;
    }

    public void setOldBroadcast(List<UserNotification> oldBroadcast) {
        this.oldBroadcast = oldBroadcast;
    }

    public List<UserNotification> getOld() {
        return old;
    }

    public void setOld(List<UserNotification> old) {
        this.old = old;
    }
}
