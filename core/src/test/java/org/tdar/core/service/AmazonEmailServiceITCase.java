package org.tdar.core.service;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.service.email.AwsEmailService;
import org.tdar.core.service.external.MockMailSender;

import com.amazonaws.services.simpleemail.model.SendEmailResult;

public class AmazonEmailServiceITCase extends AbstractIntegrationTestCase {

    @Autowired 
    protected AwsEmailService awsEmailService;
    
    @Test
    @Rollback
    public void testMockMailSender() {
    	String toEmailAddress = "bcastel1@asu.edu";
        Person to = new Person(null, null, toEmailAddress);
        String mailBody = "this is a message body";
        String subject = "this is a subject";
        
        Email email = new Email();
        email.setMessage(mailBody);
        email.setSubject(subject);
        email.addToAddress(to.getEmail());
        assertEquals(toEmailAddress, email.getTo());
        
        getLogger().debug("To Email is: {}",email.getTo());
        SendEmailResult result = awsEmailService.sendMessage(email);
        
        assertNotNull(result.getMessageId());
    }
   

}
