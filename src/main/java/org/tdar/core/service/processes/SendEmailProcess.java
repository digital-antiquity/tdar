package org.tdar.core.service.processes;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.util.Email;
import org.tdar.core.bean.util.Email.Status;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.service.external.EmailService;

/**
 * $Id$
 * 
 * ScheduledProcess to rebuild geographic keyword and resource count caches.
 * 
 * How often should this be run?
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

@Component
public class SendEmailProcess extends ScheduledBatchProcess<Email> {

    private static final long serialVersionUID = 6711790499277412427L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient EmailService emailService;

    @Override
    public String getDisplayName() {
        return "Send Email Process";
    }

    @Override
    public Class<Email> getPersistentClass() {
        return Email.class;
    }

    @Override
    public boolean isSingleRunProcess() {
        return false;
    }

    @Override
    public boolean shouldRunAtStartup() {
        return true;
    }

    @Override
    public void process(Email email) {
        DateTime dt = new DateTime(email.getDate());
        if (email.getStatus() == Status.SENT && dt.isBefore(DateTime.now().minusDays(7))) {
            genericDao.delete(email);
        }
        emailService.send(email);
    }

    /**
     * This ScheduledProcess is finished to completion after execute().
     */
    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
