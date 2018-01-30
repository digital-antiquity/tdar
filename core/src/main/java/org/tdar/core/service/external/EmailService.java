package org.tdar.core.service.external;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.tdar.core.bean.HasName;
import org.tdar.core.bean.entity.HasEmail;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.resource.Resource;

import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public interface EmailService {

	void queueAwsMessage(Email message);

	/*
	 * sends a message using a freemarker template instead of a string; templates are stored in src/main/resources/freemarker-templates
	 */
	void queueWithFreemarkerTemplate(String templateName, Map<String, ?> dataModel, Email email);

	void sendUserInviteEmail(UserInvite invite, TdarUser from);

	/**
	 * Sends an email message to the given recipients. If no recipients are passed in, defaults to TdarConfiguration.getSystemAdminEmail().
	 * 
	 * @param emailMessage
	 * @param subject
	 * @param recipients
	 *            set of String varargs
	 */
	void queue(Email email);

	/**
	 * Sends an email message to the given recipients. If no recipients are passed in, defaults to TdarConfiguration.getSystemAdminEmail().
	 * 
	 * @param emailMessage
	 * @param subject
	 * @param recipients
	 *            set of String varargs
	 */
	void send(Email email);

	Email constructEmail(Person from, HasEmail to, Resource resource, String subject, String messageBody,
			EmailType type);

	Email constructEmail(Person from, HasEmail to, Resource resource, String subjectSuffix, String messageBody,
			EmailType type, Map<String, String[]> params);

	void changeEmailStatus(Status action, List<Email> emails);

	List<Email> findEmailsWithStatus(Status status);

	void proccessPermissionsRequest(TdarUser requestor, Resource resource, TdarUser authenticatedUser, String comment,
			boolean reject, EmailType type, GeneralPermissions permission, Date expires);

	void sendUserInviteGrantedEmail(Map<TdarUser, List<HasName>> notices, TdarUser person);

	String getFromEmail();

	MimeMessage createMimeMessage(Email message) throws MessagingException;

	Email createMessage(EmailType emailType, String to);

	void renderAndUpdateEmailContent(Email message);

	void updateEmailSubject(Email message);

	byte[] getByteArray(MimeMessage message) throws IOException, MessagingException;

	SendRawEmailResult renderAndSendMessage(Email message) throws MessagingException, IOException;

	/**
	 * 
	 * @param message
	 */
	Email dequeueAwsMessage(Email message);
	
	Email dequeueAwsMessage(Long messageId);

	//Class<? extends AwsMessage> convertEmailToAwsMessage(Email email);

}