package org.tdar.struts.action.cart;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.UrlConstants;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.billing.BillingAccountService;
import org.tdar.struts.action.AbstractCartController;

import com.opensymphony.xwork2.interceptor.ValidationWorkflowAware;
import org.tdar.struts_base.action.PersistableLoadingAction;
import org.tdar.utils.PersistableUtils;

import java.util.List;

/**
 * Created by JAMES on 6/14/2014.
 */
@Component
@Scope("prototype")
@Namespace("/cart")
@ParentPackage("secured")
public class CartReviewPurchaseAction extends AbstractCartController implements ValidationWorkflowAware {

    private static final long serialVersionUID = -6055688199405862384L;

    public static final String REVIEW = "review";

    // id of one of the account chosen from the dropdown list
    private long id = -1L;

    // account chosen from the dropdown list
    private BillingAccount selectedAccount = null;

    // Or a user can specify a new account name
    private BillingAccount account = new BillingAccount();

    @Autowired
    private transient BillingAccountService accountService;
    private boolean showContributorAgreement;

    @Override
    public void prepare() {
        super.prepare();

        getLogger().debug("session data parameters::");
        getSessionData().getParameters().forEach((param) -> getLogger().debug("  param value: {}", param));

        if (!validateInvoice()) {
            return;
        }

        TdarUser owner = getInvoice().getOwner();
        if (owner == null) {
            owner = getAuthenticatedUser();
            getInvoice().setOwner(owner);
            getLogger().debug("invoice had no owner, setting to authenticated user {}", owner);
        }
        setAccounts(accountService.listAvailableAccountsForUser(owner));



        // the account id may have been set already by the "add invoice" link on /billing/{id}/view
        if (id == -1L && getInvoice() != null) {
            getLogger().debug("looking for account by invoice {}", getInvoice());
            selectedAccount = accountService.getAccountForInvoice(getInvoice());
            if (selectedAccount == null && !getAccounts().isEmpty()) {
                selectedAccount = getAccounts().iterator().next();
            }
            if (selectedAccount != null) {
                id = selectedAccount.getId();

                // billing managers may perform this action on accounts they do not have explicit permissions for
                if (!getAccounts().contains(selectedAccount) && getAuthorizationService().isBillingManager(getAuthenticatedUser())) {
                    getAccounts().add(selectedAccount);
                }
            }
        } else {
            selectedAccount = getGenericService().find(BillingAccount.class, id);
        }


        // if user is taking authenticated purchase path, Billing account information will be set already in params
        // FIXME: magic numbers
        // FIXME: bad idea to assume params will be in this order
        List<String> sessionDataParams = getSessionData().getParameters();
        if(sessionDataParams.size() > 0) {
            selectedAccount = new BillingAccount();
            setId(Long.parseLong(sessionDataParams.get(1)));
            selectedAccount.setId(Long.parseLong(sessionDataParams.get(1)));
            selectedAccount.setName(sessionDataParams.get(3));
            selectedAccount.setDescription(sessionDataParams.get(5));
//            getAccounts().add(selectedAccount);
            // Did user choose existing account, or pick a new account?
            if(PersistableUtils.isNullOrTransient(selectedAccount)) {
                account.setName(selectedAccount.getName());
                account.setDescription(selectedAccount.getDescription());
            }
        }

        if (CollectionUtils.isNotEmpty(getAccounts())) {
            getAccounts().add(new BillingAccount("Add an account"));
        }

        getLogger().debug("selected account: {}", selectedAccount);
        getLogger().debug("owner:{}\t accounts:{}", getInvoice().getOwner(), getAccounts());

        // if user is currently not contributor, show them the ToS agreement
        showContributorAgreement = !getAuthenticatedUser().isContributor();
    }

    @Override
    public void validate() {
        // rule: payment method required
        if (getInvoice() == null) {
            addActionMessage(getText("abstractCartController.select_invoice"));
            return;
        }
        if (getInvoice().getPaymentMethod() == null) {
            addActionError(getText("cartController.valid_payment_method_is_required"));
        }
    }

    @Action(value = REVIEW, results = { @Result(name = INPUT, type = TDAR_REDIRECT, location = UrlConstants.CART_ADD) })
    public String execute() {
        if (!getInvoice().isModifiable()) {
            addActionMessage(getText("cartController.cannot_modify_completed_invoice"));
            return INPUT;
        }

        return SUCCESS;
    }

    public BillingAccount getSelectedAccount() {
        return selectedAccount;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TdarUser getBlankPerson() {
        return new TdarUser();
    }

    public boolean isShowContributorAgreement() {
        return showContributorAgreement;
    }
}
