package org.tdar.core.service.processes;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.ScheduledBatchProcess;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.external.EmailService;

@Component
public class OverdrawnAccountUpdate extends ScheduledBatchProcess<Account> {

    /**
     * 
     */
    private static final long serialVersionUID = 1198012881593988016L;
    @Autowired
    private EmailService emailService;

    public String getDisplayName() {
        return "Overdrawn Account Process";
    }

    @Override
    public int getBatchSize() {
        return 10000;
    }
    
    public Class<Account> getPersistentClass() {
        return Account.class;
    }

    @Override
    public List<Long> findAllIds() {
        List<Account> results = genericDao.findAllWithStatus(getPersistentClass(), Status.FLAGGED_ACCOUNT_BALANCE);
        if (CollectionUtils.isNotEmpty(results)) {
        return Persistable.Base.extractIds(results);
        } 
        return null;
    }

    @Override
    public void execute() {
        List<Account> accounts = genericDao.findAll(getPersistentClass(), getNextBatch());
        emailService.sendTemplate("overdrawn-admin.txt", accounts, "admin accounts");
    }

    @Override
    public void process(Account account) throws Exception {
    }

    @Override
    public boolean isEnabled() {
        return TdarConfiguration.getInstance().isPayPerIngestEnabled();
    }
}
