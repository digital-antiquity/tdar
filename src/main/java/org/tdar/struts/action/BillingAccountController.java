package org.tdar.struts.action;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.Account;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
public class BillingAccountController extends AbstractPersistableController<Account> {

    private static final long serialVersionUID = 2912533895769561917L;
    private Long invoiceId;
    
    @Override
    protected String save(Account persistable) {

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