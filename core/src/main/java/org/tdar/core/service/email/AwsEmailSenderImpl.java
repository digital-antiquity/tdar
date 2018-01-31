package org.tdar.core.service.email;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.configuration.TdarConfiguration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
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
/**
 * This is the transport service for sending messages through Amazon web services. 
 * This assumes the messgae has already been constructed and rendered within the message. 
 * @author briancastellanos
 *
 */
public class AwsEmailSenderImpl implements AwsEmailSender {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final TdarConfiguration config = TdarConfiguration.getInstance();

	private Regions awsRegion = Regions.US_WEST_2;

	@Override
	public SendEmailResult sendMessage(Email awsMessage) {
		logger.debug("sendMessage() message={}", awsMessage);

		Destination toEmail = new Destination().withToAddresses(awsMessage.getTo());
		String fromEmail = awsMessage.getFrom();

		Body body = new Body().withHtml(createContent(awsMessage.getMessage()));

		Message message = new Message();
		message.withBody(body);
		message.withSubject(createContent(awsMessage.getSubject()));

		SendEmailRequest request = new SendEmailRequest();
		request.withDestination(toEmail);
		request.withMessage(message);
		request.withSource(fromEmail);

		SendEmailResult response = getSesClient().sendEmail(request);
		return response;
	}
	

	@Override
	public void setAwsRegion(Regions region) {
		this.awsRegion = region;
	}
	
	private Content createContent(String content) {
		String characterSet = TdarConfiguration.getInstance().getCharacterSet();
		return new Content().withCharset(characterSet).withData(content);
	}

	private BasicAWSCredentials getAwsCredentials() {
		return new BasicAWSCredentials(config.getAwsAccessKey(), config.getAwsSecretKey());
	}

	private AmazonSimpleEmailService getSesClient() {
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(getAwsCredentials())).withRegion(awsRegion).build();
		return client;
	}

	@Override
	public SendRawEmailResult sendMultiPartMessage(Email email) throws IOException, MessagingException {
		MimeMessage mimeMessage = createMimeMessage(email);
		RawMessage rawMessage = createRawMessage(mimeMessage);
		return sendRawMessage(rawMessage);
	}
	
	private MimeMessage createMimeMessage(Email message) throws MessagingException {
		Session session = Session.getInstance(new Properties());
		MimeMessage mimeMessage = new MimeMessage(session);

		MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

		messageHelper.setTo(message.getTo());
		messageHelper.setFrom(message.getFrom());
		messageHelper.setSubject(message.getSubject());
		messageHelper.setText(message.getMessage(), true);

		for (File file : message.getAttachments()) {
			messageHelper.addAttachment(file.getName(), file);
		}

		for (String contentId : message.getInlineAttachments().keySet()) {
			File file = message.getInlineAttachments().get(contentId);
			messageHelper.addInline(contentId, file);
		}

		ClassPathResource logo = new ClassPathResource("tdar-logo.png");
		messageHelper.addInline("logo", logo);

		mimeMessage = messageHelper.getMimeMessage();
		if(message.getMessageUuid()!=null && !message.getMessageUuid().equals("")){
			mimeMessage.addHeader("x-tdar-message-id", message.getMessageUuid());
		}
		
		return mimeMessage;
	}
	
	private RawMessage createRawMessage(MimeMessage message) throws IOException, MessagingException {
		byte[] byteArray = getByteArray(message);
		return new RawMessage(ByteBuffer.wrap(byteArray));
	}

	private byte[] getByteArray(MimeMessage message) throws IOException, MessagingException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		message.writeTo(outputStream);
		return outputStream.toByteArray();
	}
	
	private SendRawEmailResult sendRawMessage(RawMessage message) throws IOException, MessagingException {
		SendRawEmailRequest request = new SendRawEmailRequest().withRawMessage(message);
		SendRawEmailResult response = getSesClient().sendRawEmail(request);
		return response;
	}

	

}
