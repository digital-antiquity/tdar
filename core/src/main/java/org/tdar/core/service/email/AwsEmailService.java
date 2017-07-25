package org.tdar.core.service.email;

import org.tdar.core.bean.notification.Email;

import com.amazonaws.services.simpleemail.model.SendEmailResult;

public interface AwsEmailService {

	SendEmailResult sendMessage(Email message);
}
