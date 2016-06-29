package org.tdar.core.service.billing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.BillingActivity;
import org.tdar.core.bean.billing.BillingItem;
import org.tdar.core.bean.billing.BillingTransactionLog;
import org.tdar.core.bean.billing.Coupon;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.BillingAccountDao;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.InvoiceDao;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.dao.external.payment.nelnet.TransactionResponse;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.UserNotificationService;
import org.tdar.core.service.billing.PricingOption.PricingType;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

@Service
@Transactional
public class InvoiceService extends ServiceInterface.TypedDaoBase<Invoice, InvoiceDao> {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    public static final String CART_EXTERNAL_PAYMENT_RESPONSE_ACTION_NEW_INVOICE_NOTIFICATION = "cartExternalPaymentResponseAction.new_invoice_notification";

    @Autowired
    private transient GenericDao genericDao;

    @Autowired
    private transient BillingAccountDao accountDao;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient EmailService emailService;

    @Autowired
    private transient SerializationService serializationService;

    @Autowired
    private transient UserNotificationService notificationService;

    /**
     * Return defined @link BillingActivity entries that are enabled. A billing activity represents a type of charge (uses ASU Verbage)
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<BillingActivity> getActiveBillingActivities() {
        return getDao().getActiveBillingActivities();
    }


    /**
     * Given an @link Invoice calculate the cheapeset Pricing Option
     * 
     * @param invoice
     * @return
     */
    @Transactional(readOnly=true)
    public PricingOption calculateCheapestActivities(Invoice invoice) {
        PricingOption lowestByMB = getDao().getCheapestActivityBySpace(invoice.getNumberOfFiles(), invoice.getNumberOfMb());
        PricingOption lowestByFiles = getDao().getCheapestActivityByFiles(invoice.getNumberOfFiles(), invoice.getNumberOfMb(), false);

        // If we are using the ok amount of space for that activity...
        if (lowestByFiles != null) {
            logger.info("lowest by files: {}", lowestByFiles.getSubtotal());
        }
        if (lowestByMB != null) {
            logger.info("lowest by space: {} ", lowestByMB.getSubtotal());
        }
        if ((lowestByMB == null) || ((lowestByFiles != null) && (lowestByFiles.getSubtotal() < lowestByMB.getSubtotal()))) {
            return lowestByFiles;
        }
        return lowestByMB;
    }

    /**
     * Takes the controller specified list of Extra BillingItem(s) and quantities listed by ID and returns
     * 
     * @param extraItemIds
     * @param extraItemQuantities
     * @return
     */
    @Transactional(readOnly=true)

    public Collection<BillingItem> lookupExtraBillingActivities(List<Long> extraItemIds, List<Integer> extraItemQuantities) {
        Map<Long, BillingActivity> actIdMap = PersistableUtils.createIdMap(getActiveBillingActivities());
        Set<BillingItem> items = new HashSet<>();
        for (int i = 0; i < extraItemIds.size(); i++) {
            BillingActivity act = actIdMap.get(extraItemIds.get(i));
            Integer quantity = extraItemQuantities.get(i);
            logger.trace("{} {} {}", extraItemIds.get(i), act, quantity);
            if ((act == null) || (quantity == null) || (quantity < 1)) {
                continue;
            }
            items.add(new BillingItem(act, quantity));
        }
        return items;

    }

