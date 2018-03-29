package org.tdar.core.service.email;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.configuration.TdarConfiguration;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public class MockAwsEmailSenderServiceImpl implements AwsEmailSender {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Regions awsRegion;
    private static final TdarConfiguration config = TdarConfiguration.getInstance();
	private List<Email> messages = new ArrayList<Email>();
	
	@Override
	public SendEmailResult sendMessage(Email message) {
		logger.debug("Mock object sending message message={}", message);
		logger.debug("sending this message: \n{}", message);
        messages.add(message);
        return new SendEmailResult();
	}

	@Override
	public void setAwsRegion(Regions region){
		this.awsRegion = region;
	}
	
	@Override
	public SendRawEmailResult sendMultiPartMessage(Email email) throws IOException, MessagingException {
		logger.debug("Mock object sending multipart message message={}", email);
		messages.add(email);
        return new SendRawEmailResult();
	}
	
	public List<Email> getMessages() {
        return messages;
	}
}
