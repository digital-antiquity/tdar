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

public class EmailRawMessageHelper {
	
	public static final String LOGO2 = "logo";
	public static final String X_TDAR_MESSAGE_ID = "x-tdar-message-id";
    public static final String TDAR_LOGO_PNG = "tdar-logo.png";

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

		ClassPathResource logo = new ClassPathResource(TDAR_LOGO_PNG);
		messageHelper.addInline(LOGO2, logo);

		mimeMessage = messageHelper.getMimeMessage();
		
		if(message.getMessageUuid()!=null && !message.getMessageUuid().equals("")){
			mimeMessage.addHeader(X_TDAR_MESSAGE_ID, message.getMessageUuid());
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
