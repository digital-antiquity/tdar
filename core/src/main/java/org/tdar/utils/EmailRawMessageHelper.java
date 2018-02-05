package org.tdar.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.notification.Email;

import com.amazonaws.services.simpleemail.model.RawMessage;

@Component
public class EmailRawMessageHelper {
	
	public MimeMessage createMimeMessage(Email message) throws MessagingException {
		Session session = Session.getInstance(new Properties());
		MimeMessage mimeMessage = new MimeMessage(session);

		MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);

		messageHelper.setTo(message.getTo());
		messageHelper.setFrom(message.getFrom());
		messageHelper.setSubject(message.getSubject());
		messageHelper.setText(message.getMessage(), true);

		for (File file : message.getAttachments()) {
			messageHelper.addAttachment(file.getName(), file);
		}

		for (String contentId : message.getInlineAttachments().keySet()) {
			File file = message.getInlineAttachments().get(contentId);
			messageHelper.addInline(contentId, file);
		}

		ClassPathResource logo = new ClassPathResource("tdar-logo.png");
		messageHelper.addInline("logo", logo);

		mimeMessage = messageHelper.getMimeMessage();
		
		if(message.getMessageUuid()!=null && !message.getMessageUuid().equals("")){
			mimeMessage.addHeader("x-tdar-message-id", message.getMessageUuid());
		}
		
		return mimeMessage;
	}
	
	public RawMessage createRawMessage(MimeMessage message) throws IOException, MessagingException {
		byte[] byteArray = getByteArray(message);
		return new RawMessage(ByteBuffer.wrap(byteArray));
	}

	public byte[] getByteArray(MimeMessage message) throws IOException, MessagingException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		message.writeTo(outputStream);
		return outputStream.toByteArray();
	}
	
	
}
