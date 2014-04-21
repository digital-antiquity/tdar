package org.tdar.utils;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author abrin gracefully borrowed from
 *         http://lajosd.blogspot.com/2009/09/log4j
 *         -smtpappender-exception-info-in.html
 */
public class PatternSubjectSMTPAppender extends SMTPAppender {

    Logger logger = Logger.getLogger(getClass());

    @Override
    protected void sendBuffer() {

        // Note: this code already owns the monitor for this
        // appender. This frees us from needing to synchronize on 'cb'.
        try {
            MimeBodyPart part = new MimeBodyPart();
            String host = "UNKNOWN";
            try {
                InetAddress localMachine = InetAddress.getLocalHost();
                host = localMachine.getHostName();
            } catch (Exception uhe) { // [beware typo in
                // handle exception
            }

            StringBuffer sbuf = new StringBuffer();
            sbuf.append("this ERROR occurred on: " + host + "\r\n");

            String t = layout.getHeader();
            if (t != null) {
                sbuf.append(t);
            }
            int len = cb.length();
            for (int i = 0; i < len; i++) {
                // sbuf.append(MimeUtility.encodeText(layout.format(cb.get())));
                LoggingEvent event = cb.get();

                // setting the subject
                if (i == 0) {
                    Layout subjectLayout = new PatternLayout(getSubject());
                    msg.setSubject(MimeUtility.encodeText(
                            subjectLayout.format(event), "UTF-8", null));
                }

                sbuf.append(layout.format(event));
                if (layout.ignoresThrowable()) {
                    String[] s = event.getThrowableStrRep();
                    if (s != null) {
                        for (int j = 0; j < s.length; j++) {
                            sbuf.append(s[j]);
                            sbuf.append(Layout.LINE_SEP);
                        }
                    }
                }
            }
            t = layout.getFooter();
            if (t != null) {
                sbuf.append(t);
            }
            part.setContent(sbuf.toString(), layout.getContentType());

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(part);
            msg.setContent(mp);
            super.addressMessage(msg);
            // msg.setFrom(getAddress(getFrom()));
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (MessagingException e) {
            logger.trace(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void activateOptions() {
        InetAddress localMachine;
        try {
            localMachine = InetAddress.getLocalHost();
            String addr = "tdar-error@";
            // + localMachine.getHostName();
            if (!StringUtils.isEmpty(getFrom())) {
                if (getFrom().indexOf("@") != -1) {
                    addr = getFrom().substring(0, getFrom().indexOf("@"));
                } else {
                    addr = getFrom();
                    return;
                }
            } else {
                addr += localMachine.getHostName();
            }
            LogLog.warn("email logging using: " + addr);
            super.setFrom(addr);
            super.activateOptions();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}