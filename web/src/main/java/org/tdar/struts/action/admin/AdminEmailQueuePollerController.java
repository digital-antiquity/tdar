package org.tdar.struts.action.admin;


import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.service.email.AwsQueuePollerService;
import org.tdar.core.service.external.EmailService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.Pair;

import com.amazonaws.services.sqs.model.Message;
import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/admin")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class AdminEmailQueuePollerController extends AbstractAuthenticatableAction implements Preparable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2036850880577174539L;

	@Autowired 
	private AwsQueuePollerService awsQueueService;
	
	
	@Autowired
	private EmailService emailService;
	
	@Override
	public void prepare() throws Exception {
		// TODO Auto-generated method stub
	}
	
	private List<Pair<String, String>> allMessages;
	private List<Pair<String, String>> bouncedMessages;
	
	@WriteableSession
	@Action(value = "pollMessageQueue", results = {@Result(name=SUCCESS, location="queuePollResult.ftl")})
	@PostOnly
	public String execute(){
		bouncedMessages = new ArrayList<Pair<String, String>>();
		allMessages	    = new ArrayList<Pair<String, String>>();
		
		List<Message> messages = awsQueueService.getBouncedMessages();
		
		getLogger().debug("There were {} messages", messages.size());
		getLogger().debug("{}",messages);
		for(Message message : messages){
			JSONArray headers 	= awsQueueService.getMessageHeaders(message);
			String messageId 	= awsQueueService.getTdarMessageId(headers);
			String errorMessage = awsQueueService.getBounce(message);
			
			if(awsQueueService.getNotificationType(message).equals("Bounce")){
				emailService.markMessageAsBounced(messageId, errorMessage);
				bouncedMessages.add(new Pair<String, String>(errorMessage, awsQueueService.getMessageJson(message).toJSONString()));
			}
			allMessages.add(new Pair<String, String>(errorMessage, awsQueueService.getMessageJson(message).toJSONString()));
		}
		return SUCCESS;
	}



	public void setAwsQueue(AwsQueuePollerService awsQueue) {
		this.awsQueueService = awsQueue;
	}



	public List<Pair<String, String>> getAllMessages() {
		return allMessages;
	}



	public void setAllMessages(List<Pair<String, String>> messages) {
		this.allMessages = messages;
	}



	public List<Pair<String, String>> getBouncedMessages() {
		return bouncedMessages;
	}



	public void setBouncedMessages(List<Pair<String, String>> bouncedMessages) {
		this.bouncedMessages = bouncedMessages;
	}
	

}
