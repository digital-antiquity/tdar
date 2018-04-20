package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("EMB_EXP_NOTIFY")
public class EmbargoExpirationNotification extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = -6861977402830459014L;

    @Override
    public String createSubjectLine() {
        return MessageHelper.getMessage(EmailType.EMBARGO_EXPIRATION_NOTIFICATION.getLocaleKey(),
                Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
    }
}
