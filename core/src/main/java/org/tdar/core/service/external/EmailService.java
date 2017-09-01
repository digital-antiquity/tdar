package org.tdar.core.service.external;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.entity.HasEmail;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.EmailMessageType;

public interface EmailService {

	void queueAwsMessage(AwsMessage message);

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
			EmailMessageType type);

	Email constructEmail(Person from, HasEmail to, Resource resource, String subjectSuffix, String messageBody,
			EmailMessageType type, Map<String, String[]> params);

	void changeEmailStatus(Status action, List<Email> emails);

	List<Email> findEmailsWithStatus(Status status);

	void proccessPermissionsRequest(TdarUser requestor, Resource resource, TdarUser authenticatedUser, String comment,
			boolean reject, EmailMessageType type, GeneralPermissions permission, Date expires);

	void sendUserInviteGrantedEmail(Map<TdarUser, List<HasName>> notices, TdarUser person);

	String getFromEmail();

}