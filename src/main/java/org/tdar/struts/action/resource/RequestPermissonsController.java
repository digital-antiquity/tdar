package org.tdar.struts.action.resource;

import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.CrudAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/resource")
@Component
@Scope("prototype")
public class RequestPermissonsController extends AuthenticationAware.Base implements Preparable, CrudAction<Resource> {

    private static final long serialVersionUID = 1L;
    private Long requestorId;
    private Long resourceId;
    private TdarUser requestor;
    private GeneralPermissions permission;
    private Resource resource;
    private List<GeneralPermissions> availablePermissions = Arrays.asList(GeneralPermissions.values());
    @Autowired
    private transient GenericService genericService;
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

    @Override
    public void prepare() throws Exception {
        requestor = genericService.find(TdarUser.class, requestorId);
        if (requestor == null) {
            addActionError("requestPermissionsController.require_user");
        }
        resource = genericService.find(Resource.class, resourceId);
        if (resource == null) {
            addActionError("requestPermissionsController.cannot_find_resource");
        }
    }

    @Action(value = "request-access",
            results = {
                    @Result(name = SUCCESS, location = "../resource/request-access.ftl")
            })
    @HttpsOnly
    public String requestAccess() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        return SUCCESS;
    }

    @Action(value = "process-access-request",
            interceptorRefs = { @InterceptorRef("csrfAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TYPE_REDIRECT, location = "/${resource.urlNamespace}/${resource.id}")
            })
    @PostOnly
    @WriteableSession
    @HttpsOnly
    public String processAccessRequest() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (getPermission() == null) {
            addActionError("requestPermissionsController.specify_permission");
        }
        resourceCollectionService.addUserToInternalCollection(getResource(), requestor, getPermission());
        addActionMessage(getText("requestPermissionsController.success",Arrays.asList(requestor.getProperName(), permission.getLabel())));
        return SUCCESS;
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
    public boolean isCreatable() throws TdarActionException {
        return false;
    }

    @Override
    public boolean isEditable() throws TdarActionException {
        return getAuthenticationAndAuthorizationService().canEditResource(getAuthenticatedUser(), getResource(), GeneralPermissions.MODIFY_METADATA);
    }

    @Override
    public boolean isSaveable() throws TdarActionException {
        return isEditable();
    }

    @Override
    public boolean isDeleteable() throws TdarActionException {
        return false;
    }

    @Override
    public boolean isViewable() throws TdarActionException {
        return false;
    }

    @Override
    public Persistable getPersistable() {
        return getResource();
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

}
