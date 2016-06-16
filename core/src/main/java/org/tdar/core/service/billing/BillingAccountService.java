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
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingAccountGroup;
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
import org.tdar.core.dao.BillingAccountDao;
import org.tdar.core.dao.InvoiceDao;
import org.tdar.core.dao.ResourceEvaluator;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.billing.PricingOption.PricingType;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

@Transactional(readOnly = true)
@Service
public class BillingAccountService extends ServiceInterface.TypedDaoBase<BillingAccount, BillingAccountDao> {

    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    InvoiceDao invoiceDao;
    
    /**
     * Find the account (if exists) associated with the invoice
     * 
     * @param invoice
     * @return
     */
    @Transactional(readOnly=true)
    public BillingAccount getAccountForInvoice(Invoice invoice) {
        return getDao().getAccountForInvoice(invoice);
    }

    /**
     * Find all accounts for user: return accounts that are active and have not met their quota
     * 
     * @param user
     * @param statuses
     * @return
     */
    @Transactional(readOnly=true)
    public List<BillingAccount> listAvailableAccountsForUser(TdarUser user, Status... statuses) {
        if (PersistableUtils.isNullOrTransient(user)) {
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
    @Transactional(readOnly=true)
    public List<Invoice> listUnassignedInvoicesForUser(Person user) {
        if (PersistableUtils.isNullOrTransient(user)) {
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
    @Transactional(readOnly=true)
    public ResourceEvaluator getResourceEvaluator(Collection<Resource> resources) {
        return getDao().getResourceEvaluator(resources);
    }

    /**
     * Get the @link AccountGroup referenced by the @link Account
     * 
     * @param account
     * @return
     */
    @Transactional(readOnly=true)
    public BillingAccountGroup getAccountGroup(BillingAccount account) {
        if (PersistableUtils.isNullOrTransient(account)) {
            return null;
        }
        return getDao().getAccountGroup(account);
    }

    /**
     * Check that an @link Invoice can be assigned to an @link Account based on the permissions of who transacted the Invoice
     * 
     * @param find
     * @param account
     * @return
     */
    @Transactional(readOnly=true)
    public boolean checkThatInvoiceBeAssigned(Invoice find, BillingAccount account) {

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
        List<BillingAccount> accounts = listAvailableAccountsForUser(user);
        for (BillingAccount account : accounts) {
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
            BillingAccount account = createAccountForUserIfNeeded(user);
            for (Invoice invoice : unassignedInvoices) {
                logger.trace("account:{}   invoice{}   user:{}", account, invoice, user);
                account.getInvoices().add(invoice);
            }
            getDao().saveOrUpdate(account);
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
    public BillingAccount createAccountForUserIfNeeded(TdarUser user) {
        List<BillingAccount> accounts = listAvailableAccountsForUser(user);
        BillingAccount account;
        if (CollectionUtils.isNotEmpty(accounts)) {
            account = accounts.iterator().next();
        } else {
            account = new BillingAccount();
            account.setName("Generated account for " + user.getProperName());
            account.markUpdated(user);
            getDao().saveOrUpdate(account);
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
    @Transactional(readOnly=false)
    public AccountAdditionStatus updateQuota(BillingAccount account, TdarUser user, Resource... resources) {
        return updateQuota(account, Arrays.asList(resources), user);
    }

    /**
     * Refresh the account info for an @link Account
     * 
     * @param account
     */
    @Transactional(readOnly=false)
    public void updateAccountInfo(BillingAccount account) {
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
    public AccountAdditionStatus updateQuota(BillingAccount account, Collection<Resource> resourcesToEvaluate, TdarUser user) {
        return getDao().updateQuota(account, resourcesToEvaluate, user);
    }

    /**
     * Iterate through a Collection of @link Resource entries and set the @link Account
     * 
     * @param resources
     */
    @Transactional(readOnly=true)
    public void updateTransientAccountInfo(Collection<Resource> resources) {
        getDao().updateTransientAccountOnResources(resources);
    }

    /**
     * Set an @link Account on a @link Resource
     * 
     * @param resource
     */
    @Transactional(readOnly=true)
    public void updateTransientAccountInfo(Resource resource) {
        // TODO: add hql/sql for account lookup by resource
        if (resource == null) {
            return;
        }
        updateTransientAccountInfo(Arrays.asList(resource));
    }
    
    
    @Transactional(readOnly=false)
    public void transferBalanace(TdarUser user, BillingAccount from, BillingAccount to, Long numberOfFiles) {
        if (PersistableUtils.isEqual(from,  to)) {
            return;
        }
        Coupon coupon = new Coupon();
        coupon.setCode(null);
        coupon.setDateCreated(new Date());
        coupon.setDateExpires(new Date());
        coupon.setDateRedeemed(new Date());
        if (PersistableUtils.isNotNullOrTransient(numberOfFiles) && from.getAvailableNumberOfFiles() > numberOfFiles) {
            coupon.setNumberOfFiles(numberOfFiles);
        } else {
            coupon.setNumberOfFiles(from.getAvailableNumberOfFiles());
        }
        coupon.setCode("INTERNAL" + new Date());
        getDao().saveOrUpdate(coupon);
        from.getCoupons().add(coupon);
        getDao().saveOrUpdate(from);

        Invoice invoice = new Invoice();
        invoice.setPaymentMethod(PaymentMethod.MANUAL);
        invoice.setOtherReason(String.format("%s transfered credit from account (%s: %s) to account (%s : %s)", user.getProperName(), from.getId(),from.getName(),to.getId(), to.getName()));
        invoice.setNumberOfFiles(coupon.getNumberOfFiles());
        PricingOption calculateActivities = invoiceDao.calculateActivities(invoice, PricingType.SIZED_BY_FILE_ONLY);
        invoice.getItems().addAll(calculateActivities.getItems());
        invoice.markUpdated(user);
        invoice.setCoupon(coupon);
        getDao().saveOrUpdate(invoice);
        to.getInvoices().add(invoice);
        getDao().saveOrUpdate(to);
        invoiceDao.completeInvoice(invoice);
        to.resetTransientTotals();
        from.resetTransientTotals();
        logger.debug("{}", invoice);
        updateQuota(from, from.getResources(), user);
        updateQuota(to, to.getResources(), user);
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
    @Transactional(readOnly=false)
    public Coupon generateCouponCode(BillingAccount account, Long numberOfFiles, Long numberOfMb, Date dateExpires) {
        Coupon coupon = new Coupon();
        coupon.setDateCreated(new Date());
        coupon.setDateExpires(dateExpires);
        getDao().markWritableOnExistingSession(account);
        if (PersistableUtils.isNotNullOrTransient(numberOfFiles)) {
            coupon.setNumberOfFiles(numberOfFiles);
        }
        if (PersistableUtils.isNotNullOrTransient(numberOfMb)) {
            coupon.setNumberOfMb(numberOfMb);
        }
        if ((coupon.getNumberOfFiles() > 0L) && (coupon.getNumberOfMb() > 0L)) {
            throw new TdarRecoverableRuntimeException("accountService.specify_either_space_or_files");
        }

        if ((PersistableUtils.isNullOrTransient(numberOfFiles) || (numberOfFiles < 1)) && (PersistableUtils.isNullOrTransient(numberOfMb) || (numberOfMb < 1))) {
            throw new TdarRecoverableRuntimeException("accountService.cannot_generate_a_coupon_for_nothing");
        }

        if ((account.getAvailableNumberOfFiles() < coupon.getNumberOfFiles()) || (account.getAvailableSpaceInMb() < coupon.getNumberOfMb())) {
            logger.trace("{}", account.getTotalNumberOfFiles());
            logger.debug("{} < {} ", account.getAvailableNumberOfFiles(), coupon.getNumberOfFiles());
            logger.debug("{} < {} ", account.getAvailableSpaceInMb(), coupon.getNumberOfMb());
            throw new TdarRecoverableRuntimeException("accountService.not_enough_space_or_files");
        }
        getDao().save(coupon);

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
        getDao().saveOrUpdate(account);
        getDao().saveOrUpdate(coupon);
        return coupon;
    }

    @Transactional(readOnly=false)
    public void resetAccountTotalsToHaveOneFileLeft(BillingAccount account, TdarUser user) {
        getDao().markWritableOnExistingSession(account);
        getLogger().debug(">>>>> F: {} S: {} ", account.getFilesUsed(), account.getSpaceUsedInMb());
        updateQuota(account, account.getResources(), user);
        getDao().refresh(account);
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
            getDao().saveOrUpdate(invoice.getItems());
        }
        updateQuota(account, account.getResources(), user);
        getLogger().debug("<<<<<< F: {} S: {} ", account.getFilesUsed(), account.getSpaceUsedInMb());

    }

    @Transactional(readOnly=false)
    public void processBillingAccountChoice(BillingAccount acct, Invoice invoice, TdarUser authenticatedUser) {
        if (invoice.getOwner() == null) {
            invoice.setOwner(authenticatedUser);
        }
        if (PersistableUtils.isTransient(acct)) {
            acct.markUpdated(invoice.getOwner());
            acct.setStatus(Status.ACTIVE);
        }
        invoice.markUpdated(authenticatedUser);
        acct.getInvoices().add(invoice);
        getDao().saveOrUpdate(invoice);
        getDao().saveOrUpdate(acct);

    }

    /**
     * If an invoice is not assigned to a billing account,  reate a billing account with a default name and
     * assign it to the specified invoice.
     */
    @Transactional(readOnly=false)
    private BillingAccount processBillingAccountChoice(Invoice invoice, TdarUser authenticatedUser) {
        BillingAccount account = getAccountForInvoice(invoice);
        if(account == null) {
            account = new BillingAccount();
        }

        TdarUser owner = invoice.getOwner() == null ? invoice.getOwner() : authenticatedUser;
        account.setName("Default account for " + owner.getProperName());
        return account;
    }

    @Transactional(readOnly=false)
    public BillingAccount reconcileSelectedAccount(long id, Invoice invoice, BillingAccount account, List<BillingAccount> accounts) {
        BillingAccount selectedAccount = null;
        if (id == -1L) {
            if (account != null && StringUtils.isNotBlank(account.getName())) {
                getLogger().debug("looking for account by invoice {}", invoice);
                selectedAccount = account;
            } else {
                selectedAccount = processBillingAccountChoice(invoice, invoice.getOwner());
            }
        } else {
            selectedAccount = getDao().find(BillingAccount.class, id);
        }
        return selectedAccount;
    }

    @Transactional(readOnly=false)
    @Deprecated
    public void updateQuotas(BillingAccount account, ResourceEvaluator re, List<Resource> resources) {
        getDao().updateQuotas(account, re, resources);
    }

    @Transactional(readOnly=true)
    public AccountAdditionStatus canAddResource(BillingAccount account, ResourceEvaluator re) {
        return getDao().canAddResource(account, re);
    }

    @Transactional(readOnly = false)
    public void deleteForController(TextProvider provider, BillingAccount account, String deletionReason, TdarUser authenticatedUser) {
        DeleteIssue deletionIssues = getDeletionIssues(provider, account);
        if (deletionIssues != null && StringUtils.isNotBlank(deletionIssues.getIssue())) {
            logger.debug("deletion issues: {}", deletionIssues.getIssue());
            return;
        }
        BillingAccount account_ = getDao().markWritableOnExistingSession(account);
        delete(account_);

    }

    @Transactional(readOnly = false)
    public DeleteIssue getDeletionIssues(TextProvider provider, BillingAccount persistable) {
        DeleteIssue deleteIssue = new DeleteIssue();
        if (CollectionUtils.isNotEmpty(persistable.getResources()) || CollectionUtils.isNotEmpty(persistable.getCoupons())) {
            deleteIssue.setIssue(provider.getText("billingAccount.cannot_delete"));
            deleteIssue.getRelatedItems().addAll(persistable.getResources());
            return deleteIssue;
        }
        return null;
    }

}
