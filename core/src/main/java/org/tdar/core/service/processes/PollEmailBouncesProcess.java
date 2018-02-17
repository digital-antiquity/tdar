package org.tdar.core.service.processes;

import java.util.List;

import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.service.email.AwsQueuePollerService;
import org.tdar.core.service.external.EmailService;

import com.amazonaws.services.sqs.model.Message;


/**
 * Scheduled process to poll AWS SQS Queue for bouce notifications, and update the email with the messages. 
 * 
 * @author briancastellanos
 *
 */
@Component
@Scope("prototype")
public class PollEmailBouncesProcess extends AbstractScheduledProcess {

	private static final long serialVersionUID = 686514606029804834L;

	private final Logger logger = LoggerFactory.getLogger(getClass());
    
	@Autowired
	private EmailService emailService;
	
    @Autowired
    private AwsQueuePollerService awsQueuePoller;

    @Autowired
    @Qualifier("genericDao")
    protected GenericDao genericDao;


    @Override
    public String getDisplayName() {
        return "Process Email Bounces";
    }


    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

	@Override
	public void execute() {
		List<Message> messages = awsQueuePoller.getBouncedMessages();
		
		for(Message message : messages){
			JSONArray headers 	= awsQueuePoller.getMessageHeaders(message);
			String messageId 	= awsQueuePoller.getTdarMessageId(headers);
			String errorMessage = awsQueuePoller.getBounce(message);
			emailService.markMessageAsBounced(messageId, errorMessage);
		}
	}

	@Override
	public boolean isCompleted() {
		return true;
	}
	
	
	public AwsQueuePollerService getAwsQueuePoller() {
		return awsQueuePoller;
	}

	public void setAwsQueuePoller(AwsQueuePollerService awsQueuePoller) {
		this.awsQueuePoller = awsQueuePoller;
	}
	
	public EmailService getEmailService() {
		return emailService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
}
