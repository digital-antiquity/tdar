package org.tdar.struts.action.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceExportProxy;
import org.tdar.core.service.resource.ResourceExportService;
import org.tdar.struts.action.AbstractAuthenticatableAction;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/export")
public abstract class AbstractResourceExportAction extends AbstractAuthenticatableAction implements Preparable {

    /**
     * 
     */
    private static final long serialVersionUID = 6309093261229203299L;
    private List<Long> ids;
    private Long collectionId;
    private Long accountId;

    private ResourceExportProxy exportProxy;

    @Autowired
    ResourceExportService resourceExportService;

    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    SerializationService serializationService;

    private String format(HasName item) {
        return String.format("%s (%s)", item.getName(), item.getId());
    }
    
    @Override
    public void prepare() throws Exception {
        exportProxy = new ResourceExportProxy(getAuthenticatedUser());
        List<Resource> resources = getGenericService().findAll(Resource.class, ids);
        getExportProxy().setAccount(getGenericService().find(BillingAccount.class, accountId));
        getExportProxy().setCollection(getGenericService().find(ResourceCollection.class, collectionId));
        List<String> issues = new ArrayList<>();
        if (resources != null) {
            for (Resource r : resources) {
                if (!authorizationService.canEditResource(getAuthenticatedUser(), r, GeneralPermissions.MODIFY_METADATA)) {
                    issues.add(format(r));
                }
            }
        }

        ResourceCollection collection = getExportProxy().getCollection();
        if (collection != null && !authorizationService.canEditCollection(getAuthenticatedUser(), collection)) {
            addActionError(getText("abstractResourceExportAction.cannot_export", Arrays.asList(format(collection))));
        }

        BillingAccount account = getExportProxy().getAccount();
        if (account != null && !authorizationService.canEditAccount(account, getAuthenticatedUser())) {
            addActionError(getText("abstractResourceExportAction.cannot_export", Arrays.asList(format(account))));
        }

        if (CollectionUtils.isNotEmpty(resources)) {
            addActionError(getText("abstractResourceExportAction.cannot_export"));
        }
    }


    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCollectionId() {
        return collectionId;
    }
    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }

    protected ResourceExportProxy getExportProxy() {
        return exportProxy;
    }


}
