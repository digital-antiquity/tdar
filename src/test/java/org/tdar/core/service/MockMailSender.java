package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class MockMailSender implements MailSender {
    private final ArrayList<SimpleMailMessage> messages = new ArrayList<SimpleMailMessage>();
    private Logger logger = LoggerFactory.getLogger(MockMailSender.class);

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        logger.debug("sending this message: \n{}", simpleMessage);
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