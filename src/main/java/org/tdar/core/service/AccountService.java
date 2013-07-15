package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import org.tdar.core.bean.billing.BillingActivity.BillingActivityType;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.AccountDao;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.data.PricingOption;
import org.tdar.struts.data.PricingOption.PricingType;
import org.tdar.utils.AccountEvaluationHelper;

@Transactional(readOnly = true)
@Service
public class AccountService extends ServiceInterface.TypedDaoBase<Account, AccountDao> {

    public static final String SPECIFY_EITHER_SPACE_OR_FILES = "Specify either Space or Files for your coupon";
    public static final String COUPON_HAS_EXPIRED = "Coupon has expired";
    public static final String CANNOT_GENERATE_A_COUPON_FOR_NOTHING = "cannot generate a coupon for nothing";
    public static final String NOT_ENOUGH_SPACE_OR_FILES = "cannot create coupon for value greater than account's current available files or MB";
    private static final String COUPON_ALREADY_APPLIED = "Coupon already applied";
    private static final String CANNOT_REDEEM_COUPON = "Cannot redeem coupon";
    public static final String ACCOUNT_IS_NULL = "account is null";
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private GenericDao genericDao;

    @Autowired
    ResourceService resourceService;

    @Autowired
    AuthenticationAndAuthorizationService authService;

    /*
     * Find all accounts for user: return accounts that are active and have not met their quota
     */
    public Set<Account> listAvailableAccountsForUser(Person user, Status... statuses) {
        if (Persistable.Base.isNullOrTransient(user)) {
            return Collections.emptySet();
        }
        return getDao().findAccountsForUser(user, statuses);
    }

