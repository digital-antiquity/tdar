package org.tdar.core.service.processes.daily;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledBatchProcess;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
public class OverdrawnAccountUpdate extends AbstractScheduledBatchProcess<BillingAccount> {
    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    public static final String SUBJECT = "overdrawn accounts";
    public static final String SUBJECT_USER = CONFIG.getSiteAcronym() + ": Overdrawn Account";

    private static final long serialVersionUID = 1198012881593988016L;

    @Autowired
    @Qualifier("genericDao")
    // this seems really weird to have @Autowired fields in beans...
    protected GenericDao genericDao;

    @Autowired
    private transient EmailService emailService;

    @Override
    public String getDisplayName() {
        return "Overdrawn Account Process";
    }

    @Override
    public int getBatchSize() {
        return 10000;
    }

    @Override
    public Class<BillingAccount> getPersistentClass() {
        return BillingAccount.class;
    }

    @Override
    public List<Long> findAllIds() {
        List<BillingAccount> results_ = genericDao.findAllWithStatus(getPersistentClass(), Status.FLAGGED_ACCOUNT_BALANCE);
        List<BillingAccount> results = new ArrayList<>();
        for (BillingAccount account : results_) {
            if (Days.daysBetween(new DateTime(account.getDateUpdated()).toLocalDate(), DateTime.now().toLocalDate()).getDays() == 1) {
                results.add(account);
            }
        }
        if (CollectionUtils.isNotEmpty(results)) {
            return PersistableUtils.extractIds(results);
        }
        return null;
    }

    @Override
    public void execute() {
        String adminEmail = getTdarConfiguration().getSystemAdminEmail();
        List<BillingAccount> accounts = genericDao.findAll(getPersistentClass(), getNextBatch());

        Email email = emailService.createMessage(EmailType.ADMIN_OVERDRAWN_NOTIFICATION, adminEmail);
        email.addData("accounts", accounts);
        email.setUserGenerated(false);

        logger.debug("sending admin email");
        emailService.renderAndQueueMessage(email);

        logger.debug("sending {} user email(s)", accounts.size());
        for (BillingAccount account : accounts) {
            // These messages should be going to the users, but there was no email address specified previously?
            Email email_ = emailService.createMessage(EmailType.OVERDRAWN_NOTIFICATION, adminEmail);
            email_.setUserGenerated(false);
            email_.addData("account", account);
            emailService.renderAndQueueMessage(email_);
        }
    }

    @Override
    public void process(BillingAccount account) throws Exception {
    }

    @Override
    public boolean isEnabled() {
        return TdarConfiguration.getInstance().isPayPerIngestEnabled();
    }
}
