package org.tdar.core.service.external;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class MockMailSender implements JavaMailSender {
    private final ArrayList<SimpleMailMessage> messages = new ArrayList<SimpleMailMessage>();
    private Logger logger = LoggerFactory.getLogger(MockMailSender.class);

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        logger.debug("sending this message: \n{}", simpleMessage);
        messages.add(simpleMessage);
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        messages.addAll(Arrays.asList(simpleMessages));
    }

    /**
     * @return the messages
     */
    public ArrayList<SimpleMailMessage> getMessages() {
        return messages;
    }

    @Override
    public MimeMessage createMimeMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
        // TODO Auto-generated method stub

    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
        // TODO Auto-generated method stub

    }

}