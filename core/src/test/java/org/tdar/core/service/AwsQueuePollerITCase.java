package org.tdar.core.service;

import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;

import com.amazonaws.services.sqs.model.Message;

public class AwsQueuePollerITCase extends AbstractIntegrationTestCase {
	
	@Autowired
	AwsQueuePollerService awsQueue;
	
	
	@Test
	public void testFetchMessages(){
		List<Message> messages = awsQueue.getBouncedMessages();
		getLogger().debug("There are : {} messages",messages.size());
		
		for(Message message : messages){
			getLogger().debug("Message is : {}",message);
			
			StringTokenizer token = new StringTokenizer(message.getBody());
			
			token.nextToken();
		}
	}
	
	
	@Test 
	public void generateUuid(){
		String uuid = UUID.randomUUID().toString();
		assert(uuid.length()==19);
	}
	
}
