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
	public SendEmailResult sendMessage(AwsMessage message) {
		logger.debug("sendMessage() message={}", message);

		Email 		email 		= message.getEmail();
		Destination toEmail 	= new Destination().withToAddresses(email.getTo());
		String 		fromEmail 	= message.getEmail().getFrom();

		Body body = new Body().withHtml(createContent(email.getMessage()));
		
		Message message1 = new Message();
		message1.withBody(body);
		message1.withSubject(createContent(email.getSubject()));
		
		SendEmailRequest request = new SendEmailRequest();
		request.withDestination(toEmail);
		request.withMessage(message1);
		request.withSource(fromEmail);

		SendEmailResult response = getSesClient().sendEmail(request);
		
		return response;
	}


	@Override
	public SendRawEmailResult sendMultiPartMessage(RawMessage message) throws IOException, MessagingException  {
		SendRawEmailRequest request  	= new SendRawEmailRequest().withRawMessage(message);
		SendRawEmailResult  response 	= getSesClient().sendRawEmail(request);
		return response;
	}
	
		private Content createContent(String content){
		String characterSet = TdarConfiguration.getInstance().getCharacterSet();
		return new Content().withCharset(characterSet).withData(content);
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
