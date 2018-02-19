package org.tdar.core.service;

import static org.junit.Assert.*;

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
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.dao.EmailDao;
import org.tdar.core.service.email.AwsQueuePollerService;
import org.tdar.core.service.email.MockAwsEmailSenderServiceImpl;
import org.tdar.core.service.email.MockAwsQueuePollerServiceImpl;

import com.amazonaws.services.sqs.model.Message;

public class AwsQueuePollerITCase extends AbstractIntegrationTestCase {
	
	@Autowired
	AwsQueuePollerService awsQueue;
	
	@Autowired
	EmailDao emailDao;
	
	
	@Test
	public void testUsingMockObject(){
		assertTrue("The mock object is being used", awsQueue instanceof MockAwsQueuePollerServiceImpl);
		assertTrue("The email sending is being mocked", emailService.getAwsEmailService() instanceof MockAwsEmailSenderServiceImpl);
	}
	
	@Test
	@Rollback(true)
	public void testFetchMessages(){
		
		Email email1 = createBounceMesage();
		Email email2 = createBounceMesage();
		Email email3 = createBounceMesage();
		
		emailService.renderAndQueueMessage(email1);
		emailService.renderAndQueueMessage(email2);
		emailService.renderAndQueueMessage(email3);
		
		sendEmailProcess.execute();
		
		List<Message> messages = awsQueue.getBouncedMessages();
		getLogger().debug("There are : {} messages",messages.size());
		
		for(Message message : messages){
			JSONArray headers = awsQueue.getMessageHeaders(message);
			String messageId = awsQueue.getTdarMessageId(headers);
			String errorMessage = awsQueue.getBounce(message);
			getLogger().debug("The header is {}", messageId);
			getLogger().debug("The bounce is {}", errorMessage);
			
			emailService.markMessageAsBounced(messageId, errorMessage);
			
			genericService.synchronize();
			Email find = null;
			find = emailDao.findEmailByGuid(messageId).get(0);
			
			assertEquals(Status.BOUNCED,find.getStatus());
			assertEquals(errorMessage, find.getErrorMessage());
		}
	}
	
	
	public Email createBounceMesage(){
		Email email = emailService.createMessage(EmailType.TEST_EMAIL, "bounce@simulator.amazonses.com");
		email.setSubject("Test Bounced Emails");
		email.setMessage("This is a test of the bounced email response.");
		email.addData("foo", "foo");
		email.addData("bar", "bar");
		email.addData("firstName", "Brian");
		email.addData("lastName", "Castellanos");
		return email;
	}
	
}