    /*
     * Find all accounts for user: return accounts that are active and have not met their quota
     */
    public List<Invoice> listUnassignedInvoicesForUser(Person user) {
        if (Persistable.Base.isNullOrTransient(user)) {
            return Collections.emptyList();
        }
        return getDao().findUnassignedInvoicesForUser(user);
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

    public AccountGroup getAccountGroup(Account account) {
        return getDao().getAccountGroup(account);
    }

    public boolean checkThatInvoiceBeAssigned(Invoice find, Account account) {

        if (authService.isMember(find.getTransactedBy(), TdarGroup.TDAR_BILLING_MANAGER)) {
            return true;
        }

        if (account.getOwner().equals(find.getTransactedBy()) || account.getAuthorizedMembers().contains(find.getTransactedBy())) {
            return true;
        }
        throw new TdarRecoverableRuntimeException("cannot assign invoice to account");
    }

    @Transactional
    public boolean hasSpaceInAnAccount(Person user, ResourceType type, boolean createAccountIfNeeded) {
        Set<Account> accounts = listAvailableAccountsForUser(user);
        for (Account account : accounts) {
            logger.trace("evaluating account {}", account.getName());
            if (account.isActive() && account.hasMinimumForNewRecord(getResourceEvaluator(), type)) {
                logger.info("account '{}' has minimum balance for {}", account.getName(), user.getProperName());
                return true;
            }
        }

        Account account = null;
        if (createAccountIfNeeded) {
            List<Invoice> unassignedInvoices = listUnassignedInvoicesForUser(user);
            logger.info("unassigned invoices: {} ", unassignedInvoices);
            if (CollectionUtils.isNotEmpty(unassignedInvoices)) {
                if (CollectionUtils.isNotEmpty(accounts) && accounts.size() == 1) {
                    account = accounts.iterator().next();
                } else {
                    account = new Account();
                    account.setName("Generated account for " + user.getProperName());
                    account.markUpdated(user);
                    genericDao.saveOrUpdate(account);
                }

                for (Invoice invoice : unassignedInvoices) {
                    account.getInvoices().add(invoice);
                }
                genericDao.saveOrUpdate(account);
                return true;
            }
        }
        return false;
    }

    @Transactional
    protected void markResourcesAsFlagged(Collection<Resource> resources) {
        for (Resource resource : resources) {
            if (!getResourceEvaluator().getUncountedResourceStatuses().contains(resource.getStatus())) {
                resource.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
            }
        }

    }

    @Transactional
    protected void unMarkResourcesAsFlagged(Collection<Resource> resources) {
        for (Resource resource : resources) {
            if (resource.getStatus() != Status.FLAGGED_ACCOUNT_BALANCE)
                continue;
            Status status = resource.getPreviousStatus();
            if (status == null) {
                status = Status.ACTIVE;
            }
            resource.setStatus(status);
        }
    }

    @Transactional
    public AccountAdditionStatus updateQuota(Account account, Resource... resources) {
        return updateQuota(account, Arrays.asList(resources));
    }

    @Transactional
    public void updateAccountInfo(Account account) {
        getDao().updateAccountInfo(account,getResourceEvaluator());
    }

    @Transactional
    public AccountAdditionStatus updateQuota(Account account, Collection<Resource> resourcesToEvaluate) {
        logger.info("updating quota(s) {} {}", account, resourcesToEvaluate);
        logger.trace("model {}", getLatestActivityModel());

        if (Persistable.Base.isNullOrTransient(account)) {
            throw new TdarRecoverableRuntimeException(ACCOUNT_IS_NULL);
        }
        /* evaluate resources based on the model, and update their counts of files and space */
        ResourceEvaluator resourceEvaluator = getResourceEvaluator(resourcesToEvaluate);
        saveOrUpdateAll(resourcesToEvaluate);

        /* make sure the account associations are properly set for each resource in the bunch */
        getDao().updateTransientAccountOnResources(resourcesToEvaluate);
        AccountEvaluationHelper helper = new AccountEvaluationHelper(account, getLatestActivityModel());

        /* check if any of the resources have been modified (ie. resource.markUpdated() has been called */
        boolean hasUpdates = updateAccountAssociations(account, resourcesToEvaluate, helper);

        /* update the account info in the database */
        getDao().updateAccountInfo(account,resourceEvaluator);
        AccountAdditionStatus status = AccountAdditionStatus.CAN_ADD_RESOURCE;

        account.initTotals();
        helper.updateFromAccount(account);
        logAccountAndHelperState(account, helper);

        boolean overdrawn = account.isOverdrawn(getResourceEvaluator());
        logger.info("overdrawn: {}", overdrawn);

        /*
         * 
         */
        if (!hasUpdates || overdrawn) {
            /*
             * If we don't have anything to update (no resource has been marked as "changed" or the account has been overdrawn, then we need to start from
             * scratch with this account. We set it back to the normal state, and we re-evaluate ALL of the resources in the account
             */
            resourcesToEvaluate = account.getResources();

            // start at 0 and re-add everything
            // sort by date updated
            account.setStatus(Status.ACTIVE);
            account.setSpaceUsedInBytes(0L);
            account.setFilesUsed(0L);
            account.initTotals();

            for (Coupon coupon : account.getCoupons()) {
                account.setFilesUsed(coupon.getNumberOfFiles() + account.getFilesUsed());
                account.setSpaceUsedInBytes(coupon.getNumberOfMb() * Coupon.ONE_MB + account.getSpaceUsedInBytes());
            }

            helper = new AccountEvaluationHelper(account, getLatestActivityModel());
            // we have to evaluate everything so we can make sure that the transient boolean is set for deleted materials
            getResourceEvaluator().evaluateResources(resourcesToEvaluate);
            logger.info("s{} f{} r:{} ", account.getAvailableSpaceInBytes(), account.getAvailableNumberOfFiles(), helper.getUnflagged());
            processResourcesChronologically(helper, resourcesToEvaluate);

            status = updateResourceStatusesAndReconcileAccountStatus(helper, status);
            overdrawn = account.isOverdrawn(getResourceEvaluator());
            if (CollectionUtils.isNotEmpty(helper.getFlagged()) || overdrawn) {
                account.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
                logger.info("marking account as FLAGGED {} {}", overdrawn, helper.getFlagged());
            } else {
                account.setStatus(Status.ACTIVE);
            }

            saveOrUpdateAll(resourcesToEvaluate);
            helper.updateAccount();
            updateAccountInfo(account);
        }

        saveOrUpdate(account);
        logger.trace("files used: {} ", account.getFilesUsed());
        logger.trace("files avail: {} ", account.getAvailableNumberOfFiles());
        logger.trace("space used: {} ", account.getSpaceUsedInMb());
        logger.trace("space avail: {} ", account.getAvailableSpaceInMb());
        return status;
    }

    private void logAccountAndHelperState(Account account, AccountEvaluationHelper helper) {
        Object[] log = { account.getSpaceUsedInBytes(), account.getAvailableSpaceInBytes(), account.getFilesUsed(), account.getAvailableNumberOfFiles() };
        logger.info("ACCOUNT: space used: {} avail:{} files used: {} avail {}", log);
        Object[] log2 = { helper.getSpaceUsedInBytes(), helper.getAvailableSpaceInBytes(), helper.getFilesUsed(), helper.getAvailableNumberOfFiles() };
        logger.info("HELPER: space used: {} avail:{} files used: {} avail {}", log2);
        logger.info("CHANGE: existing:{} new:{}", helper.getExistingItems(), helper.getNewItems());
    }

    @Transactional
    private boolean updateAccountAssociations(Account account, Collection<Resource> resourcesToEvaluate, AccountEvaluationHelper helper) {
        // Account localAccount = account;
        Set<Account> additionalAccountsToCleanup = new HashSet<Account>();
        boolean hasUpdates = false;
        for (Resource resource : resourcesToEvaluate) {
            // if the account is null -- die
            if (resource == null) {
                continue;
            }

            if (resource.isUpdated()) {
                hasUpdates = true;
            }
            account.getResources().add(resource);

            if (Persistable.Base.isNullOrTransient(resource.getAccount()) || account.getResources().contains(resource)) {
                helper.getNewItems().add(resource);
                continue;
            }

            // if we're dealing with multiple accounts ...
            if (!account.equals(resource.getAccount())) {
                Account oldAccount = resource.getAccount();
                additionalAccountsToCleanup.add(oldAccount);
                oldAccount.getResources().remove(resource);
                helper.getNewItems().add(resource);
                continue;
            }
            helper.getExistingItems().add(resource);
        }
        getDao().merge(account);

        for (Account old : additionalAccountsToCleanup) {
            updateAccountInfo(old);
        }
        return hasUpdates;
    }

    private AccountAdditionStatus updateResourceStatusesAndReconcileAccountStatus(AccountEvaluationHelper helper, AccountAdditionStatus status) {
        markResourcesAsFlagged(helper.getFlagged());
        unMarkResourcesAsFlagged(helper.getUnflagged());
        logger.info("HELPER FINAL: s:{} f:{} r:{} ", helper.getAvailableSpaceInBytes(), helper.getAvailableNumberOfFiles(), helper.getUnflagged());
        if (helper.getFlagged().size() > 0) {
            if (helper.getAvailableSpaceInBytes() < 0) {
                status = AccountAdditionStatus.NOT_ENOUGH_SPACE;
            } else {
                status = AccountAdditionStatus.NOT_ENOUGH_FILES;
            }
        }
        logger.info("ACCOUNT Status: {} resources: {} FLAGGED: {}", status, helper.getUnflagged(), helper.getFlagged());
        return status;
    }

    private void processResourcesChronologically(AccountEvaluationHelper helper, Collection<Resource> resourcesToEvaluate) {
        List<Resource> resourceList = new ArrayList<Resource>(resourcesToEvaluate);
        GenericService.sortByUpdatedDate(resourceList);
        processResourceGroup(helper, resourceList, Mode.ADD);
    }

    /*
     * For each item, determine if the account has space for it, if so, add it, if not, add it to the flagged list.
     * Wait until the very end to add the weight of the flagged resources to try and get as many resources visible
     * and functional as possible.
     */
    private void processResourceGroup(AccountEvaluationHelper helper, List<Resource> items, Mode mode) {
        boolean seenFlagged = false;
        for (Resource resource : items) {
            if (hasSpaceFor(resource, helper, mode)) {
                helper.getUnflagged().add(resource);
                updateMarkers(resource, helper, mode);
            } else {
                if (!seenFlagged) {
                    logger.info("First Flagged item: {}", resource.getId());
                    seenFlagged = true;
                }
                helper.getFlagged().add(resource);
            }
        }
        for (Resource resource : helper.getFlagged()) {
            updateMarkers(resource, helper, mode);
        }
    }

    enum Mode {
        UPDATE,
        ADD;
    }

    /*
     * Update the account files and space settings. Based on Mode. Mode for a full re-evaulation of the account will be
     * ADD whereby the total space used is evaluated. Otherwise, in UPDATE, the differential change between last save and
     * current is used.
     */
    private void updateMarkers(Resource resource, AccountEvaluationHelper helper, Mode mode) {
        if (!resource.isCountedInBillingEvaluation()) {
            return;
        }
        Long files = resource.getEffectiveFilesUsed();
        Long space = resource.getEffectiveSpaceUsed();
        if (mode == Mode.ADD) {
            files = resource.getFilesUsed();
            space = resource.getSpaceInBytesUsed();
        }
        logger.debug(String.format(" HELPER: space:%s(%s) files:%s(%s) r:%s", helper.getSpaceUsedInBytes(), space, helper.getFilesUsed(), files,
                resource.getId()));
        helper.setSpaceUsedInBytes(helper.getSpaceUsedInBytes() + space);
        helper.setFilesUsed(helper.getFilesUsed() + files);
    }

    private boolean hasSpaceFor(Resource resource, AccountEvaluationHelper helper, Mode mode) {
        Long files = resource.getEffectiveFilesUsed();
        Long space = resource.getEffectiveSpaceUsed();
        if (mode == Mode.ADD) {
            files = resource.getFilesUsed();
            space = resource.getSpaceInBytesUsed();
        }

        if (files == 0 && space == 0) {
            return true;
        }
        if (!resource.isCountedInBillingEvaluation()) {
            logger.debug("Skipping {} in eval b/c it's not counted", resource.getId());
            return true;
        }
        Object[] log = { helper.getSpaceUsedInBytes(), helper.getAvailableSpaceInBytes(), helper.getFilesUsed(), helper.getAvailableNumberOfFiles() , space, files, resource.getId(), resource.getStatus()};
        logger.info("HELPER: space used: {} avail:{} files used: {} avail: {} ++ space: {} files: {} id: {} ({})", log);
        // Trivial changes should fall through and not update because they are no-op in terms of effective changes
        if (helper.getModel().getCountingSpace() && helper.getAvailableSpaceInBytes() - space < 0) {
            logger.info("OVERAGE ==> space used:{} space available:{} resourceId:{}", space, helper.getAvailableSpaceInBytes(), resource.getId());
            return false;
        }
        if (helper.getModel().getCountingFiles() && helper.getAvailableNumberOfFiles() - files < 0) {
            logger.info("OVERAGE ==> files used:{} files available:{} resourceId:{}", files, helper.getAvailableNumberOfFiles(), resource.getId());
            return false;
        }
        return true;
    }

    @Transactional
    public void updateTransientAccountInfo(Collection<Resource> resources) {
        getDao().updateTransientAccountOnResources(resources);
    }

    @Transactional
    public void updateTransientAccountInfo(Resource resource) {
        // TODO: add hql/sql for account lookup by resource
        if (resource == null)
            return;
        updateTransientAccountInfo(Arrays.asList(resource));
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

    public PricingOption getCheapestActivityByFiles(Long numFiles_, Long numMb_, boolean exact) {
        Long numFiles = 0L;
        Long numMb = 0L;
        if (numFiles_ != null) {
            numFiles = numFiles_;
        }
        if (numMb_ != null) {
            numMb = numMb_;
        }

        if (numFiles == 0 && numMb == 0) {
            return null;
        }

        PricingOption option = new PricingOption(PricingType.SIZED_BY_FILE_ONLY);
        if (!exact) {
            option.setType(PricingType.SIZED_BY_FILE_ABOVE_TIER);
        }
        List<BillingItem> items = new ArrayList<BillingItem>();
        logger.info("files: {} mb: {}", numFiles, numMb);

        for (BillingActivity activity : getActiveBillingActivities()) {
            int calculatedNumberOfFiles = numFiles.intValue(); // Don't use test activities or activities that are Just about MB
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
        logger.trace("{} {}", option, items);
        // finding the cheapest
        BillingItem lowest = null;
        for (BillingItem item : items) {
            if (lowest == null) {
                lowest = item;
            } else if (lowest.getSubtotal() > item.getSubtotal()) {
                lowest = item;
            } else if (lowest.getSubtotal().equals(item.getSubtotal())) {
                /*
                 * FIXME: if two items have the SAME price, but one has more "stuff" we should choose the one with more "stuff"
                 * Caution: there are many corner cases whereby we may not know about how to determine what's the better pricing decision when we're providing a
                 * per-file and per-mb price. eg: 8 files and 200 MB or 10 files and 150? There's no way to choose.
                 */
                logger.info("{} =??= {} ", lowest, item);
            }
        }
        option.getItems().add(lowest);
        BillingActivity spaceActivity = getSpaceActivity();
        if (lowest == null) {
            logger.warn("no options found for f:{} m:{} ", numFiles, numMb);
            return null;
        }
        Long spaceAvailable = lowest.getQuantity() * lowest.getActivity().getNumberOfMb();
        Long spaceNeeded = numMb - spaceAvailable;
        logger.info("adtl. space needed: {} avail: {} ", spaceNeeded, spaceAvailable);
        logger.info("space act: {} ", getSpaceActivity());
        calculateSpaceActivity(option, spaceActivity, spaceNeeded);

        if (option.getTotalMb() < numMb || option.getTotalFiles() < numFiles) {
            return null;
        }

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

    public PricingOption getCheapestActivityBySpace(Long numFiles_, Long spaceInMb_) {
        Long numFiles = 0L;
        Long spaceInMb = 0L;
        if (numFiles_ != null) {
            numFiles = numFiles_;
        }
        if (spaceInMb_ != null) {
            spaceInMb = spaceInMb_;
        }

        if (numFiles == 0 && spaceInMb == 0) {
            return null;
        }

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
        if (option == null || option.getTotalMb() < spaceInMb || option.getTotalFiles() < numFiles) {
            return null;
        }

        return option;
    }

    public PricingOption calculateCheapestActivities(Invoice invoice) {
        PricingOption lowestByMB = getCheapestActivityBySpace(invoice.getNumberOfFiles(), invoice.getNumberOfMb());
        PricingOption lowestByFiles = getCheapestActivityByFiles(invoice.getNumberOfFiles(), invoice.getNumberOfMb(), false);

        // If we are using the ok amount of space for that activity...
        if (lowestByFiles != null) {
            logger.info("lowest by files: {}", lowestByFiles.getSubtotal());
        }
        if (lowestByMB != null) {
            logger.info("lowest by space: {} ", lowestByMB.getSubtotal());
        }
        if (lowestByMB == null || (lowestByFiles != null && lowestByFiles.getSubtotal() < lowestByMB.getSubtotal())) {
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

    @Transactional
    public void checkCouponStillValidForCheckout(Coupon coupon, Invoice invoice) {
        getDao().checkCouponStillValidForCheckout(coupon, invoice);
    }
    
    @Transactional
    public void redeemCode(Invoice persistable, Person user, String code) {
        if (StringUtils.isEmpty(code)) {
            return;
        }
        Coupon coupon = locateRedeemableCoupon(code, user);
        if (coupon == null) {
            throw new TdarRecoverableRuntimeException(CANNOT_REDEEM_COUPON);
        }
        if (Persistable.Base.isNotNullOrTransient(persistable.getCoupon())) {
            if (Persistable.Base.isEqual(coupon, persistable.getCoupon())) {
                return;
            } else {
                throw new TdarRecoverableRuntimeException(COUPON_ALREADY_APPLIED);
            }
        }
        if (coupon.getDateExpires().before(new Date())) {
            throw new TdarRecoverableRuntimeException(COUPON_HAS_EXPIRED);
        }
        persistable.setCoupon(coupon);
        coupon.setUser(user);
        coupon.setDateRedeemed(new Date());
        getDao().saveOrUpdate(coupon);
    }

    @Transactional(readOnly = true)
    public Coupon locateRedeemableCoupon(String code, Person user) {
        if (org.apache.commons.lang.StringUtils.isBlank(code) || Persistable.Base.isNullOrTransient(user)) {
            return null;
        }
        return getDao().findCoupon(code, user);
    }

    @Transactional
    public Coupon generateCouponCode(Account account, Long numberOfFiles, Long numberOfMb, Date dateExpires) {
        Coupon coupon = new Coupon();
        coupon.setDateCreated(new Date());
        coupon.setDateExpires(dateExpires);
        if (Persistable.Base.isNotNullOrTransient(numberOfFiles)) {
            coupon.setNumberOfFiles(numberOfFiles);
        }
        if (Persistable.Base.isNotNullOrTransient(numberOfMb)) {
            coupon.setNumberOfMb(numberOfMb);
        }
        if (coupon.getNumberOfFiles() > 0L && coupon.getNumberOfMb() > 0L) {
            throw new TdarRecoverableRuntimeException(SPECIFY_EITHER_SPACE_OR_FILES);
        }
        
        if ((Persistable.Base.isNullOrTransient(numberOfFiles)  || numberOfFiles < 1 ) && (Persistable.Base.isNullOrTransient(numberOfMb) || numberOfMb < 1)) {
            throw new TdarRecoverableRuntimeException(CANNOT_GENERATE_A_COUPON_FOR_NOTHING);
        }

        if (account.getAvailableNumberOfFiles() < coupon.getNumberOfFiles() || account.getAvailableSpaceInMb() < coupon.getNumberOfMb()) {
            logger.info("{}", account.getTotalNumberOfFiles());
            logger.info("{} < {} " , account.getAvailableNumberOfFiles(), coupon.getNumberOfFiles());
            logger.info("{} < {} " , account.getAvailableSpaceInMb(), coupon.getNumberOfMb());
            throw new TdarRecoverableRuntimeException(NOT_ENOUGH_SPACE_OR_FILES);
        }
        genericDao.save(coupon);

        StringBuilder code = new StringBuilder();
        code.append(coupon.getId()).append("-");
        List<String> codes = TdarConfiguration.getInstance().getCouponCodes();
        for (int i = 0; i < 5; i++) {
            code.append(codes.get((int) (Math.random() * (double) codes.size())));
            code.append("-");
        }
        code.append((int)(Math.random() * 9999));
        coupon.setCode(code.toString());
        account.getCoupons().add(coupon);
        logger.info("adding coupon: {}  to account: {}", coupon, account);
        genericDao.saveOrUpdate(account);
        genericDao.saveOrUpdate(coupon);
        return coupon;
    }
}
