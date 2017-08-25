package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.email.AwsEmailService;

public class AmazonEmailServiceITCase extends AbstractIntegrationTestCase {

    @Autowired 
    protected AwsEmailService awsEmailService;
    
    @Test
    public void testAwsCredentials(){
    	String accessKey = TdarConfiguration.getInstance().getAwsAccessKey();
    	String secretKey = TdarConfiguration.getInstance().getAwsSecretKey(); 
    	
    	getLogger().debug("Key: {} Pass:{}",accessKey,secretKey);
    	
    	assertNotNull(accessKey);
    	assertNotNull(secretKey);
    	
    }
    @Test
    public void testTestAwsEmail(){
    	assertEquals(EmailType.TEST_EMAIL.getFromAddress(), "test@tdar.org");
    }
    
    @Test
    @Rollback
    public void testSendAwsMail() throws IOException {
        AwsMessage message = awsEmailService.createMessage(EmailType.TEST_EMAIL, "bcastel1@asu.edu");
        message.getEmail().setSubject("Subject");
    	message.getEmail().setMessage("This is a test message");
    	message.addData("foo", "foo");
    	message.addData("bar", "bar");
    	message.addData("firstName", "Brian");
    	message.addData("lastName", "Castellanos");
		message.getAttachments().add(new File("src/test/resources/asu_map_tempe_2008.pdf"));

    	awsEmailService.renderAndUpdateEmailContent(message);
        awsEmailService.updateEmailSubject(message);
    	
        MimeMessage mimeMessage;
		try {
			mimeMessage = awsEmailService.createMimeMessage(message);
			awsEmailService.sendMultiPartMessage(mimeMessage);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
    }   
   
    @Test
    public void testEmailContent() throws IOException {    
        AwsMessage message = awsEmailService.createMessage(EmailType.TEST_EMAIL, "success@simulator.amazonses.com");
        message.getEmail().setSubject("Subject");
    	message.getEmail().setMessage("This is a test message");
    	message.addData("foo", "foo");
    	message.addData("bar", "bar");
    	message.addData("firstName", "Brian");
    	message.addData("lastName", "Castellanos");
		message.getAttachments().add(new File("src/test/resources/asu_map_tempe_2008.pdf"));

    	awsEmailService.renderAndUpdateEmailContent(message);
        awsEmailService.updateEmailSubject(message);
    	
        getLogger().debug(message.getEmail().getMessage());
    }
    
    @Test 
    public void testBounceMessage(){
    	try {
	    	AwsMessage message = awsEmailService.createMessage(EmailType.TEST_EMAIL,"bounce@simulator.amazonses.com");
	    	message.getEmail().setMessage("This is a bounce message");
	    	awsEmailService.updateEmailSubject(message);
	    	
    		MimeMessage mimeMessage = awsEmailService.createMimeMessage(message);
    		
    		ByteArrayOutputStream os = new ByteArrayOutputStream();
    		mimeMessage.writeTo(os);
    		byte[] mimeContents = os.toByteArray();
    		
    		OutputStream out = System.out;
    		
    		mimeMessage.writeTo(out);
    		assertNotEquals(mimeContents,"");
    		
    		awsEmailService.sendMultiPartMessage(mimeMessage);
    	}
    	catch(MessagingException | IOException e ){
    		fail(e.getMessage());
    	}
    }
   

}
