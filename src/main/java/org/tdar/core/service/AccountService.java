package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Account.AccountAdditionStatus;
import org.tdar.core.bean.billing.BillingActivity.BillingActivityType;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.AccountDao;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.exception.TdarQuotaException;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.data.PricingOption;
import org.tdar.struts.data.PricingOption.PricingType;

@Transactional(readOnly = true)
@Service
public class AccountService extends ServiceInterface.TypedDaoBase<Account, AccountDao> {

    public static final String ACCOUNT_IS_NULL = "account is null";
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private GenericDao genericDao;

    @Autowired
    ResourceService resourceService;

    /*
     * Find all accounts for user: return accounts that are active and have not met their quota
     */
    public Set<Account> listAvailableAccountsForUser(Person user) {
        if (Persistable.Base.isNullOrTransient(user)) {
            return Collections.emptySet();
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
        Collections.sort(toReturn);
        logger.trace("{}", toReturn);
        return toReturn;

    }

    public BillingActivityModel getLatestActivityModel() {
        List<BillingActivityModel> findAll = getDao().findAll(BillingActivityModel.class);
        BillingActivityModel latest = null;
        for (BillingActivityModel model : findAll) {
            if (!model.getActive())
                continue;
            if (latest == null || latest.getVersion() == null || model.getVersion() > latest.getVersion()) {
                latest = model;
            }
        }
        return latest;
    }

    public ResourceEvaluator getResourceEvaluator() {
        return getResourceEvaluator(new Resource[0]);
    }

    public ResourceEvaluator getResourceEvaluator(Resource... resources) {
        return new ResourceEvaluator(getLatestActivityModel(), resources);
    }

    public ResourceEvaluator getResourceEvaluator(Collection<Resource> resources) {
        return new ResourceEvaluator(getLatestActivityModel(), resources.toArray(new Resource[0]));
    }

    /*
     * public void addResourceToAccount(Person user, Resource resource) {
     * Set<Account> accounts = listAvailableAccountsForUser(user);
     * // if it doesn't count
     * AccountAdditionStatus canAddResource = null;
     * for (Account account : accounts) {
     * ResourceEvaluator resourceEvaluator = getResourceEvaluator(resource);
     * canAddResource = account.canAddResource(resourceEvaluator);
     * if (canAddResource == AccountAdditionStatus.CAN_ADD_RESOURCE) {
     * account.updateQuotas(resourceEvaluator);
     * break;
     * }
     * }
     * if (canAddResource != AccountAdditionStatus.CAN_ADD_RESOURCE) {
     * throw new TdarQuotaException(Account.ACCOUNT_IS_OVERDRAWN, canAddResource);
     * }
     * }
     */

    public AccountGroup getAccountGroup(Account account) {
        return getDao().getAccountGroup(account);
    }

    public boolean checkThatInvoiceBeAssigned(Invoice find, Account account) {
        if (account.getOwner().equals(find.getTransactedBy()) || account.getAuthorizedMembers().contains(find.getTransactedBy())) {
            return true;
        }
        throw new TdarRecoverableRuntimeException("cannot assign invoice to account");
    }

    public boolean hasSpaceInAnAccount(Person user, ResourceType type) {
        for (Account account : listAvailableAccountsForUser(user)) {
            logger.trace("evaluating account {}", account.getName());
            if (account.isActive() && account.hasMinimumForNewRecord(getResourceEvaluator(), type)) {
                logger.info("account '{}' has minimum balance for {}", account.getName(), user.getProperName());
                return true;
            }
        }
        // logger.info("user {} has no accounts or balance", user.getProperName());
        return false;
    }

    @Transactional
    public void markResourcesAsFlagged(Collection<Resource> resources) {
        for (Resource resource : resources) {
            resource.setPreviousStatus(resource.getStatus());
            resource.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
            resourceService.logResourceModification(resource, resource.getUpdatedBy(),
                    String.format("changed status from %s to Flagged_billing", resource.getStatus()));
            resourceService.saveRecordToFilestore(resource);
        }
        saveOrUpdateAll(resources);

    }

    @Transactional
    public void unMarkResourcesAsFlagged(Collection<Resource> resources) {
        for (Resource resource : resources) {
            if(resource.getStatus() != Status.FLAGGED_ACCOUNT_BALANCE) continue;
            Status status = resource.getPreviousStatus();
            if (status == null) {
                status = Status.ACTIVE;
            }
            resource.setStatus(status);
            resource.setPreviousStatus(null);
            resourceService.logResourceModification(resource, resource.getUpdatedBy(),
                    String.format("resetting status to %s from Flagged_billing", resource.getStatus()));
            resourceService.saveRecordToFilestore(resource);
        }
        saveOrUpdateAll(resources);
    }

    @Transactional
    public AccountAdditionStatus updateQuota(ResourceEvaluator initialEvaluation, Account account, boolean logModification, Resource... resources) {
        return updateQuota(initialEvaluation, account, Arrays.asList(resources), logModification);
    }

    @Transactional
    public void updateQuotaAndResetResourceStatus(Account account) {
        if (!account.isOverdrawn(getResourceEvaluator())) {
            throw new TdarQuotaException("account not charged enough", null);
        } else {
            List<Resource> resources = new ArrayList<Resource>();

            for (Resource resource : account.getResources()) {
                if (resource.getStatus() == Status.FLAGGED_ACCOUNT_BALANCE) {
                    resource.setStatus(resource.getPreviousStatus());
                    resource.setPreviousStatus(null);
                    resources.add(resource);
                }
            }
            saveOrUpdateAll(resources);
        }
    }

    @Transactional
    public AccountAdditionStatus updateQuota(ResourceEvaluator initialEvaluation, Account account, List<Resource> resourcesToEvaluate, boolean logModification) {
        logger.info("updating quota(s)");
        if (account == null) {
            throw new TdarRecoverableRuntimeException(ACCOUNT_IS_NULL);
        }
        ResourceEvaluator endingEvaluator = getResourceEvaluator(resourcesToEvaluate);
        endingEvaluator.subtract(initialEvaluation);
        getDao().updateTransientAccountOnResources(resourcesToEvaluate);
        // if the account is null ...

        // Account localAccount = account;
        for (Resource resource : resourcesToEvaluate) {
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
        AccountAdditionStatus status = AccountAdditionStatus.CAN_ADD_RESOURCE;
        try {
            // account.getResources().addAll(resourcesToEvaluate);
            account.updateQuotas(endingEvaluator, resourcesToEvaluate);
        } catch (TdarQuotaException e) {
            status = e.getCode();
            if (logModification)
                markResourcesAsFlagged(resourcesToEvaluate);
            logger.info("marking {} resources {} FLAGGED", status, resourcesToEvaluate);
        }
        if (status == AccountAdditionStatus.CAN_ADD_RESOURCE && logModification) {
            unMarkResourcesAsFlagged(resourcesToEvaluate);
        }
        saveOrUpdate(account);
        return status;
    }

    @Transactional
    public void updateTransientAccountInfo(Collection<Resource> resources) {
        getDao().updateTransientAccountOnResources(resources);
    }

    public BillingActivity getSpaceActivity() {
        for (BillingActivity activity : getActiveBillingActivities()) {
            if (activity.getActivityType() == BillingActivityType.TEST) {
                continue;
            }
            if ((activity.getNumberOfFiles() == null || activity.getNumberOfFiles() == 0)
                    && (activity.getNumberOfResources() == null || activity.getNumberOfResources() == 0) && activity.getNumberOfMb() != null
                    && activity.getNumberOfMb() > 0) {
                return activity;
            }
        }
        return null;
    }

    public PricingOption getCheapestActivityByFiles(Long numFiles, Long numMb, boolean exact) {
        PricingOption option = new PricingOption(PricingType.SIZED_BY_FILE_ONLY);
        if (!exact) {
            option.setType(PricingType.SIZED_BY_FILE_ABOVE_TIER);
        }
        List<BillingItem> items = new ArrayList<BillingItem>();
        logger.info("files: {} mb: {}", numFiles, numMb);

        for (BillingActivity activity : getActiveBillingActivities()) {
            int calculatedNumberOfFiles = numFiles.intValue();             //Don't use test activities or activities that are Just about MB
            if (activity.getActivityType() == BillingActivityType.TEST || !activity.supportsFileLimit()) {
                continue;
            }
            logger.trace("n:{} min:{}", numFiles, activity.getMinAllowedNumberOfFiles());
            if (exact && numFiles < activity.getMinAllowedNumberOfFiles())
                continue;

            // 2 cases (1) exact value; (2) where the next step up might actually be cheaper
            if (!exact && activity.getMinAllowedNumberOfFiles() >= numFiles) {
                calculatedNumberOfFiles = activity.getMinAllowedNumberOfFiles().intValue();
            }


            BillingItem e = new BillingItem(activity, calculatedNumberOfFiles);
            logger.trace(" -- {} ({})", e.getActivity().getName(), e);
            items.add(e);
        }
        logger.trace("{} {}", option , items);
        // finding the cheapest
        BillingItem lowest = null;
        for (BillingItem item : items) {
            if (lowest == null) {
                lowest = item;
            } else if (lowest.getSubtotal() > item.getSubtotal()) {
                lowest = item;
            }
        }
        option.getItems().add(lowest);
        BillingActivity spaceActivity = getSpaceActivity();
        if (lowest == null) {
            logger.error("no options found for f:{} m:{} ", numFiles, numMb);
            return null;
        }
        Long spaceAvailable = lowest.getQuantity() * lowest.getActivity().getNumberOfMb();
        Long spaceNeeded = numMb - spaceAvailable;
        logger.info("adtl. space needed: {} avail: {} ", spaceNeeded, spaceAvailable);
        logger.info("space act: {} ", getSpaceActivity());
        calculateSpaceActivity(option, spaceActivity, spaceNeeded);
        return option;
    }

    public void calculateSpaceActivity(PricingOption option, BillingActivity spaceActivity, Long spaceNeeded) {
        BillingItem extraSpace;
        if (spaceNeeded > 0 && spaceActivity != null) {
            int qty = (int) Account.divideByRoundUp(spaceNeeded, spaceActivity.getNumberOfMb());
            extraSpace = new BillingItem(spaceActivity, qty);
            option.getItems().add(extraSpace);
        }
    }

    public PricingOption getCheapestActivityBySpace(Long numFiles, Long spaceInMb) {
        PricingOption option = new PricingOption(PricingType.SIZED_BY_MB);
        List<BillingItem> items = new ArrayList<BillingItem>();
        BillingActivity spaceActivity = getSpaceActivity();
        if (spaceActivity != null && (numFiles == null || numFiles.intValue() == 0)) {
            calculateSpaceActivity(option, spaceActivity, spaceInMb);
            return option;
        }

        for (BillingActivity activity : getActiveBillingActivities()) {
            if (activity.getActivityType() == BillingActivityType.TEST) {
                continue;
            }

            if (activity.supportsFileLimit()) {
                Long total = Account.divideByRoundUp(spaceInMb, activity.getNumberOfMb());
                Long minAllowedNumberOfFiles = activity.getMinAllowedNumberOfFiles();
                if (minAllowedNumberOfFiles == null) {
                    minAllowedNumberOfFiles = 0L;
                }

                if (total * activity.getNumberOfFiles() < minAllowedNumberOfFiles) {
                    total = minAllowedNumberOfFiles;
                }

                if (total < numFiles / activity.getNumberOfFiles()) {
                    total = numFiles;
                }
                items.add(new BillingItem(activity, total.intValue()));
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
        option.getItems().add(lowest);
        return option;
    }

    public PricingOption calculateCheapestActivities(Invoice invoice) {
        PricingOption lowestByMB = getCheapestActivityBySpace(invoice.getNumberOfFiles(), invoice.getNumberOfMb());
        PricingOption lowestByFiles = getCheapestActivityByFiles(invoice.getNumberOfFiles(), invoice.getNumberOfMb(), false);

        // If we are using the ok amount of space for that activity...
        logger.info("lowest by files: {}", lowestByFiles.getSubtotal());
        logger.info("lowest by space: {} ", lowestByMB.getSubtotal());
        if (lowestByFiles.getSubtotal() < lowestByMB.getSubtotal()) {
            return lowestByFiles;
        }
        return lowestByMB;
    }

    public PricingOption calculateActivities(Invoice invoice, PricingType pricingType) {
        switch (pricingType) {
            case SIZED_BY_FILE_ABOVE_TIER:
                return getCheapestActivityBySpace(invoice.getNumberOfFiles(), invoice.getNumberOfMb());
            case SIZED_BY_FILE_ONLY:
                return getCheapestActivityByFiles(invoice.getNumberOfFiles(), invoice.getNumberOfMb(), false);
            case SIZED_BY_MB:
                return getCheapestActivityBySpace(invoice.getNumberOfFiles(), invoice.getNumberOfMb());
        }
        return null;
    }

}
