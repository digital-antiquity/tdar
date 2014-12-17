package org.tdar.core.service.billing;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.AccountGroup;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.AccountAdditionStatus;
import org.tdar.core.dao.AccountDao;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.ResourceEvaluator;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.external.AuthorizationService;

/**
 * FIXME: getting too big, needs refactoring. also rename to BillingService?
 * 
 */
@Transactional(readOnly = true)
@Service
public class AccountService extends ServiceInterface.TypedDaoBase<Account, AccountDao> {

    @Autowired
    private transient GenericDao genericDao;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Find the account (if exists) associated with the invoice
     * 
     * @param invoice
     * @return
     */
    public Account getAccountForInvoice(Invoice invoice) {
        return getDao().getAccountForInvoice(invoice);
    }

    /**
     * Find all accounts for user: return accounts that are active and have not met their quota
     * 
     * @param user
     * @param statuses
     * @return
     */
    public List<Account> listAvailableAccountsForUser(TdarUser user, Status... statuses) {
        if (Persistable.Base.isNullOrTransient(user)) {
            return Collections.emptyList();
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
     * We know that we will change pricing from time to time, so, a @link BillingActivityModel allows us to represent different models at the same time. Return
     * the current model.
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public BillingActivityModel getLatestActivityModel() {
        return getDao().getLatestActivityModel();
    }

    /**
     * Get a pre-configured @link ResourceEvaluator with the specified array of @link Resource entries.
     * 
     * @param resources
     * @return
     */
    @Transactional(readOnly = true)
    public ResourceEvaluator getResourceEvaluator(Resource... resources) {
        return getDao().getResourceEvaluator(resources);
    }

    /**
     * Get a pre-configured @link ResourceEvaluator with the specified collection of @link Resource entries.
     * 
     * @param resources
     * @return
     */
    public ResourceEvaluator getResourceEvaluator(Collection<Resource> resources) {
        return getDao().getResourceEvaluator(resources);
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

        if (authorizationService.isMember(find.getTransactedBy(), TdarGroup.TDAR_BILLING_MANAGER)) {
            return true;
        }

        if (account.getOwner().equals(find.getTransactedBy()) || account.getAuthorizedMembers().contains(find.getTransactedBy())) {
            return true;
        }
        throw new TdarRecoverableRuntimeException("accountService.cannot_assign");
    }

    /**
     * Checks whether the @link Person / user has space in their @link Account to create a resource of the specified @link ResourceType.
     * 
     * @param user
     * @param type
     * @return
     */
    @Transactional(readOnly = false)
    public boolean hasSpaceInAnAccount(TdarUser user, ResourceType type) {
        List<Account> accounts = listAvailableAccountsForUser(user);
        for (Account account : accounts) {
            logger.trace("evaluating account {}", account.getName());
            if (account.isActive() && getDao().hasMinimumForNewRecord(account, getResourceEvaluator(), type)) {
                logger.info("account '{}' has minimum balance for {}", account.getName(), user.getProperName());
                return true;
            }
        }

        return false;
    }

    /**
     * If there are invoices owned by the specified to the user that are not assigned to one of the user's existing billing accounts, if the user has no
     * billing accounts, generate an account and then assign the invoices to the generated billing account.
     *
     * @param user
     * @return true if the method assigned any errant invoices to a user billing account
     */
    @Transactional(readOnly = false)
    public boolean assignOrphanInvoicesIfNecessary(TdarUser user) {
        List<Invoice> unassignedInvoices = listUnassignedInvoicesForUser(user);
        if (CollectionUtils.isNotEmpty(unassignedInvoices)) {
            logger.info("Unassigned invoices found for user {}. The system will assign the following invoices: {} ", user, unassignedInvoices);
            Account account = createAccountForUserIfNeeded(user);
            for (Invoice invoice : unassignedInvoices) {
                logger.trace("account:{}   invoice{}   user:{}", account, invoice, user);
                account.getInvoices().add(invoice);
            }
            genericDao.saveOrUpdate(account);
        }
        return !unassignedInvoices.isEmpty();
    }

    /**
     * Returns an account owned by the specified user. If the user has no billing accounts, this method generates a billing account then returns that account.
     * 
     * @param user
     * @return
     */
    @Transactional(readOnly = false)
    public Account createAccountForUserIfNeeded(TdarUser user) {
        List<Account> accounts = listAvailableAccountsForUser(user);
        Account account;
        if (CollectionUtils.isNotEmpty(accounts)) {
            account = accounts.iterator().next();
        } else {
            account = new Account();
            account.setName("Generated account for " + user.getProperName());
            account.markUpdated(user);
            genericDao.saveOrUpdate(account);
        }
        return account;
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
        return getDao().updateQuota(account, resourcesToEvaluate);
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

    @Transactional
    public void processBillingAccountChoice(Account acct, Invoice invoice, TdarUser authenticatedUser) {
        if (invoice.getOwner() == null) {
            invoice.setOwner(authenticatedUser);
        }
        if (Persistable.Base.isTransient(acct)) {
            acct.markUpdated(invoice.getOwner());
            acct.setStatus(Status.ACTIVE);
        }
        invoice.markUpdated(authenticatedUser);
        acct.getInvoices().add(invoice);
        getDao().saveOrUpdate(invoice);
        getDao().saveOrUpdate(acct);

    }

    @Transactional
    /**
     * Create a billing account with a default name and assign it to the specified invoice.
     */
    private Account processBillingAccountChoice(Invoice invoice, TdarUser authenticatedUser) {
        Account account = new Account();
        TdarUser owner = invoice.getOwner() == null ? invoice.getOwner() : authenticatedUser;
        account.setName("Default account for " + owner.getProperName());
        return account;
    }

    @Transactional
    public Account reconcileSelectedAccount(long id, Invoice invoice, Account account, List<Account> accounts) {
        Account selectedAccount = null;
        if (id == -1L) {
            if (account != null && StringUtils.isNotBlank(account.getName())) {
                getLogger().debug("looking for account by invoice {}", invoice);
                selectedAccount = account;
            } else {
                selectedAccount = processBillingAccountChoice(invoice, invoice.getOwner());
            }
        } else {
            selectedAccount = genericDao.find(Account.class, id);
        }
        return selectedAccount;
    }

    @Transactional
    @Deprecated
    public void updateQuotas(Account account, ResourceEvaluator re, List<Resource> resources) {
        getDao().updateQuotas(account, re, resources);
    }

    public AccountAdditionStatus canAddResource(Account account, ResourceEvaluator re) {
        return getDao().canAddResource(account, re);
    }

}
