package org.tdar.core.service.email;


import java.io.IOException;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.configuration.TdarConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public class MockAwsEmailServiceImpl implements AwsEmailService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Regions awsRegion;
    private static final TdarConfiguration config = TdarConfiguration.getInstance();

	
	@Override
	public SendEmailResult sendMessage(AwsMessage message) {
		logger.debug("Mock object sending message message={}", message);
		return new SendEmailResult();
	}


	@Override
	public SendRawEmailResult sendMultiPartMessage(RawMessage message) throws IOException, MessagingException  {
		logger.debug("Mock object sending multipart message message={}", message);
		return null;
	}
	
	@Override
	public void setAwsRegion(Regions region){
		this.awsRegion = region;
	}

	/**
	 * This method is used in the job scheduler to convert a queued email object back to an AwsMessage object
	 * so that it can be sent as a mime message.
	 */
	@Override
	public AwsMessage convertEmailToAwsMessage(Email email) {
		// TODO Auto-generated method stub
		return null;
		
	}
}
