package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("TEST_MESSAGE")
public class TestAwsMessage extends Email {

    /**
     * 
     */
    private static final long serialVersionUID = -6057420297607199621L;

    @Override
    public String createSubjectLine() {
        String firstName = "";
        String lastName = "";
        // FIXME: constants
        firstName = (String) getMap().get("firstName");
        lastName = (String) getMap().get("lastName");

        return MessageHelper.getMessage(EmailType.TEST_EMAIL.getLocaleKey(), Arrays.asList(lastName, firstName));
    }
}
