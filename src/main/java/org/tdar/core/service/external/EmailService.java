package org.tdar.core.service.external;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Email.Status;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.GenericService;
import org.tdar.utils.EmailMessageType;
import org.tdar.utils.MessageHelper;

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
        logger.debug("Queuing email {}", email);
        enforceFromAndTo(email);
        genericService.save(email);
    }

    private void enforceFromAndTo(Email email) {
        if (StringUtils.isBlank(email.getTo())) {
            email.addToAddress(getTdarConfiguration().getSystemAdminEmail());
        }
        if (StringUtils.isBlank(email.getFrom())) {
            email.setFrom(getFromEmail());
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
    public void send(Email email) {
        logger.debug("sending: {}", email);
        if (email.getNumberOfTries() < 1) {
            logger.debug("too many tries {}", email.getStatus());
            email.setStatus(Status.ERROR);
            genericService.saveOrUpdate(email);
        }
        if (email.getStatus() != Status.QUEUED) {
            logger.debug("email rejected -- not queued {}", email.getStatus());
            return;
        }
        enforceFromAndTo(email);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // Message message = new MimeMessage(session);
            message.setFrom(email.getFrom());
            message.setSubject(email.getSubject());
            message.setTo(email.getToAsArray());
            message.setText(email.getMessage());
            mailSender.send(message);
            email.setStatus(Status.SENT);
            email.setDateSent(new Date());
        } catch (MailException me) {
            email.setNumberOfTries(email.getNumberOfTries() - 1);
            email.setErrorMessage(me.getMessage());
            logger.error("email error: {} {}", email, me);
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
    @Autowired
    @Qualifier("mailSender")
    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Transactional(readOnly = false)
    public Email constructEmail(Person from, Person to, Resource resource, String subjectSuffix, String messageBody, EmailMessageType type) {
        Email email = new Email();
        genericService.markWritable(email);
        email.setFrom(getTdarConfiguration().getDefaultFromEmail());
        if (TdarConfiguration.getInstance().isSendEmailToTester()) {
            email.setTo(from.getEmail());
        }
        email.setTo(to.getEmail());
        String subject = String.format("%s: %s", TdarConfiguration.getInstance().getSiteAcronym(), MessageHelper.getMessage(type.getLocaleKey()));
        if (StringUtils.isNotBlank(subjectSuffix)) {
            subject += " - " + subjectSuffix;
        } 
        email.setSubject(subject);
        email.setStatus(Status.IN_REVIEW);
        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("to", to);
        map.put("baseUrl", TdarConfiguration.getInstance().getBaseUrl());
        if (resource != null) {
            map.put("resource", resource);
        }
        map.put("message", messageBody);
        map.put("type", type);
        email.setMessage(messageBody);
        queueWithFreemarkerTemplate(type.getTemplateName(), map, email);
        return email;

    }

    @Transactional(readOnly=false)
    public void changeEmailStatus(Status action, List<Email> emails) {
        for (Email email : emails) {
            genericService.markUpdatable(email);
            logger.debug("changing email[id={}] status from: {} to: {}", email.getId(), email.getStatus(), action);
            email.setStatus(action);
            genericService.saveOrUpdate(email);
        }
        
    }

}
