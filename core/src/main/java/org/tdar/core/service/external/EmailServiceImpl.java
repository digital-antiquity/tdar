package org.tdar.core.service.external;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.collection.RequestCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.HasEmail;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.email.AwsEmailService;
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
public class EmailServiceImpl implements EmailService {

    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ResourceCollectionDao resourceCollectionDao;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private FreemarkerService freemarkerService;

    @Autowired
    private AwsEmailService awsEmailService;
    
    public static String ATTACHMENTS = "ATTACHMENTS";
    public static String INLINE = "INLINE";

    
    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#queueAwsMessage(org.tdar.core.bean.notification.aws.AwsMessage)
	 */
    @Override
	@Transactional(readOnly = true)
    public void queueAwsMessage(AwsMessage message){
		//This will force rendering the entire message with attachments and mime boundaries. 
		//The result will be a string which gets queued. 
    	try {
    		awsEmailService.renderAndUpdateEmailContent(message);
        	awsEmailService.updateEmailSubject(message);
			MimeMessage mimeMessage = awsEmailService.createMimeMessage(message);
			String body = new String(awsEmailService.getByteArray(mimeMessage));
			message.getEmail().setMessage(body);
			queue(message.getEmail());
		} catch (MessagingException | IOException e) {
			 logger.error("Could not create MIME message {} {} ", e);
		}
    }
    
    /*
     * sends a message using a freemarker template instead of a string; templates are stored in src/main/resources/freemarker-templates
     */
    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#queueWithFreemarkerTemplate(java.lang.String, java.util.Map, org.tdar.core.bean.notification.Email)
	 */
    @Override
	@Transactional(readOnly = true)
    public void queueWithFreemarkerTemplate(String templateName, Map<String, ?> dataModel, Email email) {
        try {
            email.setMessage(freemarkerService.render(templateName, dataModel));
            queue(email);
        } catch (IOException fnf) {
            logger.error("Email template file not found (" + templateName + ")", fnf);
        }
    }
    

    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#sendUserInviteEmail(org.tdar.core.bean.entity.UserInvite, org.tdar.core.bean.entity.TdarUser)
	 */
    @Override
	@Transactional(readOnly = false)
    public void sendUserInviteEmail(UserInvite invite, TdarUser from) {
    	AwsMessage message = awsEmailService.createMessage(EmailType.INVITE, invite.getUser().getEmail());
    	message.getEmail().setUserGenerated(false);
    	message.addData("invite", 	invite);
    	message.addData("from", 	from);
        message.addData("to", 		invite.getUser());
        setupBasicComponents(message.getMap());
        
        queueAwsMessage(message);
        /*try {
        	awsEmailService.renderAndUpdateEmailContent(message);
        	awsEmailService.updateEmailSubject(message);
			awsEmailService.sendMultiPartMessage(message);
		} catch (MessagingException | IOException e) {
			logger.debug("Error happened: {} ",e,e);
		}*/

    }
    
    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#queue(org.tdar.core.bean.notification.Email)
	 */
    @Override
	@Transactional(readOnly = false)
    public void queue(Email email) {
        logger.debug("Queuing email {}", email);
        enforceFromAndTo(email);
        genericDao.save(email);
    }

