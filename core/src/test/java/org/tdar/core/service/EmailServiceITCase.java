package org.tdar.core.service;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.email.MockAwsEmailSenderServiceImpl;
import org.tdar.utils.EmailRawMessageHelper;

import com.amazonaws.services.simpleemail.model.SendRawEmailResult;

public class EmailServiceITCase extends AbstractIntegrationTestCase {

    @Test
    @Rollback
    public void testUsingMockAwsObject() {
        assertTrue("The object is an AWS mock", emailService.getAwsEmailService() instanceof MockAwsEmailSenderServiceImpl);
    }

    @Test
    @Rollback(false)
    public void testSendRequestAccessEmail() {
        sendContactRequestEmail(EmailType.REQUEST_ACCESS);
    }

    @Test
    @Rollback
    public void testSendResourceOwnerContactEmail() {
        sendContactRequestEmail(EmailType.CONTACT);
    }

    @Test
    @Rollback
    public void testSendSuggestCorrectionEmail() {
        sendContactRequestEmail(EmailType.SUGGEST_CORRECTION);
    }

    @Test
    @Rollback
    public void testPermissionRequestRejectedEmail() {
        EmailType emailType = EmailType.PERMISSION_REQUEST_REJECTED;
        String comment = "Your permission request has been rejected";
        processPermissionRequestEmail(emailType, comment);
    }

    @Test
    @Rollback
    public void testPermissionRequestCustomEmail() {
        EmailType emailType = EmailType.PERMISSION_REQUEST_CUSTOM;
        String comment = "Your permission request is pending further review";
        processPermissionRequestEmail(emailType, comment);
    }

    @Test
    @Rollback
    public void testPermissionRequestAcceptedEmail() {
        EmailType emailType = EmailType.PERMISSION_REQUEST_ACCEPTED;
        String comment = "Your permission request has been accepted";
        processPermissionRequestEmail(emailType, comment);
    }

    @Test
    @Rollback
    public void testMockMailSender() {
        Person to = new Person(null, null, getTestUserEmail());
        String mailBody = "this is a message body";
        String subject = "this is a subject";
        Email email = new Email();
        email.addToAddress(to.getEmail());
        email.setMessage(mailBody);
        email.setSubject(subject);
        emailService.send(email);

        Email received = checkMailAndGetLatest(mailBody);

        assertEquals(received.getSubject(), subject);
        assertEquals(received.getMessage(), mailBody);
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo(), to.getEmail());

