package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("NEW_USER_WELCOME")
public class NewUserWelcomeMessage extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = -5682400223003342499L;

    /**
     * 
     */

    @Override
    public String createSubjectLine() {
        logger.debug("generating the subject line for NewUserWelcomeMessage");
        String tdar = TdarConfiguration.getInstance().getSiteAcronym();
        return MessageHelper.getMessage(EmailType.NEW_USER_WELCOME.getLocaleKey(), Arrays.asList(tdar));
    }

}
