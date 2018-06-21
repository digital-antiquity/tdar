package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("CONTACT_MESSAGE")
public class ContactMessage extends Email {

    /**
     * 
     */

    /**
     * 
     */
    private static final long serialVersionUID = -837263906888018003L;

    /**
     * 
     */

    @Override
    public String createSubjectLine() {
        Resource resource = (Resource) getMap().get(EmailKeys.RESOURCE);
        Person from = (Person) getMap().get(EmailKeys.FROM);

        return MessageHelper.getMessage(EmailType.CONTACT.getLocaleKey(), Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym(),
                String.valueOf(resource.getId()), from.getProperName()));
    }
}
