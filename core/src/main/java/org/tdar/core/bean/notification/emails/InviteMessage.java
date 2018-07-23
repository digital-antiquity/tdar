package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("INVITE_MESSAGE")
public class InviteMessage extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = -4517475490706454849L;

    @Override
    public String createSubjectLine() {
        String properName = ((TdarUser) getMap().get(EmailKeys.FROM)).getProperName();
        String tdar = TdarConfiguration.getInstance().getSiteAcronym();
        return MessageHelper.getMessage(EmailType.INVITE.getLocaleKey(), Arrays.asList(properName, tdar));
    }

}
