package org.tdar.core.service;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.StatsResultObject;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.service.external.MockMailSender;

public class EmailServiceITCase extends AbstractIntegrationTestCase { 

	@Test
	@Rollback
	public void testQueueMessage(){
		
	}
	
	@Test
	@Rollback
	public void testMockMailSender() {
		Person to = new Person(null, null, "toguy@tdar.net");
		String mailBody = "this is a message body";
		String subject = "this is a subject";
		Email email = new Email();
		email.addToAddress(to.getEmail());
		email.setMessage(mailBody);
		email.setSubject(subject);
		emailService.send(email);

		SimpleMailMessage received = checkMailAndGetLatest(mailBody);

		assertEquals(received.getSubject(), subject);
		assertEquals(received.getText(), mailBody);
		assertEquals(received.getFrom(), emailService.getFromEmail());
		assertEquals(received.getTo()[0], to.getEmail());

		assertEquals(email.getStatus(), Status.SENT);
		// implicit assumption that something that is marked sent has a
		// sent-date
		assertThat(email.getDateSent(), is(not(nullValue())));
	}

	@Test
	public void testSendInviteEmail() {
		Person to = new Person("To", "Person", "bcastel1@asu.edu");
		Person from = new Person("From", "Somone", "toguy@tdar.net");
		TdarUser fromUser = new TdarUser(from, "from");
		UserInvite invite = new UserInvite();
		invite.setPerson(to);

		Resource project = createAndSaveNewProject("Test Project");
		assertEquals(project.getTitle(), "Test Project");

		invite.setResource(project);
		emailService.sendUserInviteEmail(invite, fromUser);
	}

	@Test
	@Rollback
	public void testSendTemplate() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("foo", "Hieronymous");
		map.put("bar", "Basho");
		Email email = new Email();
		email.addToAddress("toguy@tdar.net");
		email.setSubject("test");
		emailService.queueWithFreemarkerTemplate("test-email.ftl", map, email);
		sendEmailProcess.execute();
		assertTrue("expecting a mail in in the inbox",
				((MockMailSender) emailService.getMailSender()).getMessages().size() > 0);
	}

	@Test(expected = MessagingException.class)
	@Rollback
	public void testBounceMailResponses() throws IOException, MessagingException {
		Email message = emailService.createMessage(EmailType.TEST_EMAIL, "bounce@simulator.amazonses.com");
		message.setSubject("Subject");
		message.setMessage("This is a test message");
		message.addData("foo", "foo");
		message.addData("bar", "bar");
		message.addData("firstName", "Brian");
		message.addData("lastName", "Castellanos");

		emailService.renderAndUpdateEmailContent(message);
		emailService.updateEmailSubject(message);
		emailService.renderAndSendMessage(message);
	}

	@Test
	@Rollback
	public void testSendUserStats() throws MessagingException, IOException {
		TdarUser user = new TdarUser("Test", "User", "bcastel1@asu.edu");
		Long billingAccountId = 1L;
		BillingAccount billingAccount = genericService.find(BillingAccount.class, billingAccountId);

		emailService.sendUserStatisticEmail(user, billingAccount);
		// logger.debug("Email content is {}",message.getEmail().getMessage());
	}

	@Test
	@Rollback
	public void testCreateImages() {
		TdarUser user = new TdarUser("Test", "User", "bcastel1@asu.edu");
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
		getLogger().debug(message.getMessage());
	}

}
