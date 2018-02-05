package org.tdar.core.service.external;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.tdar.core.bean.HasName;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.HasEmail;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.resource.Resource;

import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public interface EmailService {

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
	@Deprecated
	void send(Email email);

	/**
	 * Deprecated method. use construct email instead, since it generates HTML emails. 
	 */
	@Deprecated
	Email constructEmail(Person from, HasEmail to, Resource resource, String subject, String messageBody,
			EmailType type);
	/**
	 * Deprecated method. use construct email instead, since it generates HTML emails. 
	 */
	@Deprecated
	Email constructEmail(Person from, HasEmail to, Resource resource, String subjectSuffix, String messageBody,
			EmailType type, Map<String, String[]> params);

	void changeEmailStatus(Status action, List<Email> emails);

	List<Email> findEmailsWithStatus(Status status);

	void proccessPermissionsRequest(TdarUser requestor, Resource resource, TdarUser authenticatedUser, String comment,
			boolean reject, EmailType type, Permissions permission, Date expires);

	void sendUserInviteGrantedEmail(Map<TdarUser, List<HasName>> notices, TdarUser person);
	
	Email sendWelcomeEmail(Person person);
	
	String getFromEmail();


	/**
	 * Generates a new message 
	 * @param emailType
	 * @param to
	 * @return
	 */
	Email createMessage(EmailType emailType, String to);

	/**
	 * Forces the message to render the contents into the Freemarker template, then update the email message with the contents. 
	 * @param message
	 */
	void renderAndUpdateEmailContent(Email message);

	/**
	 * Forces the message to render the subject line with as determined by the message class's createEmailSubject() call. 
	 * @param message
	 */
	void updateEmailSubject(Email message);

	/**
	 * Updates the email content, creates the subject line, creates a Mime message, then sends it. 
	 * @param message
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	SendRawEmailResult renderAndSendMessage(Email message) throws MessagingException, IOException;

	Email dequeue(Email message);
	
	void generateAndSendUserStatisticEmail(TdarUser user, BillingAccount billingAccount);

	/**
	 * Creates a new statistics mesage and renders it
	 * @param user
	 * @param billingAccount
	 * @return
	 */
	Email generateUserStatisticsEmail(TdarUser user, BillingAccount billingAccount);

	/**
	 * Sends an email message though the AWS gateway. 
	 * @param message
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	SendRawEmailResult sendAwsHtmlMessage(Email message) throws MessagingException, IOException;

	void renderAndQueueMessage(Email email);
}