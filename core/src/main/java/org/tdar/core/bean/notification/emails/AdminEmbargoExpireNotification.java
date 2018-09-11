package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("ADMIN_EMBARGO_EXPIRE")
public class AdminEmbargoExpireNotification extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = 4337253829902982350L;

    @Override
    public String createSubjectLine() {
        return MessageHelper.getMessage(EmailType.EMBARGO_EXPIRATION_ADMIN_NOTIFICATION.getLocaleKey(),
                Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
    }
}
