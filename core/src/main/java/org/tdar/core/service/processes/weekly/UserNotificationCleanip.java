package org.tdar.core.service.processes.weekly;

import org.joda.time.DateTime;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.UserNotification;
import org.tdar.core.bean.notification.UserNotificationType;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;

@Component
@Scope("prototype")
public class UserNotificationCleanip extends AbstractScheduledBatchProcess<UserNotification> {
    /**
     * 
     */
    private static final long serialVersionUID = 6906961880446184000L;
    DateTime sixMonths = DateTime.now().minusMonths(6);
    DateTime threeMonths = DateTime.now().minusMonths(3);

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
            genericDao.delete(note);
            return;
        }

        // if it's been sitting there for 6 months and the user has logged in aftter
        if (sixMonths.isAfter(new DateTime(note.getDateCreated()))
                && note.getTdarUser() != null && note.getMessageType() == UserNotificationType.INFO
                && sixMonths.isAfter(new DateTime(note.getTdarUser().getLastLogin()))) {
            genericDao.delete(note);
            return;
        }
        
        // delete system broadcasts after 3 months
        if (threeMonths.isAfter(new DateTime(note.getDateCreated())) && note.getMessageType()== UserNotificationType.SYSTEM_BROADCAST) {
            genericDao.delete(note);
            return;
            
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
