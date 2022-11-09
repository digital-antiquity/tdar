package org.tdar.core.service.billing;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.tdar.core.bean.billing.*;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.dao.external.payment.nelnet.TransactionResponse;
import org.tdar.core.service.billing.PricingOption.PricingType;

public interface InvoiceService {

    String CART_EXTERNAL_PAYMENT_RESPONSE_ACTION_NEW_INVOICE_NOTIFICATION = "cartExternalPaymentResponseAction.new_invoice_notification";

    /**
     * Return defined @link BillingActivity entries that are enabled. A billing activity represents a type of charge (uses ASU Verbage)
     * 
     * @return
     */
    List<BillingActivity> getActiveBillingActivities();

    /**
     * Given an @link Invoice calculate the cheapeset Pricing Option
     * 
     * @param invoice
     * @return
     */
    PricingOption calculateCheapestActivities(Invoice invoice);

    /**
     * Takes the controller specified list of Extra BillingItem(s) and quantities listed by ID and returns
     * 
     * @param extraItemIds
     * @param extraItemQuantities
     * @return
     */
    Collection<BillingItem> lookupExtraBillingActivities(List<Long> extraItemIds, List<Integer> extraItemQuantities);

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
    Invoice processInvoice(Invoice invoice, TdarUser authenticatedUser, String code, Collection<BillingItem> extraItems, PricingType pricingType,
            Long accountId);

    /**
     * Apply a @link Coupon to a @link Invoice, if the coupon is for more than an invoice, then we bump the cost of the invoice to match the value of the coupon
     * code
     * 
     * @param invoice
     * @param user
     * @param code
     */
    void redeemCode(Invoice invoice, TdarUser user, String code);

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
    Invoice finalizePayment(Invoice invoice_, PaymentMethod paymentMethod);

    /**
     * Find the @link Coupon based on the String code.
     * 
     * @param code
     * @param user
     * @return
     */
    Coupon locateRedeemableCoupon(String code, TdarUser user);

    /**
     * Confirm that @link Coupon can be used (Not assigned, not expired)
     * 
     * @param coupon
     * @param invoice
     */
    void checkCouponStillValidForCheckout(Coupon coupon, Invoice invoice);

    /**
     * Processes the results of a NelNet transaction, first validating the response hash, logging the response, and then updating the invoice with all of the
     * information available, and finally sends an email.
     * 
     * @param response
     * @param paymentTransactionProcessor
     * @throws IOException
     */
    void processTransactionResponse(TransactionResponse response, PaymentTransactionProcessor paymentTransactionProcessor) throws IOException;

    /**
     * Ensure an invoice has the correct state after setting the invoice's billing account
     * 
     * @param invoice
     */
    void updateInvoiceStatus(Invoice invoice);

    void completeInvoice(Invoice invoice);

    PricingOption getCheapestActivityByFiles(long filesUsed, long spaceUsedInMb, boolean b);

    PricingOption getCheapestActivityBySpace(long filesUsed, long spaceUsedInMb);


    /**
     * For the specified billing account, determine whether the system should apply an accession fee to that billing
     * account's next invoice.  If so, return a list of applicable activities.  If the billing account does not
     * require an accession fee, return an empty list.
     * @param account
     * @return
     */
    List<BillingActivity> getApplicableAccessionFeeActivities(BillingAccount account);

    /**
     * Apply the appropriate fee waiver to an invoice, if applicable.  This method will
     * only apply a waiver if the invoice contains a fee billing item.  If the method
     * appends a waiver to the invoice, the waiver cost will "match" the fee that the waiver
     * applies to (e.g. a $50 fee will receive a -$50 waiver).
     * @param invoice
     * @return true, if waiver added; otherwise false
     */
    boolean applyFeeWaiver(Invoice invoice);


}