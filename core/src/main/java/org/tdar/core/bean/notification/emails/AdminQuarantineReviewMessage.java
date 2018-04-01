package org.tdar.core.bean.notification.emails;

import java.util.Arrays;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.utils.MessageHelper;

@Entity
@DiscriminatorValue("QUARANTINE_REVIEW")
public class AdminQuarantineReviewMessage extends Email {
    /**
     * 
     */
    private static final long serialVersionUID = 2389674712822574793L;

    @Override
    public String createSubjectLine() {
        // FIXME: constants
        return MessageHelper.getMessage(EmailType.ADMIN_QUARANTINE_REVIEW.getLocaleKey(), Arrays.asList(getMap().get("totalEmails")));
    }

}