    /**
     * For a given invoice object, clear it and apply the appropriate BillingItems based on:
     * 1) the PricingType evaluation (files, MB, etc)
     * 2) any extra BillingItem(s) specfied by admins or for testing
     * 3) a Coupon Code, if specified
     * 
     * @param invoice
     * @param authenticatedUser
     * @param code
     * @param extraItems
     * @param pricingType
     * @param accountId
     * @return
     */
    @Transactional(readOnly = false)
    public Invoice processInvoice(Invoice invoice, TdarUser authenticatedUser, String code, Collection<BillingItem> extraItems, PricingType pricingType,
            Long accountId) {
        boolean billingManager = authorizationService.isBillingManager(authenticatedUser);
        if (!invoice.hasValidValue() && StringUtils.isBlank(code) && !billingManager) {
            throw new TdarRecoverableRuntimeException("invoiceService.specify_something");
        }

        if (PersistableUtils.isNotNullOrTransient(authenticatedUser) && PersistableUtils.isTransient(invoice.getOwner())) {
            invoice.setOwner(authenticatedUser);
            invoice.setTransactedBy(authenticatedUser);
        }

        // setup BillingItem(s) for Invoice
        invoice.getItems().clear();
        invoice.getItems().addAll(extraItems);

        // redeem coupon code, we do this before calculating costs because the redemption may change the files and space
        redeemCode(invoice, invoice.getOwner(), code);
        List<BillingItem> items = new ArrayList<BillingItem>();
        if (pricingType != null) {
            items = getDao().calculateActivities(invoice, pricingType).getItems();
        } else {
            PricingOption activities2 = calculateCheapestActivities(invoice);
            if (activities2 != null) {
                items = activities2.getItems();
            }
        }
        if (CollectionUtils.isNotEmpty(items)) {
            invoice.getItems().addAll(items);
        }

        // if somehow we have absolutely nothing in the invoice, error out
        if (CollectionUtils.isEmpty(invoice.getItems())) {
            throw new TdarRecoverableRuntimeException("cartController.no_items_found");
        }

        invoice.setTransactedBy(authenticatedUser);

        // reconcile the Invoice owner, which could be different if you're an admin and running an invoice for someone else
        updateInvoiceOwner(invoice, authenticatedUser, billingManager);
        invoice.markUpdated(authenticatedUser);

        // if invoice is persisted it will be read-only, so make it writable
        if (PersistableUtils.isNotNullOrTransient(invoice)) {
            genericDao.markUpdatable(invoice);
            genericDao.markUpdatable(invoice.getItems());
        }
        genericDao.saveOrUpdate(invoice);
        if (PersistableUtils.isNotNullOrTransient(accountId)) {
            BillingAccount account = genericDao.find(BillingAccount.class, accountId);
            account.getInvoices().add(invoice);
        }

        return invoice;
    }

    /**
     * Apply a @link Coupon to a @link Invoice, if the coupon is for more than an invoice, then we bump the cost of the invoice to match the value of the coupon
     * code
     * 
     * @param invoice
     * @param user
     * @param code
     */
    @Transactional(readOnly=false)
    public void redeemCode(Invoice invoice, TdarUser user, String code) {
        if (StringUtils.isEmpty(code)) {
            return;
        }
        // find and validate the coupon
        Coupon coupon = locateRedeemableCoupon(code, user);
        logger.debug("{}",coupon);
        if (coupon == null) {
            throw new TdarRecoverableRuntimeException("invoiceService.cannot_redeem_coupon");
        }
        if (PersistableUtils.isNotNullOrTransient(invoice.getCoupon())) {
            if (PersistableUtils.isEqual(coupon, invoice.getCoupon())) {
                return;
            } else {
                throw new TdarRecoverableRuntimeException("invoiceService.coupon_already_applied");
            }
        }
        if (coupon.getDateExpires().before(new Date())) {
            throw new TdarRecoverableRuntimeException("invoiceService.coupon_has_expired");
        }

        // assign the coupon to invoice, and 'redeem' it
        invoice.setCoupon(coupon);
        coupon.setUser(user);
        coupon.setDateRedeemed(new Date());

        // make sure the invoice cancels out the coupon by using all of it, i.e. if the invoice is for 1 file, but the coupon
        // is for 5, the invoice will be changed to be for 5, as we can't break up coupons.

        Long files = invoice.getNumberOfFiles();
        Long mb = invoice.getNumberOfMb();
        if ((files == null) || (coupon.getNumberOfFiles() > files.longValue())) {
            invoice.setNumberOfFiles(coupon.getNumberOfFiles());
        }
        if ((mb == null) || (coupon.getNumberOfMb() > mb.longValue())) {
            invoice.setNumberOfMb(coupon.getNumberOfMb());
        }

        genericDao.saveOrUpdate(coupon);
    }

    /**
     * reconcile the Invoice owner, which could be different if you're an admin and running an invoice for someone else
     * 
     * @param invoice
     * @param authenticatedUser
     * @param billingManager
     */
    @Transactional(readOnly = false)
    private void updateInvoiceOwner(Invoice invoice, TdarUser authenticatedUser, boolean billingManager) {
        TdarUser owner = invoice.getOwner();
        // if we have an owner
        if (billingManager && PersistableUtils.isNotNullOrTransient(owner)) {
            invoice.setOwner(genericDao.find(TdarUser.class, owner.getId()));
        } else {
            // if we're logged in
            if (authenticatedUser != null) {
                invoice.setOwner(authenticatedUser);
            } else {
                // just in case, clear it as we may have a transient instance id=-1
                invoice.setOwner(null);
            }
        }
    }

