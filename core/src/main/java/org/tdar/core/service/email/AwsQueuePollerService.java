package org.tdar.core.service.email;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.amazonaws.services.sqs.model.Message;

public interface AwsQueuePollerService {

	List<Message> getBouncedMessages();

	void removeMessageFromQueue(String messageReceiptHandle);

	String getTdarMessageId(JSONArray headers);

	JSONArray getMessageHeaders(Message message);

	String getNotificationType(Message message);

	String getBounce(Message message);

	JSONObject getMessageJson(Message message);

}