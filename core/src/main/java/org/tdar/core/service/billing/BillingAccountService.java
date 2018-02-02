package org.tdar.core.service.billing;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingAccountGroup;
import org.tdar.core.bean.billing.BillingActivityModel;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.AccountAdditionStatus;
import org.tdar.core.dao.ResourceEvaluator;
import org.tdar.core.service.DeleteIssue;

import com.opensymphony.xwork2.TextProvider;

public interface BillingAccountService {

    /**
     * Find the account (if exists) associated with the invoice
     * 
     * @param invoice
     * @return
     */
    BillingAccount getAccountForInvoice(Invoice invoice);

    /**
     * Find all accounts for user: return accounts that are active and have not met their quota
     * 
     * @param user
     * @param statuses
     * @return
     */
    List<BillingAccount> listAvailableAccountsForUser(TdarUser user, Status... statuses);

    /**
     * Find all accounts for user: return accounts that are active and have not met their quota
     * 
     * @param user
     * @return
     */
    List<Invoice> listUnassignedInvoicesForUser(Person user);

    /**
     * We know that we will change pricing from time to time, so, a @link BillingActivityModel allows us to represent different models at the same time. Return
     * the current model.
     * 
     * @return
     */
    BillingActivityModel getLatestActivityModel();

    /**
     * Get a pre-configured @link ResourceEvaluator with the specified array of @link Resource entries.
     * 
     * @param resources
     * @return
     */
    ResourceEvaluator getResourceEvaluator(Resource... resources);

    /**
     * Get a pre-configured @link ResourceEvaluator with the specified collection of @link Resource entries.
     * 
     * @param resources
     * @return
     */
    ResourceEvaluator getResourceEvaluator(Collection<Resource> resources);

    /**
     * Get the @link AccountGroup referenced by the @link Account
     * 
     * @param account
     * @return
     */
    BillingAccountGroup getAccountGroup(BillingAccount account);

    /**
     * Check that an @link Invoice can be assigned to an @link Account based on the permissions of who transacted the Invoice
     * 
     * @param find
     * @param account
     * @return
     */
    boolean checkThatInvoiceBeAssigned(Invoice find, BillingAccount account);

    /**
     * Checks whether the @link Person / user has space in their @link Account to create a resource of the specified @link ResourceType.
     * 
     * @param user
     * @param type
     * @return
     */
    boolean hasSpaceInAnAccount(TdarUser user, ResourceType type);

    /**
     * If there are invoices owned by the specified to the user that are not assigned to one of the user's existing billing accounts, if the user has no
     * billing accounts, generate an account and then assign the invoices to the generated billing account.
     *
     * @param user
     * @return true if the method assigned any errant invoices to a user billing account
     */
    boolean assignOrphanInvoicesIfNecessary(TdarUser user);

    /**
     * Returns an account owned by the specified user. If the user has no billing accounts, this method generates a billing account then returns that account.
     * 
     * @param user
     * @return
     */
    BillingAccount createAccountForUserIfNeeded(TdarUser user);

    /**
     * Update quota for account based on an array of @link Resource entries
     * 
     * @param account
     * @param resources
     * @return
     */
    AccountAdditionStatus updateQuota(BillingAccount account, TdarUser user, Resource... resources);

    /**
     * Refresh the account info for an @link Account
     * 
     * @param account
     */
    void updateAccountInfo(BillingAccount account);

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
    AccountAdditionStatus updateQuota(BillingAccount account, Collection<Resource> resourcesToEvaluate, TdarUser user);

    /**
     * Iterate through a Collection of @link Resource entries and set the @link Account
     * 
     * @param resources
     */
    void updateTransientAccountInfo(Collection<Resource> resources);

    /**
     * Set an @link Account on a @link Resource
     * 
     * @param resource
     */
    void updateTransientAccountInfo(Resource resource);

    void transferBalanace(TdarUser user, BillingAccount from, BillingAccount to, Long numberOfFiles);

    /**
     * Based on an @link Account and criteria, generate a @link Coupon
     * 
     * @param account
     * @param numberOfFiles
     * @param numberOfMb
     * @param dateExpires
     * @return
     */
    Coupon generateCouponCode(BillingAccount account, Long numberOfFiles, Long numberOfMb, Date dateExpires);

    void resetAccountTotalsToHaveOneFileLeft(BillingAccount account, TdarUser user);

    void processBillingAccountChoice(BillingAccount acct, Invoice invoice, TdarUser authenticatedUser);

    BillingAccount reconcileSelectedAccount(long id, Invoice invoice, BillingAccount account, List<BillingAccount> accounts);

    void updateQuotas(BillingAccount account, ResourceEvaluator re, List<Resource> resources);

    AccountAdditionStatus canAddResource(BillingAccount account, ResourceEvaluator re);

    void deleteForController(TextProvider provider, BillingAccount account, String deletionReason, TdarUser authenticatedUser);

    DeleteIssue getDeletionIssues(TextProvider provider, BillingAccount persistable);

    BillingAccount find(Long id);

    Collection<? extends BillingAccount> findAll();

    List<Invoice> getInvoicesForAccount(BillingAccount account);

    void saveForController(BillingAccount account, String name, String description, Invoice invoice, Long invoiceId, TdarUser owner, TdarUser authenticatedUser,
            List<UserRightsProxy> proxies);

}