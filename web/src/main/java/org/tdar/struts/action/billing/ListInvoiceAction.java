package org.tdar.struts.action.billing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.billing.Invoice;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/billing")
@RequiresTdarUserGroup(TdarGroup.TDAR_BILLING_MANAGER)
@HttpsOnly
public class ListInvoiceAction extends AbstractAuthenticatableAction {

    private static final long serialVersionUID = 5946241615531835007L;
    private static final String LIST_INVOICES = "listInvoices";
    private List<Invoice> invoices = new ArrayList<>();

    @Action(value = LIST_INVOICES, results = { @Result(name = SUCCESS, location = "list-invoices.ftl") })
    public String listInvoices() {
            getInvoices().addAll(getGenericService().findAll(Invoice.class));
            Collections.sort(getInvoices(), new Comparator<Invoice>() {
                @Override
                public int compare(Invoice o1, Invoice o2) {
                    return ObjectUtils.compare(o2.getDateCreated(), o1.getDateCreated());
                }
            });
        return SUCCESS;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

}
