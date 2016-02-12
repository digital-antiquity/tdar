package org.tdar.core.service.processes.daily;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.Email.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledProcess;

/**
 * $Id$
 * 
 * ScheduledProcess to update aggregate stats
 * 
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */

@Component
@Scope("prototype")
public class DailyEmailProcess extends AbstractScheduledProcess {

    private static final long serialVersionUID = -1945086550963254634L;
    public TdarConfiguration config = TdarConfiguration.getInstance();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient EntityService entityService;

    @Override
    public String getDisplayName() {
        return "Daily emails";
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
    public void execute() {
        logger.info("adding email");
        sendQuarrantineEmail();
        sendNewUsersEmail();

        logger.info("done daily emails");
    }

    private void sendNewUsersEmail() {
        List<TdarUser> people = new ArrayList<>();
        Date yesterday = DateTime.now().minusDays(1).toDate();
        for (TdarUser user : entityService.findAllRegisteredUsers(100)) {
            if (user != null && user.getDateUpdated() != null && yesterday.before(user.getDateUpdated())) {
                people.add(user);
            }
        }

        if (CollectionUtils.isNotEmpty(people)) {
            Email email = new Email();
            email.setDate(new Date());
            email.setFrom(config.getDefaultFromEmail());
            email.setTo(config.getContactEmail());
            email.setSubject(String.format("There are %s new users in %s", people.size(), config.getSiteAcronym()));
            email.setUserGenerated(false);
            Map<String, Object> dataModel = initDataModel();
            dataModel.put("users", people);
            dataModel.put("totalUsers", people.size());
            emailService.queueWithFreemarkerTemplate("email_new_users.ftl", dataModel, email);
        }
    }


    private void sendQuarrantineEmail() {
        List<Email> emails = emailService.findEmailsWithStatus(Status.IN_REVIEW);
        if (CollectionUtils.isNotEmpty(emails)) {
            Map<String, Object> dataModel = initDataModel();
            dataModel.put("emails", emails);
            dataModel.put("totalEmails", emails.size());
            Email email = new Email();
            email.setUserGenerated(false);
            email.setDate(new Date());
            email.setFrom(config.getDefaultFromEmail());
            email.setTo(config.getContactEmail());
            email.setSubject(String.format("There are %s user emails to review ", emails.size()));
            emailService.queueWithFreemarkerTemplate("email_review_message.ftl", dataModel, email);
        }
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
