package org.tdar.core.service;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.email.MockAwsEmailServiceImpl;
import org.tdar.core.service.external.MockMailSender;

public class EmailServiceITCase extends AbstractIntegrationTestCase {

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
        // implicit assumption that something that is marked sent has a sent-date
        assertThat(email.getDateSent(), is(not(nullValue())));
    }
    
    
    @Test
    public void testSendInviteEmail(){
    	 Person to 	   = new Person("To", "Person", "bcastel1@asu.edu");
    	 Person from 	   = new Person("From", "Somone", "toguy@tdar.net");
    	 TdarUser fromUser = new TdarUser(from, "from");
    	 UserInvite invite = new UserInvite();
    	 invite.setPerson(to);
    	 
    	 Resource project = createAndSaveNewProject("Test Project");
    	 assertEquals(project.getTitle(), "Test Project");
    	 
    	 invite.setResource(project);
    	 emailService.sendUserInviteEmail(invite, fromUser);
    }

    @Test
    public void testSendTemplate() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "Hieronymous");
        map.put("bar", "Basho");
        Email email = new Email();
        email.addToAddress("toguy@tdar.net");
        email.setSubject("test");
        emailService.queueWithFreemarkerTemplate("test-email.ftl", map, email);
        sendEmailProcess.execute();
        assertTrue("expecting a mail in in the inbox", ((MockMailSender) emailService.getMailSender()).getMessages().size() > 0);
    }
    
    
    @Test
    @Rollback
    public void testSendAwsMail() throws IOException {
        AwsMessage message = emailService.createMessage(EmailType.TEST_EMAIL, "bcastel1@asu.edu");
        message.getEmail().setSubject("Subject");
    	message.getEmail().setMessage("This is a test message");
    	message.addData("foo", "foo");
    	message.addData("bar", "bar");
    	message.addData("firstName", "Brian");
    	message.addData("lastName", "Castellanos");
		
    	//message.getAttachments().add(new File("src/test/resources/asu_map_tempe_2008.pdf"));

    	emailService.renderAndUpdateEmailContent(message);
    	emailService.updateEmailSubject(message);
    	
		try {
			emailService.renderAndSendMessage(message);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
    }   
    
    
    @Test
    public void testSendUserStats() throws MessagingException, IOException{
    		TdarUser user = new TdarUser("Test", "User", getTdarConfiguration().getDefaultFromEmail());
    	
    		Long billingAccountId = 418L;
    		BillingAccount billingAccount = genericService.find(BillingAccount.class, billingAccountId);
    		Map<String, Number> pieChartData = emailStatsHelper.generateUserResourcesPieChartData(billingAccount);
    		
    		emailStatsHelper.generateTotalDownloadsChartData(billingAccount);
    		
    		
    		AwsMessage message = emailService.createMessage(EmailType.MONTHLY_USER_STATISTICS, "bcastel1@asu.edu");
    		message.addData("resources",billingAccount.getResources());
    		message.addData("user", user);
    		
    		
    	
    		//emailService.renderAndSendMessage(message);
        	emailService.renderAndUpdateEmailContent(message);
        	emailService.updateEmailSubject(message);
        	
        	logger.debug("Email content is {}",message.getEmail().getMessage());
    }
    
   
    @Test
    public void testEmailContent() throws IOException {    
        AwsMessage message = emailService.createMessage(EmailType.TEST_EMAIL, "success@simulator.amazonses.com");
        message.getEmail().setSubject("Subject");
    	message.getEmail().setMessage("This is a test message");
    	message.addData("foo", "foo");
    	message.addData("bar", "bar");
    	message.addData("firstName", "Brian");
    	message.addData("lastName", "Castellanos");

    	emailService.renderAndUpdateEmailContent(message);
    	emailService.updateEmailSubject(message);
        getLogger().debug(message.getEmail().getMessage());
    }
    

    @Test
    public void testAwsMockObject(){
    	assertTrue(emailService.getAwsEmailService() instanceof MockAwsEmailServiceImpl);
    }
    
}
