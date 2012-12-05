package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Account.AccountAdditionStatus;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.AccountDao;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;

@Transactional(readOnly = true)
@Service
public class AccountService extends ServiceInterface.TypedDaoBase<Account, AccountDao> {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private GenericDao genericDao;

    /*
     * Find all accounts for user: return accounts that are active and have not met their quota
     */
    public List<Account> listAvailableAccountsForUser(Person user) {
        if (Persistable.Base.isNullOrTransient(user)) {
            return Collections.EMPTY_LIST;
        }
        return getDao().findAccountsForUser(user);
    }

    public List<BillingActivity> getActiveBillingActivities() {
        List<BillingActivity> toReturn = new ArrayList<BillingActivity>();
        for (BillingActivity activity : genericDao.findAll(BillingActivity.class)) {
            if (activity.getEnabled()) {
                toReturn.add(activity);
            }
        }
        logger.info("{}", toReturn);
        return toReturn;

    }

    public void addResourceToAccount(Person user, Resource resource) {
        List<Account> accounts = listAvailableAccountsForUser(user);
        // if it doesn't count
        AccountAdditionStatus canAddResource = null;
        for (Account account : accounts) {
            canAddResource = account.canAddResource(resource);
            if (canAddResource == AccountAdditionStatus.CAN_ADD_RESOURCE) {
                account.getResources().add(resource);
                break;
            }
        }
        if (canAddResource != AccountAdditionStatus.CAN_ADD_RESOURCE) {
            throw new TdarRecoverableRuntimeException(String.format("Cannot add resource because %s", canAddResource));
        }
    }

    public AccountGroup getAccountGroup(Account account) {
        return getDao().getAccountGroup(account);
    }

    public boolean checkThatInvoiceBeAssigned(Invoice find, Account account) {
        if (account.getOwner().equals(find.getTransactedBy()) || account.getAuthorizedMembers().contains(find.getTransactedBy())) {
            return true;
        }
        throw new TdarRecoverableRuntimeException("cannot assign invoice to account");
    }

    public boolean hasSpaceInAnAccount(Person user) {
        for (Account account : listAvailableAccountsForUser(user)) {
            if (account.isActive() && account.hasMinimumForNewRecord())
                return true;
        }
        return false;
    }

    @Transactional
    public void updateQuota(ResourceEvaluator initialEvaluation, Account account, Resource... resources) {
        ResourceEvaluator endingEvaluator = new ResourceEvaluator(resources);
        endingEvaluator.subtract(initialEvaluation);
        List<Resource> resourcesToEvaluate = Arrays.asList(resources);
        getDao().updateTransientAccountOnResources(resourcesToEvaluate);
        // if the account is null ...

        Account localAccount = account;
        for (Resource resource : resources) {
            // if the account is null -- die
            if (Persistable.Base.isNullOrTransient(resource.getAccount())) {
                throw new TdarRecoverableRuntimeException(String.format("resource: %s is not assigned to an account", resource));
            }
            // if we're dealing with multiple accounts ... die
            if (Persistable.Base.isNotNullOrTransient(localAccount) && !localAccount.equals(resource.getAccount())) {
                throw new TdarRuntimeException(String.format("we don't yet support multiple accounts applied to a single action, %s and %s", localAccount,
                        resource.getAccount()));
            }
            localAccount = resource.getAccount();
        }

        account.updateQuotas(endingEvaluator);
        account.getResources().addAll(resourcesToEvaluate);
        genericDao.saveOrUpdate(account);
    }
}
