package org.tdar.core.service.external;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.tdar.core.bean.entity.HasEmail;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.EmailMessageType;

public interface EmailService {

    void queueWithFreemarkerTemplate(String templateName, Map<String, ?> dataModel, Email email);

    void sendUserInviteEmail(UserInvite invite, TdarUser from);

    void sendMimeMessage(String templateName, Map<String, ?> dataModel, Email email, List<File> attachments, List<File> inline);

    void queue(Email email);

    void send(Email email);

    String getFromEmail();

    MailSender getMailSender();

    void setMailSender(JavaMailSender mailSender);

    Email constructEmail(Person from, HasEmail to, Resource resource, String subject, String messageBody, EmailMessageType type);

    Email constructEmail(Person from, HasEmail to, Resource resource, String subjectSuffix, String messageBody, EmailMessageType type,
            Map<String, String[]> params);

    void changeEmailStatus(Status action, List<Email> emails);

    List<Email> findEmailsWithStatus(Status status);

    void proccessPermissionsRequest(TdarUser requestor, Resource resource, TdarUser authenticatedUser, String comment, boolean reject, EmailMessageType type,
            GeneralPermissions permission, Date expires);
}
