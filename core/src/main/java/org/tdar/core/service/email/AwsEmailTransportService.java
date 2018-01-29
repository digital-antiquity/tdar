package org.tdar.core.service.email;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.aws.AwsMessage;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public interface AwsEmailTransportService {
	SendEmailResult sendMessage(AwsMessage message);
	SendRawEmailResult sendMultiPartMessage(RawMessage message) throws IOException, MessagingException;
	void setAwsRegion(Regions region);
}
