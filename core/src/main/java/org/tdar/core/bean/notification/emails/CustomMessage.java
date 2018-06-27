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
@DiscriminatorValue("CUSTOM_MESSAGE")
public class CustomMessage extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = 1930890633663795484L;

    @Override
    public String createSubjectLine() {
        Resource resource = (Resource) getMap().get(EmailKeys.RESOURCE);
        Person from = (Person) getMap().get(EmailKeys.FROM);
        String subjectPart = (String) getMap().get(EmailKeys.CUSTOM_NAME);
        return MessageHelper.getMessage(EmailType.CUSTOM_CONTACT.getLocaleKey(), Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym(),subjectPart,
                String.valueOf(resource.getId()), from.getProperName()));
    }
}
