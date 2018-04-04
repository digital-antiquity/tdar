package org.tdar.core.bean.notification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.notification.emails.AccessExpirationAdminNotification;
import org.tdar.core.bean.notification.emails.AccessExpirationOwnerNotification;
import org.tdar.core.bean.notification.emails.AccessExpirationUserNotification;
import org.tdar.core.bean.notification.emails.AccessRequestCustomMessage;
import org.tdar.core.bean.notification.emails.AccessRequestGrantedMessage;
import org.tdar.core.bean.notification.emails.AccessRequestRejectedMessage;
import org.tdar.core.bean.notification.emails.AdminNotificationMessage;
import org.tdar.core.bean.notification.emails.AdminOverdrawnNotification;
import org.tdar.core.bean.notification.emails.AdminQuarantineReviewMessage;
import org.tdar.core.bean.notification.emails.AdminReportNewUsersMessage;
import org.tdar.core.bean.notification.emails.ContactMessage;
import org.tdar.core.bean.notification.emails.CorrectionSuggestionMessage;
import org.tdar.core.bean.notification.emails.CustomMessage;
import org.tdar.core.bean.notification.emails.EmbargoExpirationAdminNotification;
import org.tdar.core.bean.notification.emails.EmbargoExpirationNotification;
import org.tdar.core.bean.notification.emails.EmbargoExpirationWarningNotification;
import org.tdar.core.bean.notification.emails.InviteAcceptedMessage;
import org.tdar.core.bean.notification.emails.InviteMessage;
import org.tdar.core.bean.notification.emails.MergeRequestMessage;
import org.tdar.core.bean.notification.emails.MonthlyUserStatisticsMessage;
import org.tdar.core.bean.notification.emails.NewUserWelcomeMessage;
import org.tdar.core.bean.notification.emails.OverdrawnNotification;
import org.tdar.core.bean.notification.emails.RequestAccessMessage;
import org.tdar.core.bean.notification.emails.TestAwsMessage;
import org.tdar.utils.MessageHelper;

public enum EmailType implements Localizable, HasLabel {
    INVITE("invite/invite.ftl", "no-reply@tdar.org", InviteMessage.class),
    INVITE_ACCEPTED("invite/invite-accepted.ftl", "no-reply@tdar.org", InviteAcceptedMessage.class),
    NEW_USER_NOTIFY("email_new_users.ftl"),
    NEW_USER_WELCOME("email-welcome.ftl", "no-reply@tdar.org", NewUserWelcomeMessage.class),
    TRANSACTION_COMPLETE_ADMIN("transaction-complete-admin.ftl"),

    PERMISSION_REQUEST_ACCEPTED("email-form/access-request-granted.ftl", "no-reply@tdar.org", AccessRequestGrantedMessage.class),
    PERMISSION_REQUEST_REJECTED("email-form/access-request-rejected.ftl", "no-reply@tdar.org", AccessRequestRejectedMessage.class),
    PERMISSION_REQUEST_CUSTOM("email-form/custom-accept.ftl", "no-reply@tdar.org", AccessRequestCustomMessage.class),

    OVERDRAWN_NOTIFICATION("overdrawn-user.ftl", null, OverdrawnNotification.class),
    RESOURCE_EXPORT("resource-export-email.ftl"),

    ADMIN_NOTIFICATION("auth-report.ftl", null, AdminNotificationMessage.class),
    ADMIN_OVERDRAWN_NOTIFICATION("overdrawn-admin.ftl", null, AdminOverdrawnNotification.class),
    ADMIN_NEW_USER_REPORT("email_new_users.ftl", null, AdminReportNewUsersMessage.class),
    ADMIN_QUARANTINE_REVIEW("email_review_message.ftl", null, AdminQuarantineReviewMessage.class),

