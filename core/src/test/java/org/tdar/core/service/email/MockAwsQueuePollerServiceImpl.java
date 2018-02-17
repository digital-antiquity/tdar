package org.tdar.core.service.email;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.EmailDao;
import org.tdar.utils.EmailRawMessageHelper;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;

@Service
public class MockAwsQueuePollerServiceImpl implements AwsQueuePollerService {
	private static final TdarConfiguration config = TdarConfiguration.getInstance();
	private Regions awsRegion = Regions.US_WEST_2;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	EmailDao emailDao;
	
	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getBouncedMessages()
	 */
	@Override
	public List<Message> getBouncedMessages() {
		logger.debug("Mocking getting messages");
		List<Message> messages =  new ArrayList<Message>();
		for(Email email : emailDao.getTestBounceBackEmails()){
			messages.add(getMessage(email));
		}
		return messages;
	}



	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#removeMessageFromQueue(java.lang.String)
	 */
	@Override
	public void removeMessageFromQueue(String messageReceiptHandle) {
		
	}

	private BasicAWSCredentials getAwsCredentials() {
		return new BasicAWSCredentials(config.getAwsAccessKey(), config.getAwsSecretKey());
	}

	private AmazonSQS getSqsClient() {
		AmazonSQS client = AmazonSQSClientBuilder.standard()
				.withCredentials(new AWSStaticCredentialsProvider(getAwsCredentials())).withRegion(awsRegion).build();
		return client;

	}
	
	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getTdarMessageId(org.json.simple.JSONArray)
	 */
	@Override
	public String getTdarMessageId(JSONArray headers) {
		String tdarMessageId = null;

		if(headers == null){
			return null;
		}
		
		int size = headers.size();
		for (int i = 0; i < size; i++) {
			JSONObject header = (JSONObject) headers.get(i);
			if (header.get("name").equals(EmailRawMessageHelper.X_TDAR_MESSAGE_ID)) {
				tdarMessageId = (String) header.get("value");
				break;
			}
		}
		return tdarMessageId;
	}
	
	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getMessageHeaders(com.amazonaws.services.sqs.model.Message)
	 */
	@Override
	public JSONArray getMessageHeaders(Message message) {
		JSONArray json  = null;
		try {
			json = (JSONArray) new JSONParser().parse(message.getBody());
		} catch (ParseException e) {
			logger.debug("Couldn't parse the message headers: {} {}",e,e);
		}
		return json;
	}
	
	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getNotificationType(com.amazonaws.services.sqs.model.Message)
	 */
	@Override
	public String getNotificationType(Message message){
		return "Bounce";
	}

	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getBounce(com.amazonaws.services.sqs.model.Message)
	 */
	@Override
	public String getBounce(Message message){
		return "{\"bounceType\":\"Permanent\",\"bounceSubType\":\"General\",\"bouncedRecipients\":[{\"emailAddress\":\"bounce@simulator.amazonses.com\",\"action\":\"failed\",\"status\":\"5.1.1\",\"diagnosticCode\":\"smtp; 550 5.1.1 user unknown\"}],\"timestamp\":\"2018-02-16T21:29:30.231Z\",\"feedbackId\":\"01010161a085ab48-f70f929e-f80c-4c04-a591-06733725ca40-000000\",\"remoteMtaIp\":\"207.171.163.188\",\"reportingMTA\":\"dsn; a27-10.smtp-out.us-west-2.amazonses.com\"}";
	}
	
	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getMessageJson(com.amazonaws.services.sqs.model.Message)
	 */
	@Override
	public JSONObject getMessageJson(Message message){
		JSONObject msg = new JSONObject();
		return msg;
	}
	
	private Message getMessage(Email email){
		Message message = new Message();
		message.setBody("[{\"name\":\""+EmailRawMessageHelper.X_TDAR_MESSAGE_ID+"\",\"value\":\""+email.getMessageUuid()+"\"}]");
		return message;
	}
	

}