    /**
     * Finalizes a payment -- given an incoming transient invoice the service will find the existing invoice, and:
     * (a) update the Invoice with any payment info that it needs such as method, or reason
     * (b) mark it as final, so it cannot be modified
     * (c) confirm that the coupon is still valid
     * (d) change the transaction status:
     * 1) if we have a coupon and that coupon is for the entire amount, complete transaction
     * 2) set status to PENDING_TRANSACTION
     * 
     * @param invoice_
     * @param paymentMethod
     * @return
     */
    @Transactional(readOnly = false)
    public Invoice finalizePayment(Invoice invoice_, PaymentMethod paymentMethod) {

        Address address = genericDao.loadFromSparseEntity(invoice_.getAddress(), Address.class);
        if (PersistableUtils.isNotNullOrTransient(address)) {
            invoice_.setAddress(address);
        }

        String otherReason = invoice_.getOtherReason();
        Invoice invoice = genericDao.loadFromSparseEntity(invoice_, Invoice.class);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setOtherReason(otherReason);
        // finalize the cost and cache it
        invoice.markFinal();
        genericDao.saveOrUpdate(invoice);
        logger.info("USER: {} IS PROCESSING TRANSACTION FOR: {} ", invoice_.getId(), invoice_.getTotal());

        // if the discount brings the total cost down to 0, then skip the credit card process
        if ((invoice.getTotal() <= 0) && CollectionUtils.isNotEmpty(invoice.getItems())) {
            if (PersistableUtils.isNotNullOrTransient(invoice.getCoupon())) {
                // accountService.redeemCode(invoice, invoice.getOwner(), invoice.getCoupon().getCode());
                checkCouponStillValidForCheckout(invoice.getCoupon(), invoice);
            }
            genericDao.saveOrUpdate(invoice);
            completeInvoice(invoice);
            handlePurchaseNotifications(invoice);
        } else {
            invoice.setTransactionStatus(TransactionStatus.PENDING_TRANSACTION);
        }

        return invoice;
    }

    /**
     * Find the @link Coupon based on the String code.
     * 
     * @param code
     * @param user
     * @return
     */
    @Transactional(readOnly = true)
    public Coupon locateRedeemableCoupon(String code, TdarUser user) {
        logger.debug("locate coupon: {} for: {} ", code, user);
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return accountDao.findCoupon(code, user);
    }

    /**
     * Confirm that @link Coupon can be used (Not assigned, not expired)
     * 
     * @param coupon
     * @param invoice
     */
    @Transactional(readOnly=true)
    public void checkCouponStillValidForCheckout(Coupon coupon, Invoice invoice) {
        accountDao.checkCouponStillValidForCheckout(coupon, invoice);
    }

    /**
     * Processes the results of a NelNet transaction, first validating the response hash, logging the response, and then updating the invoice with all of the
     * information available, and finally sends an email.
     * 
     * @param response
     * @param paymentTransactionProcessor
     * @throws IOException
     */
    @Transactional(readOnly = false)
    public void processTransactionResponse(TransactionResponse response, PaymentTransactionProcessor paymentTransactionProcessor) throws IOException {
        if (paymentTransactionProcessor.validateResponse(response)) {
            genericDao.markWritable();
            Invoice invoice = paymentTransactionProcessor.locateInvoice(response);

            BillingTransactionLog billingResponse = new BillingTransactionLog(serializationService.convertToJson(response), response.getTransactionId());
            billingResponse = genericDao.markWritableOnExistingSession(billingResponse);
            genericDao.saveOrUpdate(billingResponse);
            if (invoice != null && !response.isRefund()) {
                // if invoice has an address this will throw an exception if it is the same as one of the person adresses. (cascaded merge introduces transient
                // item in p.addresses)
                invoice = genericDao.markWritableOnExistingSession(invoice);
                paymentTransactionProcessor.updateInvoiceFromResponse(response, invoice);
                // Assume that an invoice owner will always want to see the contributor menus.
                updateAddresses(response, invoice);
                invoice.setResponse(billingResponse);
                if (invoice.getTransactionStatus().isSuccessful()) {
                    completeInvoice(invoice);
                }
                logger.info("processing payment response: {}  -> {} ", invoice, invoice.getTransactionStatus());
                // send notifications. if any error happens we want to log it but not rollback the transaction
                handlePurchaseNotifications(invoice);
                BillingAccount account = genericDao.markWritableOnExistingSession(accountDao.getAccountForInvoice(invoice));
                // this is unlikely, but possible (especially if a "bad" request is made
                if (account != null && CollectionUtils.isNotEmpty(account.getResources())) {
                    accountDao.updateQuota(account, account.getResources(), invoice.getOwner());
                }
            }
        }
    }

