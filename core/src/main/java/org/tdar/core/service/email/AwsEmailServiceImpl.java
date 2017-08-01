package org.tdar.core.service.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.bean.notification.aws.BasicAwsMessage;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.email.AwsEmailService;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

@Service
public class AwsEmailServiceImpl implements AwsEmailService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

	@Autowired
	private FreemarkerService freemarkerService;

	
	@Override
	public MimeMessage createMimeMessage(AwsMessage message) throws MessagingException {
		Session session = Session.getInstance(new Properties());
		MimeMessage mimeMessage = new MimeMessage(session);

		MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

		messageHelper.setTo(message.getEmail().getTo());
		messageHelper.setFrom(message.getEmail().getFrom());
		messageHelper.setSubject(message.getEmail().getSubject());
		messageHelper.setText(message.getEmail().getMessage(), true);

		for(File file : message.getAttachments()){
			FileSystemResource attachment = new FileSystemResource(file);
			messageHelper.addAttachment(file.getName(), file);
		}
		
		
		return messageHelper.getMimeMessage();
	}

	/**
	 * 
	 * 
	 * @param EmailType
	 *            the type of email to be created
	 * @return AwsEmail a new instance
	 */
	@Override
	public AwsMessage createMessage(EmailType emailType, String to) {
		Email message = new Email();
		
		if(emailType.getFromAddress()==null){
			message.setFrom(CONFIG.getDefaultFromEmail());
		}
		else {
			message.setFrom(emailType.getFromAddress());
		}
		
		message.setSubject(emailType.getSubject());
		message.setTo(to);
		AwsMessage awsEmail = null;
		if(emailType.getEmailClass()!=null){
			try {
				awsEmail = emailType.getEmailClass().newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		else {
			awsEmail = new BasicAwsMessage();
		}
		
		awsEmail.setEmailType(emailType);
		awsEmail.setEmail(message);

		return awsEmail;
	}

	/**
	 * Renders the template for the email and stores the content within the
	 * email bean
	 */
	@Override
	public void updateEmailContent(AwsMessage message) {
		String templateName = null;
		try {
			templateName = message.getEmailType().getTemplateLocation();
			String content = freemarkerService.render(templateName, message.getMap());
			message.getEmail().setMessage(content);
		}
		catch(IOException e) {
            logger.error("Email template file not found (" + templateName + ")", e);
		}
	}
	
	/**
	 * Forces the message to re-render dynamically created subject line.
	 */
	@Override
	public void updateEmailSubject(AwsMessage message){
		message.getEmail().setSubject(message.createSubjectLine());
	}
	
	@Override
	public SendEmailResult sendMessage(AwsMessage message) {
		logger.debug("sendMessage() message={}", message);

		Email email = message.getEmail();
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.build();

		SendEmailRequest request = new SendEmailRequest()
				.withDestination(new Destination().withToAddresses(email.getTo()))
				.withMessage(new Message()
						.withBody(
								new Body().withHtml(new Content().withCharset("UTF-8").withData(email.getMessage())))
						.withSubject(new Content().withCharset("UTF-8").withData(email.getSubject())))
				.withSource(message.getEmail().getFrom());

		SendEmailResult response = client.sendEmail(request);
		return response;
	}

	@Override
	public SendRawEmailResult sendMultiPartMessage(MimeMessage message) throws IOException, MessagingException {
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);

        RawMessage 			rawMessage 	= new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
		SendRawEmailRequest request  	= new SendRawEmailRequest().withRawMessage(rawMessage);
		SendRawEmailResult  response 	= client.sendRawEmail(request);
		
		return response;
	}

}