        assertEquals(email.getStatus(), Status.SENT);
        // implicit assumption that something that is marked sent has a
        // sent-date
        assertThat(email.getDateSent(), is(not(nullValue())));
    }

    @Test
    public void testMultipleRecipientsWithCommas() throws MessagingException{
        String toRecipients = "toPerson1@email.com,toEmail2@email.com,toEmail3@email.com";
        testSendingToMulitpleRecipients(toRecipients, 3);
    }
    
    @Test
    public void testMultipleRecipientsWithSemiColons() throws MessagingException{
        String toRecipients = "toPerson1@email.com;toEmail2@email.com;toEmail3@email.com";
        testSendingToMulitpleRecipients(toRecipients, 3);
    }
    
    @Test
    public void testMultipleRecipientsCommasAndSemiColons() throws MessagingException{
        String toRecipients = "toPerson1@email.com,toEmail2@email.com;toEmail3@email.com";
        testSendingToMulitpleRecipients(toRecipients, 3);
    }
    
    
    public void testSendingToMulitpleRecipients(String toRecipients, int size) throws MessagingException {
       
        Email email = new Email();
        email.setMessage("Some message");
        email.setTo(toRecipients);
        email.setFrom("From@me.com");
        email.setSubject("Test Subject");

        EmailRawMessageHelper helper = new EmailRawMessageHelper();
        MimeMessage mimeMessage = helper.createMimeMessage(email);

        String assertion = String.format("Testing if there's %s recipients", size);
        logger.debug(assertion);
        assertEquals(assertion, size, mimeMessage.getAllRecipients().length);

    }

    @Test
    @Rollback
    public void testSendInviteEmail() {
        Person to = new Person("To", "Person", getTestUserEmail());
        Person from = new Person("From", "Somone", "toguy@tdar.net");
        TdarUser fromUser = new TdarUser(from, "from");
        UserInvite invite = new UserInvite();
        invite.setPerson(to);

        Resource project = createAndSaveNewProject("Test Project");
        assertEquals(project.getTitle(), "Test Project");

        invite.setResource(project);
        Email email = emailService.sendUserInviteEmail(invite, fromUser);

        emailService.send(email);
    }

    @Test
    @Rollback
    public void testSendWelcomeEmail() {
        TdarUser to = new TdarUser("To", "Person", getTestUserEmail(), "testuser");
        Email email = emailService.sendWelcomeEmail(to);
        assertTrue("email has 'Welcome' in the subject'", email.getSubject().contains("Welcome"));
        assertTrue("email has content", email.getMessage().contains("Hello To,<br />"));
    }

    @Test
    @Rollback
    public void testSendTemplate() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "Hieronymous");
        map.put("bar", "Basho");
        Email email = new Email();
        email.addToAddress(getTestUserEmail());
        email.setSubject("test");
        emailService.queueWithFreemarkerTemplate("test-email.ftl", map, email);
        sendEmailProcess.execute();
        assertTrue("expecting a mail in in the inbox", ((MockAwsEmailSenderServiceImpl) emailService.getAwsEmailService()).getMessages().size() > 0);
    }

    @Test
    @Rollback
    public void testBounceMailResponses() throws IOException, MessagingException {
        Email message = emailService.createMessage(EmailType.TEST_EMAIL, "bounce@simulator.amazonses.com");
        message.setSubject("Subject");
        message.setMessage("This is a test message");
        message.addData("foo", "foo");
        message.addData("bar", "bar");
        message.addData("firstName", "First");
        message.addData("lastName", "Last");

        SendRawEmailResult response = emailService.renderAndSendMessage(message);
        logger.debug("Response is {}", response);
    }

    @Test
    @Rollback
    public void testSendUserStats() throws MessagingException, IOException {
        TdarUser user = new TdarUser("Test", "User", getTestUserEmail());
        Long billingAccountId = 1L;
        BillingAccount billingAccount = genericService.find(BillingAccount.class, billingAccountId);

        emailService.generateAndSendUserStatisticEmail(user, billingAccount);
    }

    @Test
    @Rollback(false)
    public void testDequeuingUserStatsEmail() {
        TdarUser user = new TdarUser("Test", "User", getTestUserEmail());
        Long billingAccountId = 1L;
        BillingAccount billingAccount = genericService.find(BillingAccount.class, billingAccountId);
        Email email = emailService.generateUserStatisticsEmail(user, billingAccount);
        assertTrue("The message has a UUID", StringUtils.isNotBlank(email.getMessageUuid()));
        logger.debug("The email UUID is {}", email.getMessageUuid());
        emailService.queue(email);
        Long emailId = email.getId();
        email = null;

        Email message = genericService.find(Email.class, emailId);
        assertTrue("email id is not -1", emailId != -1);
        // assertTrue("Email doesn't have attachments", message.getInlineAttachments().size()==0);
        emailService.dequeue(message);

        assertNotNull("Email has content", message.getMessage());
        // logger.debug("The message content is : {}", message.getMessage());
        assertTrue("email has 3 attachments", message.getInlineAttachments().size() == 3);

        assertNotNull("The message has a UUID", message.getMessageUuid());
        logger.debug("The UUID is {}", message.getMessageUuid());

        try {
            emailService.sendAwsHtmlMessage(message);
        } catch (MessagingException | IOException e) {
            logger.debug("The message could not be sent: {} ", e);
        }
    }

    @Test
    @Rollback
    public void testCreateImages() {
        TdarUser user = new TdarUser("Test", "User", getTestUserEmail());
        Long billingAccountId = 1L;
        BillingAccount billingAccount = genericService.find(BillingAccount.class, billingAccountId);

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

        assertTrue("The piechart exists", piechart.exists());
        assertTrue("The barchart 1 exists", barchart1.exists());
        assertTrue("The barchart 2 exists", barchart2.exists());
    }

    @Test
    @Rollback
    public void testEmailContent() throws IOException {
        Email message = emailService.createMessage(EmailType.TEST_EMAIL, "bounce@simulator.amazonses.com");
        message.setSubject("Subject");
        message.setMessage("This is a test message");
        message.addData("foo", "foo");
        message.addData("bar", "bar");
        message.addData("firstName", "Brian");
        message.addData("lastName", "Castellanos");

        try {
            emailService.renderAndSendMessage(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendContactRequestEmail(EmailType type) {
        TdarUser from = new TdarUser("Test", "User", "test@tdar.org", "tdartest", getAdminUserId());
        TdarUser to = new TdarUser("Test", "User", getTestUserEmail(), "tdartest", getUserId());
        Resource resource = createAndSaveNewDataset();
        String subject = "";
        String messageBody = "This is a test message";
        Map<String, String[]> params = null;

        Email email = emailService.constructEmail(from, to, resource, subject, messageBody, type, params);

        email.setFrom("test@tdar.org");

        /**
         * Commented out because AWS is mocked, and it doesn't actually send message.
         * The status is actually set to IN_REVIEW.
         * 
         * Email received = emailService.dequeue(email);
         * received.setFrom(from.getEmail());
         * received.setTo(to.getEmail());
         * received.setStatus(Status.QUEUED);
         * genericService.saveOrUpdate(email);
         * 
         * sendEmailProcess.execute();
         * Email received = checkMailAndGetLatest(messageBody);
         **/

        assertNotNull("Expect the subject to be the same", email.getSubject());

        try {
            emailService.sendAwsHtmlMessage(email);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    public void processPermissionRequestEmail(EmailType emailType, String comment) {
        TdarUser authenticatedUser = getAdminUser();
        TdarUser requestor = new TdarUser("Test", "User", getTestUserEmail(), "tdartest", getUserId());
        Resource resource = createAndSaveNewDataset();
        boolean reject = emailType == EmailType.PERMISSION_REQUEST_REJECTED;
        Date expires = null;
        Permissions permission = Permissions.VIEW_ALL;

        Email email = emailService.proccessPermissionsRequest(requestor, resource, authenticatedUser, comment, reject, emailType, permission, expires);

        assertTrue("The message is the same", email.getMessage().contains(comment));
        assertEquals("The mesage is to the right person", requestor.getEmail(), email.getTo());

        try {
            emailService.sendAwsHtmlMessage(email);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getTestUserEmail() {
        return getTdarConfiguration().getDeveloperTestEmail();
    }
}