    /**
     * Send notifications to a user following a successful transaction
     */
    private void handlePurchaseNotifications(Invoice invoice) {
        // always send the notification admin email, but only send the dashboard notification to the user if the transaction was successful
        sendNotificationEmail(invoice);

        if (invoice.getTransactionStatus().isSuccessful()) {
            TdarUser recipient = invoice.getOwner();
            logger.info("sending notification:{} to:{}", CART_EXTERNAL_PAYMENT_RESPONSE_ACTION_NEW_INVOICE_NOTIFICATION, recipient);
            notificationService.info(recipient, CART_EXTERNAL_PAYMENT_RESPONSE_ACTION_NEW_INVOICE_NOTIFICATION);
        } else {
            logger.info("invoice transaction not successful:{}", invoice);
        }
    }

    /**
     * Sends a email to the billing admin when a transaction is complete
     * 
     * @param invoice
     */
    private void sendNotificationEmail(Invoice invoice) {
        Map<String, Object> map = new HashMap<>();
        map.put("invoice", invoice);
        map.put("date", new Date());
        try {
            List<Person> people = new ArrayList<>();
            for (String email : StringUtils.split(TdarConfiguration.getInstance().getBillingAdminEmail(), ";")) {
                if (StringUtils.isBlank(email)) {
                    continue;
                }
                Person person = new Person("Billing", "Info", email.trim());
                genericDao.markReadOnly(person);
                people.add(person);
            }
            Email email = new Email();
            email.setSubject(MessageHelper.getMessage("cartController.subject", Arrays.asList(invoice.getId(), invoice.getTransactedBy().getProperName())));
            for (Person person : people) {
                email.addToAddress(person.getEmail());
            }
            email.setUserGenerated(false);
            emailService.queueWithFreemarkerTemplate("transaction-complete-admin.ftl", map, email);
        } catch (Exception e) {
            logger.error("could not send email: {} ", e);
        }
    }

    /**
     * updates a person's address to include the billing address from the response
     * 
     * @param response
     * @param invoice
     */
    private void updateAddresses(TransactionResponse response, Invoice invoice) {
        TdarUser p = invoice.getOwner();
        boolean found = false;
        Address addressToSave = response.getAddress();
        for (Address address : p.getAddresses()) {
            if (address.isSameAs(addressToSave)) {
                found = true;
            }
        }

        // if user provided an address to nelnet, add that address to user's list of addresses
        // fixme: This is sketchy behavior. Just because I gave the payment processor my billing address does not imply I want to give it to tDAR.
        if (!found) {
            p.getAddresses().add(addressToSave);
            logger.info(addressToSave.getAddressSingleLine());
            // this will always fail(you can't save a transient addresss directly because it is a child relation of person). It's also unnecessary: If p
            // is on the session hibernate will save the address.
            // genericDao.saveOrUpdate(addressToSave);
            invoice.setAddress(addressToSave);
        }
    }

    /**
     * Ensure an invoice has the correct state after setting the invoice's billing account
     * 
     * @param invoice
     */
    @Transactional(readOnly=false)
    public void updateInvoiceStatus(Invoice invoice) {
        // Assume that an invoice owner will always want to see the contributor menus.
        invoice.getOwner().setContributor(true);

        PaymentMethod paymentMethod = invoice.getPaymentMethod();

        switch (paymentMethod) {
            case CHECK:
                break;
            case CREDIT_CARD:
                genericDao.saveOrUpdate(invoice);
                finalizePayment(invoice, paymentMethod);
                break;
            case INVOICE:
            case MANUAL:
                completeInvoice(invoice);
                break;
        }
    }

    @Transactional(readOnly = false)
    public void completeInvoice(Invoice invoice) {
    	logger.debug("completing invoice");
    	getDao().completeInvoice(invoice);

    }

    @Transactional(readOnly=true)
    public PricingOption getCheapestActivityByFiles(long filesUsed, long spaceUsedInMb, boolean b) {
        return getDao().getCheapestActivityByFiles(filesUsed, spaceUsedInMb, b);
    }


    public PricingOption getCheapestActivityBySpace(long filesUsed, long spaceUsedInMb) {
        return getDao().getCheapestActivityBySpace(filesUsed, spaceUsedInMb);
    }
}
