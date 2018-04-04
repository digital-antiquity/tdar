package org.tdar.core.service.external;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.mail.MessagingException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.RequestCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.HasEmail;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.notification.emails.BasicAwsMessage;
import org.tdar.core.bean.notification.emails.EmailKeys;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.EmailDao;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.email.AwsEmailSender;
import org.tdar.utils.EmailStatisticsHelper;
import org.tdar.utils.MathUtils;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.StatsChartGenerator;

import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

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
    private AwsEmailSender awsEmailService;

    @Autowired
    private EmailStatisticsHelper emailStatsHelper;

    @Autowired
    private StatsChartGenerator chartGenerator;

    @Autowired
    private EmailDao emailDao;

    public static String ATTACHMENTS = "ATTACHMENTS";
    public static String INLINE = "INLINE";

    @Override
    @Transactional(readOnly = true)
    public Email dequeue(Email message) {
        retrieveAttachments(message);
        return message;
    }

    /**
     * Takes the attachements that are on the message, and saves them on disk.
     */
    public void saveAttachments(Email email) {
        if (CollectionUtils.isEmpty(email.getAttachments()) && email.getInlineAttachments().size() == 0) {
            logger.debug("There are no attachments to save");
            return;
        } else {
            String _attachmentDirectory = TdarConfiguration.getInstance().getEmailAttachmentsDirectory();
            String _messageAttachmentDir = _attachmentDirectory + email.getId().toString();
            String _inlineAttachmentDir = _messageAttachmentDir + File.separator + INLINE2;
            String _mimeAttachmentDir = _messageAttachmentDir + File.separator + MIME;
            logger.debug("Saving email attachments to " + _messageAttachmentDir);

            File msgAttachmentsDir = new File(_messageAttachmentDir);
            File inlineAttachments = new File(_inlineAttachmentDir);
            File mimeAttachments = new File(_mimeAttachmentDir);

            if (!msgAttachmentsDir.exists()) {
                logger.debug("Creating attachment directories");
                msgAttachmentsDir.mkdirs();
                inlineAttachments.mkdirs();
                mimeAttachments.mkdirs();
            }

            email.getInlineAttachments().forEach((fileName, file) -> {
                // Remember: the filename is needed so the inline image or file
                // renders correctly. The freemarker template will attributes
                // `src="cid:FILE.EXT"`.
                // This will generate a base64 encoded mime segment with for
                // FILE.EXT.
                file.renameTo(new File(_inlineAttachmentDir + File.separator + fileName));
            });

            email.getAttachments().forEach(file -> {
                // Save the file to the attachment.
                file.renameTo(new File(_mimeAttachmentDir + File.separator + file.getName()));
            });
        }
    }

    /**
     * Go through the attachment directories and add the attachments back to the
     * message.
     * 
     * @param email
     */
    public void retrieveAttachments(Email email) {
        String _attachmentDirectory = TdarConfiguration.getInstance().getEmailAttachmentsDirectory();
        String _messageAttachmentDir = _attachmentDirectory + File.separator + email.getId().toString();
        String _inlineAttachmentDir = _messageAttachmentDir + File.separator + INLINE2;
        String _mimeAttachmentDir = _messageAttachmentDir + File.separator + MIME;

        // Check to see if the message attachment directory exists. if not, dont
        // bother attaching files.
        File dir = new File(_messageAttachmentDir);
        if (!dir.exists()) {
            return;
        }

        FileUtils.listFiles(new File(_inlineAttachmentDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
                .forEach(file -> {
                    email.addInlineAttachment(file.getName(), file);
                });

        FileUtils.listFiles(new File(_mimeAttachmentDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
                .forEach(file -> {
                    email.addAttachment(file);
                });
    }

    /*
     * sends a message using a freemarker template instead of a string;
     * templates are stored in src/main/resources/freemarker-templates
     */
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.EmailService#queueWithFreemarkerTemplate(
     * java.lang.String, java.util.Map, org.tdar.core.bean.notification.Email)
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.EmailService#sendUserInviteEmail(org.tdar.
     * core.bean.entity.UserInvite, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public Email sendUserInviteEmail(UserInvite invite, TdarUser from) {
        Email message = createMessage(EmailType.INVITE, invite.getUser().getEmail());
        message.setUserGenerated(false);
        message.addData(EmailKeys.INVITE, invite);
        message.addData(EmailKeys.FROM, from);
        message.addData(EmailKeys.TO, invite.getUser());
        message.setStatus(Status.QUEUED);
        setupBasicComponents(message.getMap());
        renderAndUpdateEmailContent(message);
        updateEmailSubject(message);
        queue(message);
        return message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.EmailService#queue(org.tdar.core.bean.
     * notification.Email)
     */
    @Override
    @Transactional(readOnly = false)
    public void queue(Email email) {
        logger.debug("Queuing message for delivery: {} ", email);
        enforceFromAndTo(email);
        genericDao.save(email);
        logger.debug("Queuing email {}", email);
        saveAttachments(email);
    }

    private void enforceFromAndTo(Email email) {
        if (StringUtils.isBlank(email.getTo())) {
            email.addToAddress(CONFIG.getSystemAdminEmail());
        }
        if (StringUtils.isBlank(email.getFrom())) {
            email.setFrom(getFromEmail());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.external.EmailService#send(org.tdar.core.bean.
     * notification.Email)
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
            // If the message is only of Email, then it isn't set up as an HTML
            // email. Doing send will
            // send it as a single part message without attachments. The logo
            // won't be embedded into the message.
            if (email.getClass().equals(Email.class)) {
                awsEmailService.sendMessage(email);
            } else {
                // For all other messages, attach the logo and send as
                // multi-part.
                awsEmailService.sendMultiPartMessage(email);
            }
            email.setStatus(Status.SENT);
            email.setDateSent(new Date());
        } catch (MailException | IOException | MessagingException me) {
            email.setNumberOfTries(email.getNumberOfTries() - 1);
            email.setErrorMessage(me.getMessage());
            logger.error("email error: {} {}", email, me);
        }
        genericDao.saveOrUpdate(email);
    }

    @Override
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


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.EmailService#constructEmail(org.tdar.core.
     * bean.entity.Person, org.tdar.core.bean.entity.HasEmail,
     * org.tdar.core.bean.resource.Resource, java.lang.String, java.lang.String,
     * org.tdar.utils.EmailType, java.util.Map)
     */
    @Override
    @Transactional(readOnly = false)
    public Email createAccessRequestEmail(Person from, HasEmail to, Resource resource, String subjectSuffix, String messageBody,
            EmailType type, Map<String, String[]> params) {
        
        String subjectPart = type.name();

        Email email = createMessage(type, to.getEmail());
        genericDao.markWritable(email);
        setupBasicComponents(email.getMap());
        email.setFrom(CONFIG.getDefaultFromEmail());
        email.setTo(to.getEmail());
        email.setType(type);
        email.setStatus(Status.IN_REVIEW);
        email.addData(EmailKeys.FROM, from);
        email.addData(EmailKeys.TO, to);
        email.addData(EmailKeys.MESSAGE, messageBody);
        email.addData(EmailKeys.TYPE, type);

        if (type == EmailType.CUSTOM) {
            RequestCollection customRequest = resourceCollectionDao.findCustomRequest(resource);
            logger.debug("{}", customRequest);
            subjectPart = customRequest.getName();
            email.addData(DESCRIPTION_REQUEST, customRequest.getDescriptionRequest());
            email.addData(CUSTOM_NAME, customRequest.getName());
        }
        
        if (MapUtils.isNotEmpty(params)) {
            email.getMap().putAll(params);
        }

        if (resource != null) {
            email.setResource(resource);
            email.addData(EmailKeys.RESOURCE, resource);
        }
        
        if (CONFIG.isSendEmailToTester()) {
            email.setTo(from.getEmail());
        }

        createResourceRevisionLogEntry(from, to, resource, subjectPart);
        updateEmailSubject(email);
        if (StringUtils.isNotBlank(subjectSuffix)) {
           String subject = email.getSubject().concat(" - " + subjectSuffix);
           email.setSubject(subject);
        }
        
        renderAndUpdateEmailContent(email);
        queue(email);
        return email;
    }

    private void createResourceRevisionLogEntry(Person from, HasEmail to, Resource resource, String subjectPart) {
        String msg = String.format("%s[%s] requesting access (%s) sent to %s[%s]", from.getProperName(), from.getId(),
                subjectPart, to.getProperName(), to.getId());
        TdarUser user = genericDao.find(TdarUser.class, CONFIG.getAdminUserId());
        if (from instanceof TdarUser) {
            user = (TdarUser) from;
        }
        ResourceRevisionLog rrl = new ResourceRevisionLog(msg, resource, user, RevisionLogType.REQUEST);
        genericDao.markWritable(rrl);
        genericDao.saveOrUpdate(rrl);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.EmailService#changeEmailStatus(org.tdar.
     * core.bean.notification.Status, java.util.List)
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.EmailService#findEmailsWithStatus(org.tdar
     * .core.bean.notification.Status)
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.EmailService#proccessPermissionsRequest(
     * org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.resource.Resource,
     * org.tdar.core.bean.entity.TdarUser, java.lang.String, boolean,
     * org.tdar.utils.EmailType,
     * org.tdar.core.bean.entity.permissions.GeneralPermissions, java.util.Date)
     */
    @Override
    @Transactional(readOnly = false)
    public Email proccessPermissionsRequest(TdarUser requestor, Resource resource, TdarUser authenticatedUser,
            String comment, boolean reject, EmailType type, Permissions permission, Date expires) {
        EmailType emailType = null;
        if (reject) {
            emailType = EmailType.PERMISSION_REQUEST_REJECTED;
        } else {
            if (type == EmailType.CUSTOM) {
                emailType = EmailType.PERMISSION_REQUEST_CUSTOM;
            } else {
                emailType = EmailType.PERMISSION_REQUEST_ACCEPTED;
            }
        }

        logger.debug("email type is {}", emailType);

        Email message = createMessage(emailType, requestor.getEmail());
        message.addData(EmailKeys.REQUESTOR, requestor);
        message.addData(EmailKeys.RESOURCE, resource);
        message.addData(EmailKeys.EXPIRES, expires);

        message.addData(EmailKeys.AUTHORIZED_USER, authenticatedUser);
        if (type == EmailType.CUSTOM) {
            RequestCollection customRequest = resourceCollectionDao.findCustomRequest(resource);
            message.addData(CUSTOM_NAME, customRequest.getName());
            message.addData(EmailKeys.DESCRIPTION_RESPONSE, customRequest.getDescriptionResponse());
        }
        if (StringUtils.isNotBlank(comment)) {
            message.addData(EmailKeys.MESSAGE, comment);
        }

        setupBasicComponents(message.getMap());

        if (!reject) {
            resource.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, requestor, permission, expires));
            genericDao.saveOrUpdate(resource.getAuthorizedUsers());
            genericDao.saveOrUpdate(resource);
        }

        // email.setResource(resource);
        message.setUserGenerated(false);

        updateEmailSubject(message);
        logger.debug("email subject is {}", message.getSubject());
        renderAndUpdateEmailContent(message);
        queue(message);

        try {
            sendAwsHtmlMessage(message);
        } catch (MessagingException | IOException e) {
            logger.error("Couldn't send email: {}", e, e);
        }
        return message;
    }

    private void setupBasicComponents(Map<String, Object> map) {
        map.put(EmailKeys.BASE_URL, CONFIG.getBaseUrl());
        map.put(EmailKeys.SITE_ACRONYM, CONFIG.getSiteAcronym());
        map.put(EmailKeys.SERVICE_PROVIDER, CONFIG.getServiceProvider());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.tdar.core.service.external.EmailService#sendUserInviteGrantedEmail(
     * java.util.Map, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void sendUserInviteGrantedEmail(Map<TdarUser, List<HasName>> notices, TdarUser person) {
        for (Entry<TdarUser, List<HasName>> entry : notices.entrySet()) {
            Email message = createMessage(EmailType.INVITE_ACCEPTED, entry.getKey().getEmail());
            message.addData(EmailKeys.OWNER, entry.getKey());
            message.addData(EmailKeys.ITEMS, entry.getValue());
            message.addData(EmailKeys.USER, person);
            setupBasicComponents(message.getMap());
            // queueemail(message);
            try {
                renderAndSendMessage(message);
            } catch (MessagingException | IOException e) {
                logger.error("Could not send the email: {} ", e, e);
            }
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Email sendWelcomeEmail(Person person) {
        Map<String, Object> result = new HashMap<>();
        result.put(EmailKeys.USER, person);
        result.put(EmailKeys.CONFIG, CONFIG);
        Email email = createMessage(EmailType.NEW_USER_WELCOME, person.getEmail());
        email.setUserGenerated(false);
        email.addData(EmailKeys.USER, person);
        email.addData(EmailKeys.CONFIG, CONFIG);
        renderAndQueueMessage(email);
        return email;
    }

    /**
     * Generates a summary of the user's resources by billing account and
     * immediately sends the email
     * 
     * @param user
     * @param billingAccount
     */
    @Override
    public void generateAndSendUserStatisticEmail(TdarUser user, BillingAccount billingAccount) {
        Email message = generateUserStatisticsEmail(user, billingAccount);
        try {
            sendAwsHtmlMessage(message);
        } catch (MessagingException | IOException e) {
            logger.error("Couldn't send userStatisticsEmail: {} {}", e, e);
        }
    }

    @Override
    public Email generateUserStatisticsEmail(TdarUser user, BillingAccount billingAccount) {
        logger.debug("Starting sending statistics email to {}", user);

        // Get the date granularity.
        Date date = emailStatsHelper.getStartDate(billingAccount.getResources());
        DateGranularity granularity = emailStatsHelper.getDateGranularity(date);
        StatsResultObject stats = emailStatsHelper.getAccountStatistics(billingAccount, granularity);

        // Generate temporary file names
        String piechartFileName = System.currentTimeMillis() + _RESOURCE_PIECHART_PNG;
        String downloadsFileName = System.currentTimeMillis() + _DOWNLOADS_BARCHART_PNG;
        String viewsFileName = System.currentTimeMillis() + _VIEWS_BARCHART_PNG;

        // Generate the resources pie graph.
        Map<String, Number> pieChartData = emailStatsHelper.generateUserResourcesPieChartData(billingAccount);
        File piechart = chartGenerator.generateResourcesPieChart(pieChartData, piechartFileName);

        // Generate the downloads graph
        Map<String, Number> totalDownloadsData = emailStatsHelper.generateTotalDownloadsChartData(billingAccount,
                stats);
        File barchart1 = chartGenerator.generateTotalDownloadsBarChart(totalDownloadsData, downloadsFileName);

        // Generate the total views graph
        Map<String, Number> totalViewsData = emailStatsHelper.generateTotalViewsChartData(billingAccount, stats);
        File barchart2 = chartGenerator.generateTotalViewsBarChart(totalViewsData, viewsFileName);

        // Construct the message and attach the graphs.
        Email message = createMessage(EmailType.MONTHLY_USER_STATISTICS, user.getEmail());
        message.addData(RESOURCES, emailStatsHelper.getTopResources(billingAccount));
        message.addData(EmailKeys.USER, user);
        message.addData(AVAILABLE_SPACE,
                MathUtils.divideByRoundDown(billingAccount.getAvailableSpaceInBytes(), 1024 * 1024));
        message.addData("availableFiles", billingAccount.getAvailableNumberOfFiles());
        message.addInlineAttachment(RESOURCES_PNG, piechart);
        message.addInlineAttachment(TOTALVIEWS_PNG, barchart1);
        message.addInlineAttachment(TOTALDOWNLOADS_PNG, barchart2);

        updateEmailSubject(message);
        renderAndUpdateEmailContent(message);
        return message;
    }

    /**
     * 
     * 
     * @param EmailType
     *            the type of email to be created
     * @return AwsEmail a new instance
     */
    @Override
    public Email createMessage(EmailType emailType, String to) {
        Email email = null;
        if (emailType.getEmailClass() != null) {
            try {
                email = emailType.getEmailClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Coudn't instantiate a new email object)", e);
            }
        } else {
            email = new BasicAwsMessage();
        }
        email.setMessageUuid(UUID.randomUUID().toString());
        email.setType(emailType);
        if (emailType.getFromAddress() == null) {
            email.setFrom(CONFIG.getDefaultFromEmail());
        } else {
            email.setFrom(emailType.getFromAddress());
        }

        email.setSubject("");
        email.setTo(to);
        setupBasicComponents(email.getMap());
        return email;
    }

    @Override
    public void renderAndUpdateEmailContent(Email message) {
        String templateName = null;
        try {
            templateName = message.getType().getTemplateLocation();
            String content = freemarkerService.render(templateName, message.getMap());
            message.setMessage(content);
        } catch (IOException e) {
            logger.error("Email template file not found (" + templateName + ")", e);
        }
    }

    /**
     * Forces the message to re-render dynamically created subject line.
     */
    @Override
    public void updateEmailSubject(Email message) {
        message.setSubject(message.createSubjectLine());
    }

    /**
     * Takes an AWS message, renders the Freemarker template to update the HTML
     * body, renders the subject line, then creates an MIME version and sends it
     * via AWS.
     */
    @Override
    public SendRawEmailResult renderAndSendMessage(Email message) throws MessagingException, IOException {
        updateEmailSubject(message);
        renderAndUpdateEmailContent(message);
        return sendAwsHtmlMessage(message);
    }

    @Override
    public Email renderAndQueueMessage(Email message) {
        updateEmailSubject(message);
        renderAndUpdateEmailContent(message);
        queue(message);
        return message;
    }

    @Override
    public SendRawEmailResult sendAwsHtmlMessage(Email message) throws MessagingException, IOException {
        logger.debug("Sending Multi-part email via AWS", message.getTo());
        return awsEmailService.sendMultiPartMessage(message);
    }

    @Override
    public void markMessageAsBounced(String messageGuid, String errorMessage) {
        List<Email> emails = emailDao.findEmailByGuid(messageGuid);
        logger.debug("Found {} emails", emails.size());
        if (CollectionUtils.isNotEmpty(emails)) {
            markMessageAsBounced(emails.get(0), errorMessage);
        }
    }

    @Override
    public void markMessageAsBounced(Email email, String errorMessage) {
        logger.debug("Marking email {} as bounced", email);
        email.setStatus(Status.BOUNCED);
        email.setErrorMessage(errorMessage);
        genericDao.saveOrUpdate(email);
    }

    public AwsEmailSender getAwsEmailService() {
        return awsEmailService;
    }

    public void setAwsEmailService(AwsEmailSender awsEmailService) {
        this.awsEmailService = awsEmailService;
    }

    public EmailStatisticsHelper getEmailStatsHelper() {
        return emailStatsHelper;
    }

    public void setEmailStatsHelper(EmailStatisticsHelper emailStatsHelper) {
        this.emailStatsHelper = emailStatsHelper;
    }

    public StatsChartGenerator getChartGenerator() {
        return chartGenerator;
    }

    public void setChartGenerator(StatsChartGenerator chartGenerator) {
        this.chartGenerator = chartGenerator;
    }

}