    EMBARGO_EXPIRATION_ADMIN_NOTIFICATION("embargo/expiration-admin.ftl", null, EmbargoExpirationAdminNotification.class),
    EMBARGO_EXPIRATION_WARNING_NOTIFICATION("embargo/expiration-warning.ftl", null, EmbargoExpirationWarningNotification.class),
    EMBARGO_EXPIRATION_NOTIFICATION("embargo/expiration.ftl", null, EmbargoExpirationNotification.class),

    ACCESS_EXPIRE_ADMIN_NOTIFICATION("expire/expire_admin.ftl", null, AccessExpirationAdminNotification.class),
    ACCESS_EXPIRE_OWNER_NOTIFICATION("expire/expire_owner.ftl", null, AccessExpirationOwnerNotification.class),
    ACCESS_EXPIRE_USER_NOTIFICATION("expire/expire_owner.ftl", null, AccessExpirationUserNotification.class),

    MONTHLY_USER_STATISTICS("monthly_user_statistics.ftl", "no-reply@tdar.org", MonthlyUserStatisticsMessage.class),
    TEST_EMAIL("test-email.ftl", "no-reply@tdar.org", TestAwsMessage.class),

    // Refactored from EmailMessageType.
    CONTACT("email-form/contact.ftl",null,ContactMessage.class),
    REQUEST_ACCESS("email-form/access-request.ftl",null,RequestAccessMessage.class),
    SUGGEST_CORRECTION("email-form/correction.ftl",null,CorrectionSuggestionMessage.class),//There's no references to this enum, or the template in any java class.
    MERGE_REQUEST("email-form/merge-people.ftl", null, MergeRequestMessage.class), //There's no references to this enum, or the template in any java class.
    CUSTOM_CONTACT("email-form/custom-request.ftl",null,CustomMessage.class);

    /**
     * a string representation of the .ftl template to use
     */
    private final String templateLocation;
    /**
     * The sending address to populate on the email
     */
    private String fromAddress;

    private Class<? extends Email> emailClass;

    private EmailType(String template) {
        this.templateLocation = template;
    }

    private EmailType(String template, String fromAddress) {
        this(template);
        this.setFromAddress(fromAddress);
    }

    /**
     * If a class is specified, then when the email is rendered, it will call the createSubject() method for that subclass.
     * The email discriminator will also be stored in the database.
     * @param template
     * @param fromAddress
     * @param emailClass
     */
    private EmailType(String template, String fromAddress, Class<? extends Email> emailClass) {
        this(template, fromAddress);
        this.setEmailClass(emailClass);
    }

    public boolean requiresResource() {
        return true;
    }

    @Override
    public String getLabel() {
        return MessageHelper.getMessage(getLocaleKey());
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public static List<EmailType> valuesWithoutConfidentialFiles() {
        ArrayList<EmailType> types = new ArrayList<EmailType>();
        for (EmailType type : valuesForUserSelection()) {
            switch (type) {
                case REQUEST_ACCESS:
                case CUSTOM_CONTACT:
                case MERGE_REQUEST:
                    break;
                default:
                    types.add(type);
            }
        }
        return types;
    }

    public static List<EmailType> valuesWithoutCustom() {
        ArrayList<EmailType> types = new ArrayList<EmailType>();
        for (EmailType type : valuesForUserSelection()) {
            if (type != CUSTOM_CONTACT && type != MERGE_REQUEST) {
                types.add(type);
            }
        }
        return types;
    }

    public static List<EmailType> valuesForUserSelection() {
        return new ArrayList<EmailType>(Arrays.asList(
                CONTACT,
                REQUEST_ACCESS,
                SUGGEST_CORRECTION,
                MERGE_REQUEST,
                CUSTOM_CONTACT
        ));
    }

    public String getTemplateLocation() {
        return templateLocation;
    }

    public Class<? extends Email> getEmailClass() {
        return emailClass;
    }

    public void setEmailClass(Class<? extends Email> emailClass) {
        this.emailClass = emailClass;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }
    
}
