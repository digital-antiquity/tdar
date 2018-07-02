package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("CORRECTION_MESSAGE")
public class CorrectionSuggestionMessage extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = -6656033457514113295L;

    @Override
    public String createSubjectLine() {
        Resource resource = (Resource) getMap().get(EmailKeys.RESOURCE);
        Person from = (Person) getMap().get(EmailKeys.FROM);

        return MessageHelper.getMessage(EmailType.SUGGEST_CORRECTION.getLocaleKey(), Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym(),
                String.valueOf(resource.getId()), from.getProperName()));
    }
}
