package org.tdar.core.bean.notification;

import org.tdar.core.bean.notification.aws.AccessRequestCustomMessage;
import org.tdar.core.bean.notification.aws.AccessRequestGrantedMessage;
import org.tdar.core.bean.notification.aws.AccessRequestRejectedMessage;
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.bean.notification.aws.InviteAcceptedMessage;
import org.tdar.core.bean.notification.aws.InviteMessage;
import org.tdar.core.bean.notification.aws.TestAwsMessage;

public enum EmailType {
	INVITE("invite/invite.ftl","test@tdar.org",null,InviteMessage.class),
	INVITE_ACCEPTED("invite/invite-accepted.ftl","test@tdar.org",null,InviteAcceptedMessage.class),
	NEW_USER_NOTIFY("email_new_users.ftl"),
	NEW_USER_WELCOME("email-welcome.ftl"),
	TRANSACTION_COMPLETE_ADMIN("transaction-complete-admin.ftl"),
	
	PERMISSION_REQUEST_ACCEPTED("email-form/access-request-granted.ftl","test@tdar.org",null,AccessRequestGrantedMessage.class),
	PERMISSION_REQUEST_REJECTED("email-form/access-request-rejected.ftl","test@tdar.org",null,AccessRequestRejectedMessage.class),
	PERMISSION_REQUEST_CUSTOM("email-form/custom-accept.ftl","test@tdar.org",null,AccessRequestCustomMessage.class),
	
	OVERDRAWN_NOTIFICATION("overdrawn-user.ftl"),
	RESOURCE_EXPORT("resource-export-email.ftl"),
	
	ADMIN_NOTIFICATION("auth-report.ftl"),
	ADMIN_NEW_USER_REPORT("email_new_users.ftl"),
	ADMIN_QUARANTINE_REVIEW("email_review_message.ftl"),
	ADMIN_EMBARGO_EXPIRE("embargo/expiration-admin.ftl"),
	ADMIN_OVERDRAWN_NOTIFICATION("overdrawn-admin.ftl"),
	
	TEST_EMAIL("test-email.ftl", "test@tdar.org", "This is a test email %2$s %1$s",TestAwsMessage.class);
	
	/**
	 * a string representation of the .ftl template to use
	 */
	private final String templateLocation; 
	/**
	 * The sending address to populate on the email
	 */
	private String fromAddress;
	/**
	 * The subject line to populate on the email
	 */
	private String subject;
	
	private Class<? extends AwsMessage> emailClass;
	
	private EmailType(String template){
		this.templateLocation = template;
	}
	
	private EmailType(String template, String fromAddress){
		this(template);
		this.setFromAddress(fromAddress);
	}
	
	private EmailType(String template, String fromAddress, String subject) {
		this(template, fromAddress);
		this.setSubject(subject);
    }
	
	private EmailType(String template,  String fromAddress, String subject, Class<? extends AwsMessage> emailClass) {
		this(template, fromAddress, subject);
		this.setEmailClass(emailClass);
    }

	public String getTemplateLocation() {
		return templateLocation;
	}

	public Class<? extends AwsMessage> getEmailClass() {
		return emailClass;
	}

	public void setEmailClass(Class<? extends AwsMessage> emailClass) {
		this.emailClass = emailClass;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	/**
	 * Returns the subject line text or the formatter pattern to replace. 
	 * @return the subject line
	 */
	public String getSubject() {
		return subject==null?"":subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
