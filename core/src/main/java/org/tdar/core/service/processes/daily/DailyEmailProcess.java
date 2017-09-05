package org.tdar.core.service.processes.daily;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.notification.Status;
import org.tdar.core.bean.notification.aws.AwsMessage;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.email.AwsEmailService;
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

    public static final int NEW_USER_MAX_RESULTS = 100;

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
        // get user accounts updated since yesterday @ midnight
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();


        List<TdarUser> people = entityService.findAllRegisteredUsers(NEW_USER_MAX_RESULTS).stream()
                // remove empty user objects
                .filter(Objects::nonNull)
                // remove users registered prior to yesterday
                .filter(user -> yesterday.isBefore(LocalDateTime.ofInstant(user.getDateUpdated().toInstant(), ZoneId.systemDefault())))
                // collect them into a list
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(people)) {
        	AwsMessage message = emailService.createMessage(EmailType.ADMIN_NEW_USER_REPORT, config.getContactEmail());
        	message.setMap(initDataModel());
        	message.addData("users", people);
            message.addData("totalUsers", people.size());
            message.getEmail().setUserGenerated(false);
        	message.getEmail().setDate(new Date());
        	
        	emailService.queueAwsMessage(message);
        }
    }


    private void sendQuarrantineEmail() {
        List<Email> emails = emailService.findEmailsWithStatus(Status.IN_REVIEW);
        if (CollectionUtils.isNotEmpty(emails)) {
        	AwsMessage message = emailService.createMessage(EmailType.ADMIN_QUARANTINE_REVIEW, config.getContactEmail());
        	message.setMap(initDataModel());
        	message.addData("emails", emails);
        	message.addData("totalEmails",emails.size());
        	message.getEmail().setUserGenerated(false);
        	message.getEmail().setDate(new Date());
        	
        	emailService.queueAwsMessage(message);
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
