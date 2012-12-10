package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import org.tdar.core.bean.billing.BillingItem;
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

        // Account localAccount = account;
        for (Resource resource : resources) {
            // if the account is null -- die
            if (resource == null) {
                continue;
            }
            if (Persistable.Base.isNotNullOrTransient(account) && Persistable.Base.isNullOrTransient(resource.getAccount()) ||
                    account.getResources().contains(resource)) {
                continue;
            }

            if (Persistable.Base.isNullOrTransient(account) && Persistable.Base.isNullOrTransient(resource.getAccount())) {
                throw new TdarRecoverableRuntimeException(String.format("resource: %s is not assigned to an account", resource));
            }

            // if we're dealing with multiple accounts ... die
            if (Persistable.Base.isNotNullOrTransient(account) && !account.equals(resource.getAccount())) {
                throw new TdarRuntimeException(String.format("we don't yet support multiple accounts applied to a single action, %s and %s", account,
                        resource.getAccount()));
            }
        }
        getDao().merge(account);
        account.updateQuotas(endingEvaluator);
        account.getResources().addAll(resourcesToEvaluate);
        saveOrUpdate(account);
    }

    @Transactional
    public void updateTransientAccountInfo(Collection<Resource> resources) {
        getDao().updateTransientAccountOnResources(resources);
    }

    public BillingActivity getSpaceActivity() {
        for (BillingActivity activity : getActiveBillingActivities()) {
            if (activity.getNumberOfFiles() == null && activity.getNumberOfResources() == null && activity.getNumberOfMb() != null
                    && activity.getNumberOfMb() > 0) {
                return activity;
            }
        }
        return null;
    }

    public BillingItem getCheapestActivityByFiles(List<BillingItem> items, Long numFiles) {
        for (BillingActivity activity : getActiveBillingActivities()) {
            // 2 cases (1) exact value; (2) where the next step up might actually be cheaper
            int files = numFiles.intValue();
            if (activity.getMinAllowedNumberOfFiles() > numFiles) {
                files = activity.getMinAllowedNumberOfFiles().intValue();
            }
            BillingItem e = new BillingItem(activity, files);
            logger.info(" -- {}", e);
            items.add(e);
        }
        BillingItem lowest = null;
        for (BillingItem item : items) {
            if (lowest == null) {
                lowest = item;
            } else if (lowest.getSubtotal() > item.getSubtotal()) {
                lowest = item;
            }
        }
        return lowest;
    }

    public BillingItem getCheapestActivityBySpace(List<BillingItem> items, Long numFiles, Long spaceInMb) {
        for (BillingActivity activity : getActiveBillingActivities()) {
            if (activity.getMinAllowedNumberOfFiles() <= numFiles && activity.getNumberOfMb() * numFiles > spaceInMb) {
                items.add(new BillingItem(activity, numFiles.intValue()));
            }
        }
        BillingItem lowest = null;
        for (BillingItem item : items) {
            if (lowest == null) {
                lowest = item;
            } else if (lowest.getSubtotal() > item.getSubtotal()) {
                lowest = item;
            }
        }
        return lowest;
    }

    public BillingItem calculateCheapestActivities(Invoice invoice) {
        List<BillingItem> items = new ArrayList<BillingItem>();
        BillingItem lowest = getCheapestActivityByFiles(items, invoice.getNumberOfFiles());
        BillingItem lowest2 = getCheapestActivityBySpace(items, invoice.getNumberOfFiles(), invoice.getNumberOfMb());
        BillingItem lowestBySpace = null;
        BillingActivity spaceActivity = getSpaceActivity();
        if (spaceActivity != null) {
            Long spaceUsed = lowest.getQuantity() * lowest.getActivity().getNumberOfMb();
            spaceUsed -= invoice.getNumberOfMb();
            int qty = (int) Math.ceil(Math.abs(spaceUsed) / spaceActivity.getNumberOfMb());
            lowestBySpace = new BillingItem(spaceActivity, qty);
        }
        logger.info("lowest by files: {}", lowest);
        logger.info("lowest by space: {}", lowest2);
        logger.info("lowest combo: {}", lowestBySpace);
        return lowest;
    }

}
