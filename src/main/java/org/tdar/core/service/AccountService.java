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
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.ResourceEvaluator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.AccountDao;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.utils.AccountEvaluationHelper;

@Transactional(readOnly = true)
@Service
public class AccountService extends ServiceInterface.TypedDaoBase<Account, AccountDao> {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private AuthenticationAndAuthorizationService authService;

    /**
     * Find all accounts for user: return accounts that are active and have not met their quota
     * 
     * @param user
     * @param statuses
     * @return
     */
    public Set<Account> listAvailableAccountsForUser(Person user, Status... statuses) {
        if (Persistable.Base.isNullOrTransient(user)) {
            return Collections.emptySet();
        }
        return getDao().findAccountsForUser(user, statuses);
    }

    /**
     * Find all accounts for user: return accounts that are active and have not met their quota
     * 
     * @param user
     * @return
     */
    public List<Invoice> listUnassignedInvoicesForUser(Person user) {
        if (Persistable.Base.isNullOrTransient(user)) {
            return Collections.emptyList();
        }
        return getDao().findUnassignedInvoicesForUser(user);
    }

    /**
     * Return defined @link BillingActivity entries that are enabled. A billing activity represents a type of charge (uses ASU Verbage)
     * 
     * @return
     */
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

    /**
     * We know that we will change pricing from time to time, so, a @link BillingActivityModel allows us to represent different models at the same time. Return
     * the current model.
     * 
     * @return
     */
    public BillingActivityModel getLatestActivityModel() {
        List<BillingActivityModel> findAll = getDao().findAll(BillingActivityModel.class);
        BillingActivityModel latest = null;
        for (BillingActivityModel model : findAll) {
            if (!model.getActive()) {
                continue;
            }
            if ((latest == null) || (latest.getVersion() == null) || (model.getVersion() > latest.getVersion())) {
                latest = model;
            }
        }
        return latest;
    }

    /**
     * Get a pre-configured @link ResourceEvaluator with the specified array of @link Resource entries.
     * 
     * @param resources
     * @return
     */
    public ResourceEvaluator getResourceEvaluator(Resource... resources) {
        return new ResourceEvaluator(getLatestActivityModel(), resources);
    }

    /**
     * Get a pre-configured @link ResourceEvaluator with the specified collection of @link Resource entries.
     * 
     * @param resources
     * @return
     */
    public ResourceEvaluator getResourceEvaluator(Collection<Resource> resources) {
        return new ResourceEvaluator(getLatestActivityModel(), resources.toArray(new Resource[0]));
    }

    /**
     * Get the @link AccountGroup referenced by the @link Account
     * 
     * @param account
     * @return
     */
    public AccountGroup getAccountGroup(Account account) {
        return getDao().getAccountGroup(account);
    }

    /**
     * Check that an @link Invoice can be assigned to an @link Account based on the permissions of who transacted the Invoice
     * 
     * @param find
     * @param account
     * @return
     */
    public boolean checkThatInvoiceBeAssigned(Invoice find, Account account) {

        if (authService.isMember(find.getTransactedBy(), TdarGroup.TDAR_BILLING_MANAGER)) {
            return true;
        }

        if (account.getOwner().equals(find.getTransactedBy()) || account.getAuthorizedMembers().contains(find.getTransactedBy())) {
            return true;
        }
        throw new TdarRecoverableRuntimeException("accountService.cannot_assign");
    }

