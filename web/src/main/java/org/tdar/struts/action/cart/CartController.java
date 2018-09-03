package org.tdar.struts.action.cart;

import java.net.URL;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.UrlConstants;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.core.bean.billing.TransactionStatus;
import org.tdar.core.dao.external.payment.PaymentMethod;
import org.tdar.core.dao.external.payment.nelnet.PaymentTransactionProcessor;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.core.service.billing.InvoiceService;
import org.tdar.struts.action.AbstractCartController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

/**
 * Responsible for processing payments and viewing finalized invoices.
 * 
 */
@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/cart")
public class CartController extends AbstractCartController {
    private static final long serialVersionUID = 1592977664145682926L;

    public static final String SUCCESS_COMPLETE = "success-complete";
    public static final String SIMPLE = "simple";
    public static final String POLLING = "polling";
    private String redirectUrl;
    private BillingAccount account;

    private PaymentMethod paymentMethod;

    private Long invoiceId = -1L;

    @Autowired
    private transient PaymentTransactionProcessor paymentTransactionProcessor;

    @Autowired
    private transient BillingAccountService accountService;

    @Autowired
    private transient InvoiceService invoiceService;

    /**
     * This method will take the response and prepare it for the CC processing transaction; admin(s) will have additional rights. Ultimately, the redirect URL
     * will open in a "new frame" or window and the resulting window will poll for a response.
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
    @Action(value = "process-payment-request", results = {
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "/cart/${invoice.id}"),
            @Result(name = POLLING, location = "polling.ftl"),
            @Result(name = ADD, type = TdarActionSupport.TDAR_REDIRECT, location = TdarActionSupport.ADD),
            @Result(name = SUCCESS_COMPLETE, type = TDAR_REDIRECT, location = UrlConstants.DASHBOARD),
            @Result(name = ERROR, type = TDAR_REDIRECT, location = UrlConstants.CART_ADD)
    })
    @HttpsOnly
    public String processPaymentRequest() throws TdarActionException {
        if (PersistableUtils.isNullOrTransient(getInvoice())) {
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
                URL url = paymentTransactionProcessor.buildPostUrl(getInvoice());
                if (url != null) {
                    setRedirectUrl(url.toExternalForm());
                }
                return POLLING;
            case INVOICE:
            case MANUAL:
                invoiceService.completeInvoice(invoice);
                break;
        }
        return SUCCESS;
    }

    @Override
    public void prepare() {
        super.prepare();
        if (invoiceId != -1L) {
            setInvoice(getGenericService().find(Invoice.class, invoiceId));
        }
        validateNotNull(getInvoice(), "cartController.invoice_expected_but_not_found");
        if (getInvoice() != null) {
            paymentMethod = getInvoice().getPaymentMethod();
            account = accountService.getAccountForInvoice(getInvoice());
        }
    }

    @Override
    public void validate() {
        validateNotNull(paymentMethod, "cartController.valid_payment_method_is_required");
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public BillingAccount getAccount() {
        return account;
    }

    @Override
    // if user fails validation here, we treat it as an error
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