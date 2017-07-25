package org.tdar.core.service.email;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.service.email.AwsEmailService;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;

@Service
public class AwsEmailServiceImpl implements AwsEmailService {
	
	@Override
	public SendEmailResult sendMessage(Email message) {
		
		AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
        .withRegion(Regions.US_EAST_1).build();
		
		SendEmailRequest request = new SendEmailRequest()
		        .withDestination(new Destination()
		                        .withToAddresses(message.getTo()))
		        .withMessage(new Message().withBody(new Body().withHtml(
                             new Content().withCharset("UTF-8")
                         	.withData(message.getMessage())))
                .withSubject(new Content().withCharset("UTF-8")
                			.withData(message.getSubject())))
		        // .withReplyToAddresses().withReturnPath("")
		        //.withReturnPathArn("").
		        .withSource("brian@thinkkreative.com");
		        //.withSourceArn("");
		SendEmailResult response = client.sendEmail(request);
		return response;
	}

}
