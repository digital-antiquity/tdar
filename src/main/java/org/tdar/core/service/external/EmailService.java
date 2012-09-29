package org.tdar.core.service.external;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import freemarker.template.Configuration;

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

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String FROM_EMAIL_NAME = "info@";

    @Autowired
    private MailSender mailSender;

    @Autowired
    private Configuration freemarkerConfiguration;
    
    public void sendTemplate(String templateName, Object dataModel, String subject, String ... recipients) {
        send(render(templateName, dataModel), subject, recipients);
    }

    /**
     * Sends an email message to the given recipients.  If no recipients are passed in, defaults to TdarConfiguration.getSystemAdminEmail().
     * @param emailMessage
     * @param subject
     * @param recipients set of String varargs 
     */
    public void send(String emailMessage, String subject, String ... recipients) {
        if (ArrayUtils.isEmpty(recipients)) {
            // if we don't receive any recipients, admin email is our default.
            recipients = new String[] { getTdarConfiguration().getSystemAdminEmail() };
        }
        SimpleMailMessage message = new SimpleMailMessage();
        // Message message = new MimeMessage(session);
        message.setFrom(getFromEmail());
        message.setSubject(subject);
        message.setTo(recipients);
        // message.addRecipient(RecipientType.TO, toAddress);
        // FIXME: send HTML and plaintext email? Will need to use JavaMailMessage and MimeMessages instead
        // see http://java.sun.com/products/javamail/FAQ.html#sendmpa
        // for more info
        // message.setContent(emailMessage, "text/plain");
        message.setText(emailMessage);
        mailSender.send(message);
    }

    public String getFromEmail() {
        return FROM_EMAIL_NAME + getTdarConfiguration().getEmailHostName();
    }

    public TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    public String render(String templateName, Object dataModel) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(
                    freemarkerConfiguration.getTemplate(templateName), dataModel);
        } catch (Exception e) {
            logger.error("Unable to process template " + templateName, e);
            throw new TdarRecoverableRuntimeException(e);
        }
    }

    /**
     * @return the mailSender
     */
    public MailSender getMailSender() {
        return mailSender;
    }

    /**
     * @param mailSender the mailSender to set
     */
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

}
