package org.tdar.struts.action;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.billing.Account;
import org.tdar.core.bean.billing.Invoice;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
public class BillingAccountController extends AbstractPersistableController<Account> {

    private static final long serialVersionUID = 2912533895769561917L;
    private static final String NEW_ACCOUNT = "new_account";
    private Long invoiceId;
    private List<Account> accounts;

    @SkipValidation
    @Action(value = "choose", results = {
            @Result(name = SUCCESS, location = "select-account.ftl"),
            @Result(name = NEW_ACCOUNT, location = "add")
    })
    public String selectAccount() throws TdarActionException {
        accounts = getAccountService().listAvailableAccountsForUser(getAuthenticatedUser());
        if (CollectionUtils.isNotEmpty(accounts)) {
            return SUCCESS;
        }
        return NEW_ACCOUNT;
    }

    @Override
    protected String save(Account persistable) {
        logger.info("invocieId {}", getInvoiceId());
        if (Persistable.Base.isNotNullOrTransient(invoiceId)) {
            Invoice invoice = getGenericService().find(Invoice.class, invoiceId);
            logger.info("attaching invoice: {} ", invoice);
            // if we have rights
            if (true) {
                getAccount().getInvoices().add(invoice);
                getGenericService().saveOrUpdate(invoice);
                getGenericService().saveOrUpdate(getAccount());
            }
        }
        return SUCCESS;
    }

    @Override
    protected void delete(Account persistable) {
        // TODO Auto-generated method stub

    }

    @Override
    public Class<Account> getPersistableClass() {
        return Account.class;
    }

    @Override
    public String loadMetadata() {
        return SUCCESS;
    }

    public Account getAccount() {
        if (getPersistable() == null)
            setPersistable(createPersistable());

        return (Account) getPersistable();
    }

    @Override
    public boolean isEditable() throws TdarActionException {
        return true;
    }

    public void setAccount(Account account) {
        setPersistable(account);
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }
}