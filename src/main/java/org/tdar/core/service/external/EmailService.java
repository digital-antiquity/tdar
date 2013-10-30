package org.tdar.core.service.external;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;

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

    @Autowired
    private MailSender mailSender;

    @Autowired
    FreemarkerService freemarkerService;

    /*
     * sends a message using a freemarker template instead of a string; templates are stored in src/main/resources/freemarker-templates
     */
    public void sendWithFreemarkerTemplate(String templateName, Object dataModel, String subject, Person... recipients) {
        try {
            send(freemarkerService.render(templateName, dataModel), subject, recipients);
        } catch (IOException fnf) {
            logger.error("Email template file not found (" + subject + ")", fnf);
        }
    }

    /**
     * Sends an email message to the given recipients. If no recipients are passed in, defaults to TdarConfiguration.getSystemAdminEmail().
     * 
     * @param emailMessage
     * @param subject
     * @param recipients
     *            set of String varargs
     */
    public void send(String emailMessage, String subject, Person... recipients) {
        List<String> toAddresses = new ArrayList<String>();
        if (ArrayUtils.isEmpty(recipients)) {
            // if we don't receive any recipients, admin email is our default.
            toAddresses.add(getTdarConfiguration().getSystemAdminEmail());
        } else {
            for (Person recipient : recipients) {
                if (StringUtils.isNotEmpty(recipient.getEmail())) {
                    toAddresses.add(recipient.getEmail());
                }
            }
        }
        SimpleMailMessage message = new SimpleMailMessage();
        // Message message = new MimeMessage(session);
        message.setFrom(getFromEmail());
        message.setSubject(subject);
        message.setTo(toAddresses.toArray(new String[0]));
        // message.addRecipient(RecipientType.TO, toAddress);
        // FIXME: send HTML and plaintext email? Will need to use JavaMailMessage and MimeMessages instead
        // see http://java.sun.com/products/javamail/FAQ.html#sendmpa
        // for more info
        // message.setContent(emailMessage, "text/plain");
        message.setText(emailMessage);
        mailSender.send(message);
    }

    public String getFromEmail() {
        return getTdarConfiguration().getDefaultFromEmail();
    }

    public TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    /**
     * The mailSender allows us to plug in our own SMTP server to test
     * @return the mailSender
     */
    public MailSender getMailSender() {
        return mailSender;
    }

    /**
     * @param mailSender
     *            the mailSender to set
     */
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

}
