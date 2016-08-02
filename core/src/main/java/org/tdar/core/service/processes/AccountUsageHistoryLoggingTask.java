package org.tdar.core.service.processes;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.AccountUsageHistory;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.PersistableUtils;

/**
 * Logs account usage history.
 * 
 * @author abrin
 *
 */
@Component
@Scope("prototype")
public class AccountUsageHistoryLoggingTask extends AbstractScheduledBatchProcess<BillingAccount> {

    private static final long serialVersionUID = -6773975693075576122L;

    @Override
    public String getDisplayName() {
        return "Account History Logging Process";
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
        List<BillingAccount> results = genericDao.findAll(getPersistentClass());
        if (CollectionUtils.isNotEmpty(results)) {
            return PersistableUtils.extractIds(results);
        }
        return null;
    }

    @Override
    public void process(BillingAccount account) throws Exception {
        AccountUsageHistory history = new AccountUsageHistory(account);
        account.getUsageHistory().add(history);
        genericDao.saveOrUpdate(account);
        genericDao.saveOrUpdate(history);
    }

    @Override
    public boolean isEnabled() {
        return TdarConfiguration.getInstance().isPayPerIngestEnabled();
    }
}
