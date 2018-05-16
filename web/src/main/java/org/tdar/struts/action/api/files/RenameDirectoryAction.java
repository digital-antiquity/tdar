package org.tdar.struts.action.api.files;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class RenameDirectoryAction extends AbstractHasFileAction<TdarDir>{

    private static final long serialVersionUID = 7688622418020400216L;
    private String name;
    private BillingAccount account;
    private Long accountId;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    
    @Override
    public void prepare() throws Exception {
        super.prepare();
        if (PersistableUtils.isNotNullOrTransient(accountId)) {
            account = getGenericService().find(BillingAccount.class, accountId);
        }

    }
    
    @Override
    public void validate() {
        super.validate();
        if (StringUtils.isBlank(name)) {
            addActionError("dir.missing");
        }
        if (PersistableUtils.isNotNullOrTransient(accountId)) {
            account = getGenericService().find(BillingAccount.class, accountId);
        }

    }
    
    @Action(value = "renameDir",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        personalFilestoreService.renameDirectory(getFile(), getAccount(),  name, getAuthenticatedUser());
        setResultObject(getFile());
        return SUCCESS;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BillingAccount getAccount() {
        return account;
    }

    public void setAccount(BillingAccount account) {
        this.account = account;
    }


}
