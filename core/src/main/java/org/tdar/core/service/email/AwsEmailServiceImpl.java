package org.tdar.core.service.email;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.bean.notification.aws.BasicAwsMessage;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.email.AwsEmailService;

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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Service
public class AwsEmailServiceImpl implements AwsEmailService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final TdarConfiguration config = TdarConfiguration.getInstance();

	@Autowired
	private FreemarkerService freemarkerService;

	private Regions awsRegion = Regions.US_WEST_2;
	
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
			messageHelper.addAttachment(file.getName(), file);
		}
		
		ClassPathResource logo =  new ClassPathResource("tdar-logo.png");
		messageHelper.addInline("logo", logo);
		
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
			message.setFrom(config.getDefaultFromEmail());
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
	public void renderAndUpdateEmailContent(AwsMessage message) {
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
		SendEmailRequest request = new SendEmailRequest()
				.withDestination(new Destination().withToAddresses(email.getTo()))
				.withMessage(new Message()
						.withBody(
								new Body().withHtml(new Content().withCharset("UTF-8").withData(email.getMessage())))
						.withSubject(new Content().withCharset("UTF-8").withData(email.getSubject())))
				.withSource(message.getEmail().getFrom());

		SendEmailResult response = getSesClient().sendEmail(request);
		return response;
	}
	
	/**
	 * Takes an AWS message, renders the Freemarker template to update the HTML body, renders the subject line,
	 * then creates an MIME version and sends it via AWS.
	 */
	@Override
	public SendRawEmailResult renderAndSendMessage(AwsMessage message) throws MessagingException, IOException{
		updateEmailSubject(message);
		renderAndUpdateEmailContent(message);
		MimeMessage mimeMessage = createMimeMessage(message);
		return sendMultiPartMessage(mimeMessage);
	}

	@Override
	public SendRawEmailResult sendMultiPartMessage(MimeMessage message) throws IOException, MessagingException  {
		SendRawEmailRequest request  	= new SendRawEmailRequest().withRawMessage(createRawMimeMessage(message));
		SendRawEmailResult  response 	= getSesClient().sendRawEmail(request);
		return response;
	}
	
	private RawMessage createRawMimeMessage(MimeMessage message) throws IOException, MessagingException {
		byte[] byteArray = getByteArray(message);
        return new RawMessage(ByteBuffer.wrap(byteArray));
	}

	@Override
	public byte[] getByteArray(MimeMessage message) throws IOException, MessagingException{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        message.writeTo(outputStream);
        return outputStream.toByteArray();
	}
	
	private BasicAWSCredentials getAwsCredentials(){
		return new BasicAWSCredentials(config.getAwsAccessKey(), config.getAwsSecretKey());
	}
	
	private AmazonSimpleEmailService getSesClient(){
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().
				withCredentials(new AWSStaticCredentialsProvider(getAwsCredentials())).
				withRegion(awsRegion).build();
		return client;
	}
	
	@Override
	public void setAwsRegion(Regions region){
		this.awsRegion = region;
	}


	/**
	 * This method is used in the job scheduler to convert a queued email object back to an AwsMessage object
	 * so that it can be sent as a mime message.
	 */
	@Override
	public AwsMessage convertEmailToAwsMessage(Email email) {
		// TODO Auto-generated method stub
		return null;
		
		
		
	}
}
