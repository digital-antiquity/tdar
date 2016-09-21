package org.tdar.struts.action.collection;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.collection.AdhocShare;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/share")
@PostOnly
public class AdhocShareCreateAction extends AbstractAuthenticatableAction implements Preparable, Validateable {

    private static final long serialVersionUID = -5278866253460110671L;

    private AdhocShare share = new AdhocShare();
    private List<Resource> resources = new ArrayList<>();
    private BillingAccount account;
    private ResourceCollection collection;

    @Autowired
    private ResourceCollectionService resourceCollectionService;
    @Autowired
    private AuthorizationService authorizationService;
    
    @Override
    public void prepare() throws Exception {
        if (share != null) {
            resources = getGenericService().findAll(Resource.class, share.getResourceIds());
            account = getGenericService().find(BillingAccount.class, share.getAccountId());
            collection = getGenericService().find(ResourceCollection.class, share.getCollectionId());
        }
    }
    
    @Override
    public void validate() {
        if (share == null) {
            addActionError("adhocShareCreateAction.no_share_provided");
        }
        super.validate();
    }
    
    @Override
    public String execute() throws Exception {
        resourceCollectionService.createShareFromAdhoc(share, resources, collection, account, getAuthenticatedUser());
        return SUCCESS;
    }

    public AdhocShare getShare() {
        return share;
    }

    public void setShare(AdhocShare share) {
        this.share = share;
    }
}
