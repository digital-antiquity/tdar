package org.tdar.struts.action.cart;

import org.apache.commons.httpclient.URIException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.Invoice.TransactionStatus;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.service.InvoiceService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
public class CartController extends AbstractCartController {
    private static final long serialVersionUID = 1592977664145682926L;

    public static final String SUCCESS_COMPLETE = "success-complete";
    public static final String PROCESS_PAYMENT_REQUEST = "process-payment-request";
    public static final String SIMPLE = "simple";
    public static final String POLLING = "polling";
    private String redirectUrl;
    private Account account;

    PaymentMethod paymentMethod;

    private Long invoiceId = -1L;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient InvoiceService cartService;

    @Autowired
    private transient AuthorizationService authService;

    public boolean isNotNullOrZero(Long num) {
        if ((num == null) || (num < 1)) {
            return false;
        }
        return true;
    }

    /**
     * This method will take the response and prepare it for the CC processing transaction; admin(s) will have additional rights. Ultimately, the redirect URL
     * will open in a "new  frame" or window and the resulting window will poll for a response.
     * <p/>
     * <p/>
     * This is the 'launchpad' page were we hand-off control to the external payment processor. The action itself does very little -- most of functionality
     * exists client-side via javascript and ajax.
     * <p/>
     * The user initiates the payment process by clicking on a button that spawns a child window/tab. This child window points to an external url hosted by the
     * payment processing company (i.e. Nelnet) and this host facilitates the entire payment process. Once the process is complete, the child window prompts the
     * user to close the child window.
     * <p/>
     * Meanwhile, the tdar-hosted parent page ("/cart/process-payment-request") polls the "/cart/polling-check" action to determine if the transaction is
     * complete (successful, cancelled, or failed). When complete, the page performs a "client-side redirect" to change browser location to an appropriate
     * landing page.
     * 
     * @return
     * @throws TdarActionException
     */
    @SkipValidation
        @WriteableSession
    // @GetOnly
    @Action(value = PROCESS_PAYMENT_REQUEST, results = {
            @Result(name = SUCCESS, type = "redirect", location = "/invoice/${invoice.id}"),
            @Result(name = POLLING, location = "polling.ftl"),
            @Result(name = ADD, type = TYPE_REDIRECT, location = "add"),
            @Result(name = SUCCESS_COMPLETE, type = "redirect", location = URLConstants.DASHBOARD)
    })
    @HttpsOnly
    public String processPayment() throws TdarActionException {
        if (Persistable.Base.isNullOrTransient(getInvoice())) {
            return ADD;
        }

        Invoice invoice = getInvoice();
        if (!invoice.isModifiable()) {
            return SUCCESS;
        }

        if (invoice.getTransactionStatus().isComplete()) {
            return ERROR;
        }

        if (invoice.getTransactionStatus() == TransactionStatus.TRANSACTION_SUCCESSFUL) {
            return SUCCESS_COMPLETE;
        }
        switch (paymentMethod) {
            case CHECK:
                break;
            case CREDIT_CARD:
                getGenericService().saveOrUpdate(invoice);
                try {
                    //fixme: i shouldn't need to "process" the invoice to figure out what the nelnet url is going to be
                    cartService.finalizePayment(invoice, paymentMethod);
                    setRedirectUrl(paymentTransactionProcessor.prepareRequest(invoice));
                } catch (URIException e) {
                    getLogger().warn("error happend {}", e);
                }
                return POLLING;
            //FIXME: any invoice modification for manual invoices needs to go into service layer. (and should be called via POST action)
            case INVOICE:
            case MANUAL:
                invoice.setTransactionStatus(TransactionStatus.TRANSACTION_SUCCESSFUL);
                getGenericService().saveOrUpdate(invoice);
                break;
        }
        return SUCCESS;
    }

    @Action("view")
    //FIXME: move to new InvoiceController  or create new view-invoice action on BillingAccountController
    public String viewInvoice() {
        return SUCCESS;
    }

    @Override
    public void prepare() {
        super.prepare();

        if(invoiceId != -1L ) {
            setInvoice(getGenericService().find(Invoice.class, invoiceId));
        }

        validateNotNull(getInvoice(), "cart.invoice_expected_but_not_found");
        if(getInvoice() != null) {
            paymentMethod = getInvoice().getPaymentMethod();
            account = cartService.getAccountForInvoice(getInvoice());
        }

    }

    private boolean isViewable()  {
        if (Persistable.Base.isNullOrTransient(getAuthenticatedUser())) {
            return false;
        }
        if (authService.can(InternalTdarRights.VIEW_BILLING_INFO, getAuthenticatedUser())) {
            return true;
        }
        if (getAuthenticatedUser().equals(getInvoice().getOwner())) {
            return true;
        }
        return false;
    }

    @Override
    public void validate() {
        if(!isViewable())  {
            addActionError("you do not have permission to view this invoice");
        }

        validateNotNull(paymentMethod, "cartController.valid_payment_method_is_required");
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    //if user fails validation here, we treat it as an error
    public String getInputResultName() {
        return ERROR;
    }

    public void setInvoiceId(Long invoiceId) {
        getLogger().info("setting invoice id to {}", invoiceId);
        this.invoiceId = invoiceId;
    }

    public void setId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }


    public Long getAccountId() {
        return account.getId();
    }
}