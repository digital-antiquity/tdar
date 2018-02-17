package org.tdar.core.service.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.EmailRawMessageHelper;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

@Service
public class AwsQueuePollerServiceImpl implements AwsQueuePollerService {
	private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final TdarConfiguration config = TdarConfiguration.getInstance();
	private Regions awsRegion = Regions.US_WEST_2;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	
	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getBouncedMessages()
	 */
	@Override
	public List<Message> getBouncedMessages() {
		Map<String, Message> allMessages = new HashMap<String, Message>();
		//List<Message> allMessages = new ArrayList<Message>();

		try {
			logger.debug("Starting getting messages");
			AmazonSQS sqs = getSqsClient();
			String queueName = config.getAwsQueueName();
			logger.debug("Queue name is {}",queueName);
			logger.debug("Getting queue URL");
			String queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
			logger.debug("Queue URL is {}",queueUrl);
			ReceiveMessageRequest rmr = new ReceiveMessageRequest(queueUrl);
			logger.debug("Receiving messages from {}", queueName);
			rmr.setMaxNumberOfMessages(10);
			rmr.setWaitTimeSeconds(20);
			boolean hasMessages = true;

			while (hasMessages) {
				List<Message> messages = sqs.receiveMessage(rmr).getMessages();
				logger.debug("retrived {} messages", messages.size());

				if (messages.size() == 0) {
					hasMessages = false;
				} 
				else {
					for (Message message : messages) {
						logger.debug("{}",message);
						
						removeMessageFromQueue(message.getReceiptHandle());
						allMessages.put(message.getMessageId(), message);
					}
				}
			}
		} 
		catch (AmazonServiceException ase) {
			ase.printStackTrace();
		} 
		catch (AmazonClientException ace) {
			ace.printStackTrace();
		} 
		
		return new ArrayList<Message>(allMessages.values());
	}



	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#removeMessageFromQueue(java.lang.String)
	 */
	@Override
	public void removeMessageFromQueue(String messageReceiptHandle) {
		AmazonSQS sqs = getSqsClient();

		sqs.deleteMessage(new DeleteMessageRequest().withQueueUrl(config.getAwsQueueName())
				.withReceiptHandle(messageReceiptHandle));
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
			if (header.get(NAME).equals(EmailRawMessageHelper.X_TDAR_MESSAGE_ID)) {
				tdarMessageId = (String) header.get(VALUE);
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
		JSONArray headers = null;
		JSONObject msg = getMessageJson(message);
		JSONObject mail = (JSONObject) msg.get("mail");
		headers = (JSONArray) mail.get("headers");
		return headers;
	}
	
	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getNotificationType(com.amazonaws.services.sqs.model.Message)
	 */
	@Override
	public String getNotificationType(Message message){
		return (String) getMessageJson(message).get("notificationType");
	}

	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getBounce(com.amazonaws.services.sqs.model.Message)
	 */
	@Override
	public String getBounce(Message message){
		return ((JSONObject) getMessageJson(message).get("bounce")).toJSONString();
	}
	
	/* (non-Javadoc)
	 * @see org.tdar.core.service.email.AwsQueuePollerService#getMessageJson(com.amazonaws.services.sqs.model.Message)
	 */
	@Override
	public JSONObject getMessageJson(Message message){
		JSONObject msg = null;
		try {
			JSONObject json;
			json = (JSONObject) new JSONParser().parse(message.getBody());
			msg = (JSONObject) new JSONParser().parse((String) json.get("Message"));
		} catch (ParseException e) {
			logger.debug("Couldn't parse the message headers: {} {}",e,e);
		}
		return msg;
	}
	
	
	

}
