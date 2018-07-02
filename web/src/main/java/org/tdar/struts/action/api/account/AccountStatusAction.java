package org.tdar.struts.action.api.account;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonAccountFilter;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/account")
@Results({
        @Result(name = "exception", type = TdarActionSupport.HTTPHEADER, params = { "error", "500" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.HTTPHEADER, params = { "error", "500" })
})
@HttpForbiddenErrorResponseOnly
public abstract class AccountStatusAction extends AbstractJsonApiAction implements Preparable, Validateable {

    private static final long serialVersionUID = 8057250882285063260L;
    private Long id;
    private BillingAccount account;

    @Override
    public void validate() {

        if (account != null) {
            if (getAuthorizationService().cannotChargeAccount(getAuthenticatedUser(), account)) {
                addActionError("not.allowed");
            }
        }

        super.validate();
    }

    @Override
    public void prepare() throws Exception {

        if (PersistableUtils.isNotNullOrTransient(id)) {
            account = getGenericService().find(BillingAccount.class, getId());
        }
    }

    @Action(value = "getAvailableSpace")
    public String execute() throws IOException {
        setJsonObject(account, JsonAccountFilter.class);
        return SUCCESS;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}