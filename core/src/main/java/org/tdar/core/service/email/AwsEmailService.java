package org.tdar.core.service.email;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.aws.AwsMessage;

import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public interface AwsEmailService {

	SendEmailResult sendMessage(AwsMessage message);

	AwsMessage createMessage(EmailType emailType,  String to);
	
	void updateEmailContent(AwsMessage message) ;

	MimeMessage createMimeMessage(AwsMessage message) throws MessagingException;

	SendRawEmailResult sendMultiPartMessage(MimeMessage message) throws IOException, MessagingException;

	void updateEmailSubject(AwsMessage message);
}