    /**
     * Checks whether the @link Person / user has space in their @link Account to create a resource of the specified @link ResourceType. This method also looks
     * to see if their are unassigned Invoices and assigns them to an account if needed.
     * 
     * @param user
     * @param type
     * @param createAccountIfNeeded
     * @return
     */
    @Transactional
    public boolean hasSpaceInAnAccount(TdarUser user, ResourceType type, boolean createAccountIfNeeded) {
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
                if (CollectionUtils.isNotEmpty(accounts) && (accounts.size() == 1)) {
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

    /**
     * Marks a @link Resource @link Status to be FLAGGED_ACCOUNT_BALANCE
     * 
     * @param resources
     */
    @Transactional
    protected void markResourcesAsFlagged(Collection<Resource> resources) {
        for (Resource resource : resources) {
            if (!getResourceEvaluator().getUncountedResourceStatuses().contains(resource.getStatus())) {
                resource.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
            }
        }

    }

    /**
     * Set the @link Resource back to whatever the resource.getPreviousStatus() was set to; set to Status.ACTIVE if NULL.
     * 
     * @param resources
     */
    @Transactional
    protected void unMarkResourcesAsFlagged(Collection<Resource> resources) {
        for (Resource resource : resources) {
            if (resource.getStatus() != Status.FLAGGED_ACCOUNT_BALANCE) {
                continue;
            }
            Status status = resource.getPreviousStatus();
            if (status == null) {
                status = Status.ACTIVE;
            }
            resource.setStatus(status);
        }
    }

    /**
     * Update quota for account based on an array of @link Resource entries
     * 
     * @param account
     * @param resources
     * @return
     */
    @Transactional
    public AccountAdditionStatus updateQuota(Account account, Resource... resources) {
        return updateQuota(account, Arrays.asList(resources));
    }

    /**
     * Refresh the account info for an @link Account
     * 
     * @param account
     */
    @Transactional
    public void updateAccountInfo(Account account) {
        getDao().updateAccountInfo(account, getResourceEvaluator());
    }

    /**
     * Update quota for account based on an Collection of @link Resource entries. This method works in two different paths:
     * (a) if there's an incremental change that does not bump the quota
     * (b) if the account is overdrawn or manually told to re-evaluate.
     * 
     * If (a) then just adjust the small bit; otherwise if (b) sort the resources chronologically and then evaluate chronologically so only the recent files
     * fail
     * 
     * @param account
     * @param resourcesToEvaluate
     * @return
     */
    @Transactional(readOnly = false)
    public AccountAdditionStatus updateQuota(Account account, Collection<Resource> resourcesToEvaluate) {
        logger.info("updating quota(s) {} {}", account, resourcesToEvaluate);
        logger.trace("model {}", getLatestActivityModel());

        if (Persistable.Base.isNullOrTransient(account)) {
            throw new TdarRecoverableRuntimeException("accountService.account_is_null");
        }
        /* evaluate resources based on the model, and update their counts of files and space */
        ResourceEvaluator resourceEvaluator = getResourceEvaluator(resourcesToEvaluate);
        genericDao.saveOrUpdate(resourcesToEvaluate);

        /* make sure the account associations are properly set for each resource in the bunch */
        getDao().updateTransientAccountOnResources(resourcesToEvaluate);
        AccountEvaluationHelper helper = new AccountEvaluationHelper(account, getLatestActivityModel());

        /* check if any of the resources have been modified (ie. resource.markUpdated() has been called */
        boolean hasUpdates = updateAccountAssociations(account, resourcesToEvaluate, helper);

        /* update the account info in the database */
        getDao().updateAccountInfo(account, resourceEvaluator);
        AccountAdditionStatus status = AccountAdditionStatus.CAN_ADD_RESOURCE;

        account.initTotals();
        helper.updateFromAccount(account);
        logAccountAndHelperState(account, helper);

        boolean overdrawn = account.isOverdrawn(getResourceEvaluator());
        logger.info("overdrawn: {} hasUpdates: {}", overdrawn, hasUpdates);

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
            account.reset();

            for (Coupon coupon : account.getCoupons()) {
                account.setFilesUsed(coupon.getNumberOfFiles() + account.getFilesUsed());
                account.setSpaceUsedInBytes((coupon.getNumberOfMb() * Persistable.ONE_MB) + account.getSpaceUsedInBytes());
            }

            helper = new AccountEvaluationHelper(account, getLatestActivityModel());
            // we have to evaluate everything so we can make sure that the transient boolean is set for deleted materials
            getResourceEvaluator().evaluateResources(resourcesToEvaluate);
            logger.info("s{} f{} r:{} ", account.getAvailableSpaceInBytes(), account.getAvailableNumberOfFiles(), helper.getUnflagged());
            processResourcesChronologically(helper, resourcesToEvaluate);

            status = updateResourceStatusesAndReconcileAccountStatus(helper, status);
            overdrawn = account.isOverdrawn(getResourceEvaluator());
            logger.info("flagged: {} overdrawn:{}", helper.getFlagged(), overdrawn);
            if (CollectionUtils.isNotEmpty(helper.getFlagged()) || overdrawn) {
                account.setStatus(Status.FLAGGED_ACCOUNT_BALANCE);
                logger.info("marking account as FLAGGED {} {}", overdrawn, helper.getFlagged());
            } else {
                if (account.getStatus().equals(Status.FLAGGED_ACCOUNT_BALANCE)) {
                    account.setStatus(Status.ACTIVE);
                }
            }

            genericDao.saveOrUpdate(resourcesToEvaluate);
            helper.updateAccount();
            updateAccountInfo(account);
        } else {
            account.setStatus(Status.ACTIVE);
        }

        saveOrUpdate(account);
        helper = null;
        logger.trace("files used: {} ", account.getFilesUsed());
        logger.trace("files avail: {} ", account.getAvailableNumberOfFiles());
        logger.trace("space used: {} ", account.getSpaceUsedInMb());
        logger.trace("space avail: {} ", account.getAvailableSpaceInMb());
        return status;
    }

    /**
     * Log out the account info and the identified change
     * 
     * @param account
     * @param helper
     */
    private void logAccountAndHelperState(Account account, AccountEvaluationHelper helper) {
        Object[] log = { account.getSpaceUsedInBytes(), account.getAvailableSpaceInBytes(), account.getFilesUsed(), account.getAvailableNumberOfFiles() };
        logger.info("ACCOUNT: space used: {} avail:{} files used: {} avail {}", log);
        Object[] log2 = { helper.getSpaceUsedInBytes(), helper.getAvailableSpaceInBytes(), helper.getFilesUsed(), helper.getAvailableNumberOfFiles() };
        logger.info("HELPER: space used: {} avail:{} files used: {} avail {}", log2);
        logger.info("CHANGE: existing:{} new:{}", helper.getExistingItems(), helper.getNewItems());
    }

    /**
     * Update a Collection of @link Resource entries to use the specified @link Account and keep track using the @link AccountEvaluationHelper
     * 
     * @param account
     * @param resourcesToEvaluate
     * @param helper
     * @return
     */
    @Transactional
    private boolean updateAccountAssociations(Account account, Collection<Resource> resourcesToEvaluate, AccountEvaluationHelper helper) {
        // Account localAccount = account;
        Set<Account> additionalAccountsToCleanup = new HashSet<Account>();
        boolean hasUpdates = false;

        // try this without the merge
        getDao().merge(account);

        for (Resource resource : resourcesToEvaluate) {
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

        for (Account old : additionalAccountsToCleanup) {
            updateAccountInfo(old);
        }
        return hasUpdates;
    }

    /**
     * Once the @link Account has been reconcilled, update all of the @link Status entires for each @link Resource and report back
     * 
     * @param helper
     * @param status
     * @return
     */
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

    /**
     * Process through all @link Resource entries chronologically and identify items that should be marked as Status.FLAGGED_FOR_BILLING
     * 
     * @param helper
     * @param resourcesToEvaluate
     */
    private void processResourcesChronologically(AccountEvaluationHelper helper, Collection<Resource> resourcesToEvaluate) {
        List<Resource> resourceList = new ArrayList<Resource>(resourcesToEvaluate);
        GenericService.sortByCreatedDate(resourceList);
        processResourceGroup(helper, resourceList, Mode.ADD);
    }

    /**
     * For each item, determine if the account has space for it, if so, add it, if not, add it to the flagged list.
     * Wait until the very end to add the weight of the flagged resources to try and get as many resources visible
     * and functional as possible.
     * 
     * @param helper
     * @param items
     * @param mode
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

    /**
     * Update the account files and space settings. Based on Mode. Mode for a full re-evaulation of the account will be
     * ADD whereby the total space used is evaluated. Otherwise, in UPDATE, the differential change between last save and
     * current is used.
     * 
     * @param resource
     * @param helper
     * @param mode
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
        logger.trace(String.format(" HELPER: space:%s(%s) files:%s(%s) r:%s", helper.getSpaceUsedInBytes(), space, helper.getFilesUsed(), files,
                resource.getId()));
        helper.setSpaceUsedInBytes(helper.getSpaceUsedInBytes() + space);
        helper.setFilesUsed(helper.getFilesUsed() + files);
    }

    /**
     * Check that an @link Account has space for the @link Resource using the metadata in @link AccountEvaluationHelper
     * 
     * @param resource
     * @param helper
     * @param mode
     * @return
     */
    private boolean hasSpaceFor(Resource resource, AccountEvaluationHelper helper, Mode mode) {
        Long files = resource.getEffectiveFilesUsed();
        Long space = resource.getEffectiveSpaceUsed();
        if (mode == Mode.ADD) {
            files = resource.getFilesUsed();
            space = resource.getSpaceInBytesUsed();
        }

        if ((files == 0) && (space == 0)) {
            return true;
        }
        if (!resource.isCountedInBillingEvaluation()) {
            logger.debug("Skipping {} in eval b/c it's not counted", resource.getId());
            return true;
        }
        logger.trace("mode: {}", mode);
        Object[] log = { helper.getSpaceUsedInBytes(), helper.getAvailableSpaceInBytes(), helper.getFilesUsed(), helper.getAvailableNumberOfFiles(), space,
                files, resource.getId(), resource.getStatus() };
        logger.debug("HELPER: space used: {} avail:{} files used: {} avail: {} ++ space: {} files: {} id: {} ({})", log);
        // Trivial changes should fall through and not update because they are no-op in terms of effective changes
        if (helper.getModel().getCountingSpace() && ((helper.getAvailableSpaceInBytes() - space) < 0)) {
            logger.debug("OVERAGE ==> space used:{} space available:{} resourceId:{}", space, helper.getAvailableSpaceInBytes(), resource.getId());
            return false;
        }
        if (helper.getModel().getCountingFiles() && ((helper.getAvailableNumberOfFiles() - files) < 0)) {
            logger.trace("files used:{} files available:{} resourceId:{}", files, helper.getAvailableNumberOfFiles(), resource.getId());
            return false;
        }
        return true;
    }

    /**
     * Iterate through a Collection of @link Resource entries and set the @link Account
     * 
     * @param resources
     */
    @Transactional
    public void updateTransientAccountInfo(Collection<Resource> resources) {
        getDao().updateTransientAccountOnResources(resources);
    }

    /**
     * Set an @link Account on a @link Resource
     * 
     * @param resource
     */
    @Transactional
    public void updateTransientAccountInfo(Resource resource) {
        // TODO: add hql/sql for account lookup by resource
        if (resource == null) {
            return;
        }
        updateTransientAccountInfo(Arrays.asList(resource));
    }

    /**
     * Based on an @link Account and criteria, generate a @link Coupon
     * 
     * @param account
     * @param numberOfFiles
     * @param numberOfMb
     * @param dateExpires
     * @return
     */
    @Transactional
    public Coupon generateCouponCode(Account account, Long numberOfFiles, Long numberOfMb, Date dateExpires) {
        Coupon coupon = new Coupon();
        coupon.setDateCreated(new Date());
        coupon.setDateExpires(dateExpires);
        genericDao.markWritableOnExistingSession(account);
        if (Persistable.Base.isNotNullOrTransient(numberOfFiles)) {
            coupon.setNumberOfFiles(numberOfFiles);
        }
        if (Persistable.Base.isNotNullOrTransient(numberOfMb)) {
            coupon.setNumberOfMb(numberOfMb);
        }
        if ((coupon.getNumberOfFiles() > 0L) && (coupon.getNumberOfMb() > 0L)) {
            throw new TdarRecoverableRuntimeException("accountService.specify_either_space_or_files");
        }

        if ((Persistable.Base.isNullOrTransient(numberOfFiles) || (numberOfFiles < 1)) && (Persistable.Base.isNullOrTransient(numberOfMb) || (numberOfMb < 1))) {
            throw new TdarRecoverableRuntimeException("accountService.cannot_generate_a_coupon_for_nothing");
        }

        if ((account.getAvailableNumberOfFiles() < coupon.getNumberOfFiles()) || (account.getAvailableSpaceInMb() < coupon.getNumberOfMb())) {
            logger.trace("{}", account.getTotalNumberOfFiles());
            logger.debug("{} < {} ", account.getAvailableNumberOfFiles(), coupon.getNumberOfFiles());
            logger.debug("{} < {} ", account.getAvailableSpaceInMb(), coupon.getNumberOfMb());
            throw new TdarRecoverableRuntimeException("accountService.not_enough_space_or_files");
        }
        genericDao.save(coupon);

        StringBuilder code = new StringBuilder();
        code.append(coupon.getId()).append("-");
        List<String> codes = TdarConfiguration.getInstance().getCouponCodes();
        for (int i = 0; i < 5; i++) {
            code.append(codes.get((int) (Math.random() * codes.size())));
            code.append("-");
        }
        code.append((int) (Math.random() * 9999));
        coupon.setCode(code.toString());
        account.getCoupons().add(coupon);
        logger.info("adding coupon: {}  to account: {}", coupon, account);
        genericDao.saveOrUpdate(account);
        genericDao.saveOrUpdate(coupon);
        return coupon;
    }

    public void resetAccountTotalsToHaveOneFileLeft(Account account) {
        genericDao.markWritableOnExistingSession(account);
        getLogger().debug(">>>>> F: {} S: {} ", account.getFilesUsed(), account.getSpaceUsedInMb());
        updateQuota(account, account.getResources());
        genericDao.refresh(account);
        getLogger().debug(":::: F: {} S: {} ", account.getFilesUsed(), account.getSpaceUsedInMb());
        if (CollectionUtils.isNotEmpty(account.getInvoices()) && (account.getInvoices().size() == 1)) {
            Invoice invoice = account.getInvoices().iterator().next();
            Long space = account.getSpaceUsedInMb() + 10l;
            Long files = account.getFilesUsed() + 1l;
            for (BillingItem item : invoice.getItems()) {
                if (item.getActivity().isSpaceOnly()) {
                    getLogger().debug("changing space from: {} to {}", item.getQuantity(), space);
                    item.setQuantity(space.intValue());
                }

                if (item.getActivity().isFilesOnly()) {
                    getLogger().debug("changing files from: {} to {}", item.getQuantity(), files);
                    item.setQuantity(files.intValue());
                }
            }
            genericDao.saveOrUpdate(invoice.getItems());
        }
        updateQuota(account, account.getResources());
        getLogger().debug("<<<<<< F: {} S: {} ", account.getFilesUsed(), account.getSpaceUsedInMb());
        
    }
}
