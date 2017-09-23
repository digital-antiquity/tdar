package org.tdar.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.tdar.core.configuration.TdarConfiguration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;

@Service
public class AwsQueuePollerService {
	private static final TdarConfiguration config = TdarConfiguration.getInstance();
	private Regions awsRegion = Regions.US_WEST_2;

	public List<Message> getBouncedMessages() {
		AmazonSQS sqs = getSqsClient();
		
		List<Message> messages = sqs.receiveMessage(config.getAwsQueueName()).getMessages();
		return messages;
	}
	
	public void removeMessageFromQueue(String messageReceiptHandle){
		AmazonSQS sqs = getSqsClient();

		sqs.deleteMessage(new DeleteMessageRequest()
		    .withQueueUrl(config.getAwsQueueName())
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

}
