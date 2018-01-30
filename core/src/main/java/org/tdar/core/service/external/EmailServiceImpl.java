package org.tdar.core.service.external;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.notification.aws.BasicAwsMessage;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.email.AwsEmailSender;
import org.tdar.utils.EmailStatisticsHelper;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.StatsChartGenerator;

import com.amazonaws.services.simpleemail.model.RawMessage;
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
		String _attachmentDirectory = TdarConfiguration.getInstance().getEmailAttachmentsDirectory();
		String _messageAttachmentDir = _attachmentDirectory + File.pathSeparator + email.getId().toString();
		String _inlineAttachmentDir = _messageAttachmentDir + File.pathSeparator + "inline";
		String _mimeAttachmentDir = _messageAttachmentDir + File.pathSeparator + "mime";

		File attachmentDir = new File(_messageAttachmentDir);
		File inlineAttachments = new File(_inlineAttachmentDir);
		File mimeAttachments = new File(_mimeAttachmentDir);

		if (!attachmentDir.exists()) {
			attachmentDir.mkdir();
			inlineAttachments.mkdir();
			mimeAttachments.mkdir();
		}

		email.getInlineAttachments().forEach((fileName, file) -> {
			// Remember: the filename is needed so the inline image or file
			// renders correctly. The name needs to be the same
			// since its probably referenced somewhere in the HTML body of what
			// it expects.
			file.renameTo(new File(_mimeAttachmentDir + File.separator + fileName));
		});
		email.getAttachments().forEach(file -> {
			// Save the file to the attachment.
			file.renameTo(new File(_mimeAttachmentDir + File.separator + file.getName()));
		});
	}

	/**
	 * Go through the attachment directories and add the attachments back to the
	 * message.
	 * 
	 * @param email
	 */
	public void retrieveAttachments(Email email) {
		String _attachmentDirectory = TdarConfiguration.getInstance().getEmailAttachmentsDirectory();
		String _messageAttachmentDir = _attachmentDirectory + File.pathSeparator + email.getId().toString();
		String _inlineAttachmentDir = _messageAttachmentDir + File.pathSeparator + "inline";
		String _mimeAttachmentDir = _messageAttachmentDir + File.pathSeparator + "mime";

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
	public void sendUserInviteEmail(UserInvite invite, TdarUser from) {
		Email message = createMessage(EmailType.INVITE, invite.getUser().getEmail());
		message.setUserGenerated(false);
		message.addData("invite", invite);
		message.addData("from", from);
		message.addData("to", invite.getUser());
		message.setStatus(Status.QUEUED);
		setupBasicComponents(message.getMap());
		queue(message);
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
		saveAttachments(email);
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
	 * org.tdar.utils.EmailType)
	 */
	@Override
	@Transactional(readOnly = false)
	public Email constructEmail(Person from, HasEmail to, Resource resource, String subject, String messageBody,
			EmailType type) {
		return constructEmail(from, to, resource, subject, messageBody, type, null);
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
	public Email constructEmail(Person from, HasEmail to, Resource resource, String subjectSuffix, String messageBody,
			EmailType type, Map<String, String[]> params) {
		Email email = new Email();
		genericDao.markWritable(email);
		email.setFrom(CONFIG.getDefaultFromEmail());
		String subjectPart = MessageHelper.getMessage(type.getLocaleKey());
		Map<String, Object> map = new HashMap<>();

		if (type == EmailType.CUSTOM) {
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

		String subject = String.format("%s: %s [id: %s] %s", CONFIG.getSiteAcronym(), subjectPart, resource.getId(),
				from.getProperName());
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
		queueWithFreemarkerTemplate(type.getTemplateLocation(), map, email);
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
	public void proccessPermissionsRequest(TdarUser requestor, Resource resource, TdarUser authenticatedUser,
			String comment, boolean reject, EmailType type, GeneralPermissions permission, Date expires) {

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

		Email message = createMessage(emailType, requestor.getEmail());
		message.addData("requestor", requestor);
		message.addData("resource", resource);
		message.addData("expires", expires);
		message.addData("authorizedUser", authenticatedUser);
		if (type == EmailType.CUSTOM) {
			RequestCollection customRequest = resourceCollectionDao.findCustomRequest(resource);
			message.addData("customName", customRequest.getName());
			message.addData("descriptionResponse", customRequest.getDescriptionResponse());
		}
		if (StringUtils.isNotBlank(comment)) {
			message.addData("message", comment);
		}

		setupBasicComponents(message.getMap());

		if (!reject) {
			resource.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, requestor, permission, expires));
			genericDao.saveOrUpdate(resource.getAuthorizedUsers());
			genericDao.saveOrUpdate(resource);
		}

		// email.setResource(resource);
		message.setUserGenerated(false);
		queue(message);

		try {
			renderAndSendMessage(message);
		} catch (MessagingException | IOException e) {
			logger.error("Couldn't send email: {}", e, e);
		}
	}

	private void setupBasicComponents(Map<String, Object> map) {
		map.put("baseUrl", CONFIG.getBaseUrl());
		map.put("siteAcronym", CONFIG.getSiteAcronym());
		map.put("serviceProvider", CONFIG.getServiceProvider());
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
			message.addData("owner", entry.getKey());
			message.addData("items", entry.getValue());
			message.addData("user", person);
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

		ClassPathResource logo = new ClassPathResource("tdar-logo.png");
		messageHelper.addInline("logo", logo);

		mimeMessage = messageHelper.getMimeMessage();
		mimeMessage.addHeader("x-tdar-message-id", message.getMessageUuid());
		return mimeMessage;
	}

	/**
	 * Generates a summary of the user's resources by billing account and
	 * immediately sends the email
	 * 
	 * @param user
	 * @param billingAccount
	 */
	@Override
	public void sendUserStatisticEmail(TdarUser user, BillingAccount billingAccount) {
		Email message = generateUserStatisticsEmail(user, billingAccount);
		try {

			renderAndSendMessage(message);
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
		String piechartFileName = System.currentTimeMillis() + "_resource-piechart";
		String downloadsFileName = System.currentTimeMillis() + "_downloads-barchart";
		String viewsFileName = System.currentTimeMillis() + "_views-barchart";

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
		message.addData("resources", emailStatsHelper.getTopResources(billingAccount));
		message.addData("user", user);
		message.addData("availableSpace", billingAccount.getAvailableSpaceInBytes());
		message.addData("availableFiles", billingAccount.getAvailableNumberOfFiles());
		message.addInlineAttachment("resources", piechart);
		message.addInlineAttachment("totalviews", barchart1);
		message.addInlineAttachment("totaldownloads", barchart2);
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

	private RawMessage createRawMessage(MimeMessage message) throws IOException, MessagingException {
		byte[] byteArray = getByteArray(message);
		return new RawMessage(ByteBuffer.wrap(byteArray));
	}

	private byte[] getByteArray(MimeMessage message) throws IOException, MessagingException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		message.writeTo(outputStream);
		return outputStream.toByteArray();
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
	public SendRawEmailResult sendAwsHtmlMessage(Email message) throws MessagingException, IOException {
		MimeMessage mimeMessage = createMimeMessage(message);
		RawMessage rawMessage = createRawMessage(mimeMessage);
		return awsEmailService.sendMultiPartMessage(rawMessage);
	}

	public void createUserStatEmail(TdarUser user) {
		// given a user, retrieve their billing account information.
		// Given a billign account, retrieve all the resources.
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