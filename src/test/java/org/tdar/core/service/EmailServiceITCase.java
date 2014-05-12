package org.tdar.core.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.util.Email;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.SendEmailProcess;

public class EmailServiceITCase extends AbstractIntegrationTestCase {

    @Autowired
    SendEmailProcess sendEmailProcess;
    
    @Test
    public void testMockMailSender() {
        Person to = new Person(null, null, "toguy@mailinator.com");
        String mailBody = "this is a message body";
        String subject = "this is a subject";
        Email email = new Email();
        email.addToAddress(to.getEmail());
        email.setMessage(mailBody);
        email.setSubject(subject);
        emailService.send(email);

        assertTrue("should have a mail in our 'inbox'", mockMailSender.getMessages().size() > 0);
        SimpleMailMessage received = mockMailSender.getMessages().get(0);

        assertEquals(received.getSubject(), subject);
        assertEquals(received.getText(), mailBody);
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo()[0], to.getEmail());
    }

    @Test
    public void testSendTemplate() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "Hieronymous");
        map.put("bar", "Basho");
        Email email = new Email();
        email.addToAddress("toguy@mailinator.com");
        email.setSubject("test");
        sendEmailProcess.setEmailService(emailService);
        emailService.queueWithFreemarkerTemplate("test-email.ftl", map, email);
        sendEmailProcess.execute();
        assertTrue("expecting a mail in in the inbox", mockMailSender.getMessages().size() > 0);

    }

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

}
