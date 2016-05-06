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
    private List<Resource> resources;
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

    @Override
    public void prepare() throws Exception {
        exportProxy = new ResourceExportProxy(getAuthenticatedUser());
        resources = getGenericService().findAll(Resource.class, ids);
        getExportProxy().setAccount(getGenericService().find(BillingAccount.class, accountId));
        getExportProxy().setCollection(getGenericService().find(ResourceCollection.class, collectionId));
        List<String> issues = new ArrayList<>();
        if (resources != null) {
            for (Resource r : resources) {
                if (!authorizationService.canEditResource(getAuthenticatedUser(), r, GeneralPermissions.MODIFY_METADATA)) {
                    issues.add(String.format("%s (%s)", r.getTitle(), r.getId()));
                }
            }
        }

        if (getExportProxy().getCollection() != null && !authorizationService.canEditCollection(getAuthenticatedUser(), getExportProxy().getCollection())) {
            addActionError(getText("abstractResourceExportAction.cannot_export", Arrays.asList(getExportProxy().getCollection())));
        }

        if (getExportProxy().getAccount() != null && !authorizationService.canEditAccount(getExportProxy().getAccount(), getAuthenticatedUser())) {
            addActionError(getText("abstractResourceExportAction.cannot_export", Arrays.asList(getExportProxy().getAccount())));
        }

        if (CollectionUtils.isNotEmpty(resources)) {
            addActionError(getText("abstractResourceExportAction.cannot_export", Arrays.asList(issues)));
        }
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
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

    public ResourceExportProxy getExportProxy() {
        return exportProxy;
    }

    public void setExportProxy(ResourceExportProxy exportProxy) {
        this.exportProxy = exportProxy;
    }

}
