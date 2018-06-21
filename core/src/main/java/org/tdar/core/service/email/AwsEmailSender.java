package org.tdar.core.service.email;

import java.io.IOException;

import javax.mail.MessagingException;

import org.tdar.core.bean.notification.Email;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public interface AwsEmailSender {
    /**
     * Used to send an HTML email with no attachments. Use sendMultiPartEmail to send attachments.
     * 
     * @param message
     * @return
     */
    SendEmailResult sendMessage(Email message);

    /**
     * Sends an email with MIME boundaries and support for attachments.
     * 
     * @param email
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    SendRawEmailResult sendMultiPartMessage(Email email) throws IOException, MessagingException;

//    void setAwsRegion(Regions region);
}
