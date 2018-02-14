package org.tdar.core.service;

import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.mail.MessagingException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.service.email.AwsQueuePollerService;

import com.amazonaws.services.sqs.model.Message;

public class AwsQueuePollerITCase extends AbstractIntegrationTestCase {
	
	@Autowired
	AwsQueuePollerService awsQueue;
	
	
	@Test
	public void testFetchMessages(){
		
		Email email = emailService.createMessage(EmailType.TEST_EMAIL, "bounce@simulator.amazonses.com");
		email.setSubject("Test Bounced Emails");
		email.setMessage("This is a test of the bounced email response.");
		email.addData("foo", "foo");
		email.addData("bar", "bar");
		email.addData("firstName", "Brian");
		email.addData("lastName", "Castellanos");

		try {
			emailService.renderAndSendMessage(email);
			emailService.renderAndSendMessage(email);
			emailService.renderAndSendMessage(email);
		} catch (MessagingException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Message> messages = awsQueue.getBouncedMessages();
		getLogger().debug("There are : {} messages",messages.size());
		
		for(Message message : messages){
			JSONArray headers = awsQueue.getMessageHeaders(message);
			String messageId = awsQueue.getTdarMessageId(headers);
			
			getLogger().debug("This is the object: {}",			headers);
			
		}
	}
	
}
