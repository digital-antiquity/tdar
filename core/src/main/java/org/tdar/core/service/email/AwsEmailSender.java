package org.tdar.core.service.email;

import java.io.IOException;

import javax.mail.MessagingException;

import org.tdar.core.bean.notification.Email;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public interface AwsEmailSender {
	SendEmailResult sendMessage(Email message);
	SendRawEmailResult sendMultiPartMessage(RawMessage message) throws IOException, MessagingException;
	void setAwsRegion(Regions region);
}
