package org.tdar.core.service;

import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.service.email.AwsQueuePollerService;

import com.amazonaws.services.sqs.model.Message;

public class AwsQueuePollerITCase extends AbstractIntegrationTestCase {
	
	@Autowired
	AwsQueuePollerService awsQueue;
	
	 
	@Test
	public void testFetchMessages(){
		List<Message> messages = awsQueue.getBouncedMessages();
		getLogger().debug("There are : {} messages",messages.size());
		
		for(Message message : messages){
			JSONArray headers = awsQueue.getMessageHeaders(message);
			String messageId = awsQueue.getTdarMessageId(headers);
			
			getLogger().debug("This is the object: {}",			headers);
			
		}
	}
	
}
