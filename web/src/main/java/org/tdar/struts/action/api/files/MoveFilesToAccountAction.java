package org.tdar.struts.action.api.files;

import java.io.IOException;

import org.apache.http.auth.AUTH;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class MoveFilesToAccountAction extends AbstractHasFilesAction<AbstractFile> {

    private static final long serialVersionUID = 5163531363907929404L;
    private Long toAccountId;
    private BillingAccount account;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        if (getToAccountId() != null) {
            setAccount(getGenericService().find(BillingAccount.class, getToAccountId()));
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (getAuthorizationService().cannotChargeAccount(getAuthenticatedUser(), getAccount())) {
            addActionError("not.allowed");
        }
    }

    @Action(value = "move",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        personalFilestoreService.moveFilesBetweenAccounts(getFiles(), getAccount(), getAuthenticatedUser());
        setResultObject(true);
        return SUCCESS;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

}
