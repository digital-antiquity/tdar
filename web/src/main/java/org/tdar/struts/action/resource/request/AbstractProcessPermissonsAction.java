package org.tdar.struts.action.resource.request;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;

import com.opensymphony.xwork2.Preparable;

public abstract class AbstractProcessPermissonsAction extends AuthenticationAware.Base implements Preparable, PersistableLoadingAction<Resource> {

    private static final long serialVersionUID = 6775247968159166454L;
    private Long requestorId;
    private Long resourceId;
    private TdarUser requestor;
    private GeneralPermissions permission;
    private Resource resource;
    private List<GeneralPermissions> availablePermissions = Arrays.asList(GeneralPermissions.values());
    @Autowired
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient GenericService genericService;
    
    @Override
    public void prepare() throws Exception {
        requestor = genericService.find(TdarUser.class, requestorId);
        if (requestor == null) {
            addActionError("requestPermissionsController.require_user");
        }
        prepareAndLoad(this, RequestType.EDIT);
        checkValidRequest(this);

        resource = genericService.find(Resource.class, resourceId);
        if (resource == null) {
            addActionError("requestPermissionsController.cannot_find_resource");
        }
    }


    public Long getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(Long requestorId) {
        this.requestorId = requestorId;
    }

    public GeneralPermissions getPermission() {
        return permission;
    }

    public void setPermission(GeneralPermissions permission) {
        this.permission = permission;
    }

    public TdarUser getRequestor() {
        return requestor;
    }

    public void setRequestor(TdarUser requestor) {
        this.requestor = requestor;
    }

    @Override
    public boolean authorize() throws TdarActionException {
        return authorizationService.canEditResource(getAuthenticatedUser(), getResource(), GeneralPermissions.MODIFY_METADATA);
    }

    @Override
    public Resource getPersistable() {
        return getResource();
    }

    @Override
    public void setPersistable(Resource persistable) {
        this.resource = persistable;
    }

    @Override
    public Class<Resource> getPersistableClass() {
        return Resource.class;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public List<GeneralPermissions> getAvailablePermissions() {
        return availablePermissions;
    }

    public void setAvailablePermissions(List<GeneralPermissions> availablePermissions) {
        this.availablePermissions = availablePermissions;
    }

    @Override
    public Long getId() {
        return resourceId;
    }

    @Override
    public InternalTdarRights getAdminRights() {
        return InternalTdarRights.EDIT_ANYTHING;
    }

}
