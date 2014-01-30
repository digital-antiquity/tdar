package org.tdar.core.service.processes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static final String SUBJECT = "overdrawn accounts";

    private static final long serialVersionUID = 1198012881593988016L;

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
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("accounts", accounts);
        emailService.sendWithFreemarkerTemplate("overdrawn-admin.ftl", map, SUBJECT);
    }

    @Override
    public void process(Account account) throws Exception {
    }

    @Override
    public boolean isEnabled() {
        return TdarConfiguration.getInstance().isPayPerIngestEnabled();
    }
}
