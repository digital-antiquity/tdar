package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("OVRDRW_NOTIFY")
public class OverdrawnNotification extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = 4083360442424946685L;

    @Override
    public String createSubjectLine() {
        return MessageHelper.getMessage(EmailType.OVERDRAWN_NOTIFICATION.getLocaleKey(), Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
    }
}
