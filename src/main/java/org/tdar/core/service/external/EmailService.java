package org.tdar.core.service.external;

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
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
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

    private final static String FROM_EMAIL_NAME = "info@";

    @Autowired
    private MailSender mailSender;

    @Autowired
    FreemarkerService freemarkerService;

    public void sendTemplate(String templateName, Object dataModel, String subject, Person... recipients) {
        send(freemarkerService.render(templateName, dataModel), subject, recipients);
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
        return FROM_EMAIL_NAME + getTdarConfiguration().getEmailHostName();
    }

    public TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    /**
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
