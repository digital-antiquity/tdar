package org.tdar.core.service.external;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

/**
 * $Id$
 *
 * Provides email capabilities.
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
public class EmailService {

    private final Logger logger = Logger.getLogger(getClass());

    private final Properties smtpProperties = new Properties();
    
    private final static String FROM_EMAIL_ADDRESS = "info@tdar.org";
    
    private Address fromAddress;
    
    private String smtpHost;
    
    public EmailService() {
    	this.smtpHost = TdarConfiguration.getInstance().getSmtpHost();
    	this.smtpProperties.put("mail.smtp.host", smtpHost);
	}
    
    /**
     * Sends an email message to the email associated with the given person.
     * @param emailMessage
     * @param person
     * @param subject
     */
    public void send(String emailMessage, Person person, String subject) {
        String email = person.getEmail();
        if (StringUtils.isBlank(email)) {
            logger.warn("Trying to send message to a person with no email: " +  person + " message: " + emailMessage);
            return;
        }
        send(emailMessage, person.getEmail(), subject);
    }

    /**
     * Sends an email message to the email associated with the given person.
     * @param emailMessage
     * @param person
     * @param subject
     */
    public void send(String emailMessage, String email, String subject) {
        try {
            Address toAddress = new InternetAddress(email);
            Address fromAddress = getFromAddress();
            Session session = getMailSession();
            Message message = new MimeMessage(session);
            message.setFrom(fromAddress);
            message.setSubject(subject);
            message.addRecipient(RecipientType.TO, toAddress);
            // FIXME: send HTML and plaintext email?
            // see http://java.sun.com/products/javamail/FAQ.html#sendmpa
            // for more info
            message.setContent(emailMessage, "text/plain");
            Transport.send(message);

        }
        catch (AddressException exception) {
            exception.printStackTrace();
            logger.error("Couldn't send mail to " + email, exception);
            throw new TdarRecoverableRuntimeException(exception.getMessage(), exception);
        }
        catch (MessagingException exception) {
            exception.printStackTrace();
            logger.error(exception);
            throw new TdarRecoverableRuntimeException(exception.getMessage(), exception);
        }
    }

    public synchronized Address getFromAddress() throws AddressException {
        if (fromAddress == null) {
            fromAddress = new InternetAddress(FROM_EMAIL_ADDRESS, true);
        }
        return fromAddress;
    }
    
    public Session getMailSession() {
        return Session.getDefaultInstance(smtpProperties);
    }
    
    public String getSmtpHost() {
		return smtpHost;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

}
