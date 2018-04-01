package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("INVITE_ACCEPTED")
public class InviteAcceptedMessage extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = -6035465612724342667L;

    @Override
    public String createSubjectLine() {
        return MessageHelper.getInstance().getText(EmailType.INVITE_ACCEPTED.getLocaleKey(), Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym()));
    }
}
