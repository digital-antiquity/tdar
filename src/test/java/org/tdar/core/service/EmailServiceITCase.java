package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.service.external.EmailService;

import static org.junit.Assert.*;

public class EmailServiceITCase extends AbstractIntegrationTestCase {

    
    private EmailService emailService; 
    private MockMailSender mailSender = new MockMailSender();

    @Test
    public void testFreemarkerRendering() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("foo", "Hieronymous");
        map.put("bar", "Basho");
        String output = emailService.render("test.ftl", map);
        logger.debug("output: {}", output);
        assertTrue(output.contains("Hieronymous"));
        assertTrue(output.contains("Basho"));
    }
    
    @Test
    public void testMockMailSender() {
        emailService.setMailSender(mailSender);

    }
    
    @Autowired    
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    
    private static class MockMailSender implements MailSender {
        private final ArrayList<SimpleMailMessage> messages = new ArrayList<SimpleMailMessage>();

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            messages.add(simpleMessage);
        }

        @Override
        public void send(SimpleMailMessage[] simpleMessages) throws MailException {
            messages.addAll(Arrays.asList(simpleMessages));
        }

        /**
         * @return the messages
         */
        public ArrayList<SimpleMailMessage> getMessages() {
            return messages;
        }

    }
    
}
