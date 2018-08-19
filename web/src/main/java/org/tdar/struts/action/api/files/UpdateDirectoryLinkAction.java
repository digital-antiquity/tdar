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
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class UpdateDirectoryLinkAction extends AbstractHasFileAction<TdarDir>{


    private static final long serialVersionUID = -8391498966729712455L;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Override
    public void prepare() throws Exception {
        super.prepare();
    }

    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNullOrTransient(getFile().getCollection())) {
            addActionError("missing.collection");
        }
        
        if (!getAuthorizationService().canAddToCollection(getAuthenticatedUser(), getFile().getCollection())) {
            addActionError("bad.collection.permissions");
        }

    }

    @Action(value = "updateDirCollection",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        personalFilestoreService.updateLinkedCollection(getFile(), getAuthenticatedUser());
        setResultObject(getFile());
        return SUCCESS;
    }

}