    private void enforceFromAndTo(Email email) {
        if (StringUtils.isBlank(email.getTo())) {
            email.addToAddress(CONFIG.getSystemAdminEmail());
        }
        if (StringUtils.isBlank(email.getFrom())) {
            email.setFrom(getFromEmail());
        }
    }

    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#send(org.tdar.core.bean.notification.Email)
	 */
    @Override
	@Transactional(readOnly = false)
    public void send(Email email) {
        logger.debug("sending: {}", email);
        if (email.getNumberOfTries() < 1) {
            logger.debug("too many tries {}", email.getStatus());
            email.setStatus(Status.ERROR);
            genericDao.saveOrUpdate(email);
        }
        if (email.getStatus() != Status.QUEUED) {
            logger.trace("email rejected -- not queued {}", email.getStatus());
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
        genericDao.saveOrUpdate(email);
    }

    public String getFromEmail() {
        return CONFIG.getDefaultFromEmail();
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
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#constructEmail(org.tdar.core.bean.entity.Person, org.tdar.core.bean.entity.HasEmail, org.tdar.core.bean.resource.Resource, java.lang.String, java.lang.String, org.tdar.utils.EmailMessageType)
	 */
    @Override
	@Transactional(readOnly = false)
    public Email constructEmail(Person from, HasEmail to, Resource resource, String subject, String messageBody, EmailMessageType type) {
        return constructEmail(from, to, resource, subject, messageBody, type, null);
    }

    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#constructEmail(org.tdar.core.bean.entity.Person, org.tdar.core.bean.entity.HasEmail, org.tdar.core.bean.resource.Resource, java.lang.String, java.lang.String, org.tdar.utils.EmailMessageType, java.util.Map)
	 */
    @Override
	@Transactional(readOnly = false)
    public Email constructEmail(Person from, HasEmail to, Resource resource, String subjectSuffix, String messageBody, EmailMessageType type,
            Map<String, String[]> params) {
        Email email = new Email();
        genericDao.markWritable(email);
        email.setFrom(CONFIG.getDefaultFromEmail());
        String subjectPart = MessageHelper.getMessage(type.getLocaleKey());
        Map<String, Object> map = new HashMap<>();

        if (type == EmailMessageType.CUSTOM) {
            RequestCollection customRequest = resourceCollectionDao.findCustomRequest(resource);
            logger.debug("{}", customRequest);
            subjectPart = customRequest.getName();
            map.put("descriptionRequest", customRequest.getDescriptionRequest());
            map.put("customName", customRequest.getName());

        }
        if (CONFIG.isSendEmailToTester()) {
            email.setTo(from.getEmail());
        }
        email.setTo(to.getEmail());
        createResourceRevisionLogEntry(from, to, resource, subjectPart);

        String subject = String.format("%s: %s [id: %s] %s", CONFIG.getSiteAcronym(), subjectPart, resource.getId(), from.getProperName());
        if (StringUtils.isNotBlank(subjectSuffix)) {
            subject += " - " + subjectSuffix;
        }
        email.setSubject(subject);
        email.setType(type);
        if (resource != null) {
            email.setResource(resource);
        }
        email.setStatus(Status.IN_REVIEW);
        map.put("from", from);
        map.put("to", to);
        setupBasicComponents(map);
        if (MapUtils.isNotEmpty(params)) {
            map.putAll(params);
        }
        if (resource != null) {
            map.put("resource", resource);
        }
        map.put("message", messageBody);
        map.put("type", type);
        email.setMessage(messageBody);
        queueWithFreemarkerTemplate(type.getTemplateName(), map, email);
        return email;

    }

    private void createResourceRevisionLogEntry(Person from, HasEmail to, Resource resource, String subjectPart) {
        String msg = String.format("%s[%s] requesting access (%s) sent to %s[%s]", from.getProperName(), from.getId(), subjectPart, to.getProperName(),
                to.getId());
        TdarUser user = genericDao.find(TdarUser.class, CONFIG.getAdminUserId());
        if (from instanceof TdarUser) {
            user = (TdarUser) from;
        }
        ResourceRevisionLog rrl = new ResourceRevisionLog(msg, resource, user, RevisionLogType.REQUEST);
        genericDao.markWritable(rrl);
        genericDao.saveOrUpdate(rrl);
    }

    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#changeEmailStatus(org.tdar.core.bean.notification.Status, java.util.List)
	 */
    @Override
	@Transactional(readOnly = false)
    public void changeEmailStatus(Status action, List<Email> emails) {
        for (Email email : emails) {
            genericDao.markUpdatable(email);
            logger.debug("changing email[id={}] status from: {} to: {}", email.getId(), email.getStatus(), action);
            email.setStatus(action);
            genericDao.saveOrUpdate(email);
        }

    }

    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#findEmailsWithStatus(org.tdar.core.bean.notification.Status)
	 */
    @Override
	@Transactional(readOnly = true)
    public List<Email> findEmailsWithStatus(Status status) {
        List<Email> allEmails = genericDao.findAll(Email.class);
        List<Email> toReturn = new ArrayList<>();
        for (Email email : allEmails) {
            if (email.getStatus() == status) {
                toReturn.add(email);
            }
        }
        return toReturn;
    }

    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#proccessPermissionsRequest(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Resource, org.tdar.core.bean.entity.TdarUser, java.lang.String, boolean, org.tdar.utils.EmailMessageType, org.tdar.core.bean.entity.permissions.GeneralPermissions, java.util.Date)
	 */
    @Override
	@Transactional(readOnly = false)
    public void proccessPermissionsRequest(TdarUser requestor, Resource resource, TdarUser authenticatedUser, String comment, boolean reject,
            EmailMessageType type, GeneralPermissions permission, Date expires) {
        
    	EmailType emailType = null;
    	if(reject){
    		emailType = EmailType.PERMISSION_REQUEST_REJECTED;
    	}
    	else {
    		if(type == EmailMessageType.CUSTOM){
    			emailType = EmailType.PERMISSION_REQUEST_CUSTOM;
    		}
    		else {
    			emailType = EmailType.PERMISSION_REQUEST_ACCEPTED;
    		}
    	}
    	
    	AwsMessage message = awsEmailService.createMessage(emailType, requestor.getEmail());
    	message.addData("requestor", requestor);
    	message.addData("resource", resource);
    	message.addData("expires", expires);
    	message.addData("authorizedUser", authenticatedUser);
    	if (type == EmailMessageType.CUSTOM) {
            RequestCollection customRequest = resourceCollectionDao.findCustomRequest(resource);
            message.addData("customName", customRequest.getName());
            message.addData("descriptionResponse", customRequest.getDescriptionResponse());
        }
        if (StringUtils.isNotBlank(comment)) {
        	message.addData("message", comment);
        }
    	
        setupBasicComponents(message.getMap());
    	

        if(!reject){
            resource.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, requestor, permission, expires));
            genericDao.saveOrUpdate(resource.getAuthorizedUsers());
            genericDao.saveOrUpdate(resource);
        }
        
        //email.setResource(resource);
        message.getEmail().setUserGenerated(false);
        queueAwsMessage(message);
        
        try {
			awsEmailService.renderAndSendMessage(message);
		} catch (MessagingException | IOException e) {
			logger.debug("Couldn't send email: {}",e,e);
		}
    }

    private void setupBasicComponents(Map<String, Object> map) {
        map.put("baseUrl", CONFIG.getBaseUrl());
        map.put("siteAcronym", CONFIG.getSiteAcronym());
        map.put("serviceProvider", CONFIG.getServiceProvider());
    }

    /* (non-Javadoc)
	 * @see org.tdar.core.service.external.EmailService#sendUserInviteGrantedEmail(java.util.Map, org.tdar.core.bean.entity.TdarUser)
	 */
    @Override
	@Transactional(readOnly = false)
    public void sendUserInviteGrantedEmail(Map<TdarUser, List<HasName>> notices, TdarUser person) {
        for (Entry<TdarUser, List<HasName>> entry : notices.entrySet()) {
        	AwsMessage message = awsEmailService.createMessage(EmailType.INVITE_ACCEPTED, entry.getKey().getEmail());
            message.addData("owner", entry.getKey());
            message.addData("items", entry.getValue());
            message.addData("user", person);
            setupBasicComponents(message.getMap());
            //queueAwsMessage(message);
            try {
				awsEmailService.renderAndSendMessage(message);
			} catch (MessagingException | IOException e) {
				logger.debug("Could not send the email: {} ",e,e);
			}
        }
    }

}
