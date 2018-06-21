package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("EMB_EXP_WARN")
public class EmbargoExpirationWarningNotification extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = 6235627079633333613L;

    @Override
    public String createSubjectLine() {
        return MessageHelper.getMessage(EmailType.EMBARGO_EXPIRATION_WARNING_NOTIFICATION.getLocaleKey(),
                Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
    }
}
