package org.tdar.core.service.external;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.tdar.core.bean.util.Email;
import org.tdar.core.bean.util.Email.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.GenericService;

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
    private GenericService genericService;
    
    @Autowired
    private FreemarkerService freemarkerService;

    /*
     * sends a message using a freemarker template instead of a string; templates are stored in src/main/resources/freemarker-templates
     */
    public void queueWithFreemarkerTemplate(String templateName, Object dataModel, Email email) {
        try {
            email.setMessage(freemarkerService.render(templateName, dataModel));
            queue(email);
        } catch (IOException fnf) {
            logger.error("Email template file not found (" + templateName + ")", fnf);
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
    public void queue(Email email) {
        if (StringUtils.isBlank(email.getTo())) {
            email.addToAddress(getTdarConfiguration().getSystemAdminEmail());
        } 
        genericService.save(email);
    }

    /**
     * Sends an email message to the given recipients. If no recipients are passed in, defaults to TdarConfiguration.getSystemAdminEmail().
     * 
     * @param emailMessage
     * @param subject
     * @param recipients
     *            set of String varargs
     */
    public void send(Email email) {
        if (email.getNumberOfTries() < 1) {
            email.setStatus(Status.ERROR);
            genericService.saveOrUpdate(email);
        }
        if (email.getStatus() != Status.QUEUED) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Message message = new MimeMessage(session);
            message.setFrom(email.getFrom());
            message.setSubject(email.getSubject());
            message.setTo(email.getToAsArray());
            message.setText(email.getMessage());
            mailSender.send(message);
            email.setStatus(Status.SENT);
        } catch (MailException me) {
            email.setNumberOfTries(email.getNumberOfTries() - 1);
            email.setErrorMessage(me.getMessage());
        }
        genericService.saveOrUpdate(email);
    }

    
    public String getFromEmail() {
        return getTdarConfiguration().getDefaultFromEmail();
    }

    public TdarConfiguration getTdarConfiguration() {
        return TdarConfiguration.getInstance();
    }

    /**
     * The mailSender allows us to plug in our own SMTP server to test
     * 
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
