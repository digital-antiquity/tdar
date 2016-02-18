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
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.AbstractPersistableController.RequestType;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.action.PersistableLoadingAction;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/resource")
@Component
@Scope("prototype")
public class RequestPermissonsController extends AuthenticationAware.Base implements Preparable, PersistableLoadingAction<Resource> {

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
    @Autowired
    private transient ResourceCollectionService resourceCollectionService;

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

    @Action(value = "request-access",
            results = {
                    @Result(name = SUCCESS, location = "../resource/request-access.ftl"),
                    @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" }),
                    @Result(name = INPUT, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" })
            })
    @HttpsOnly
    public String requestAccess() throws TdarActionException {
        // checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        return SUCCESS;
    }

    @Action(value = "process-access-request",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TYPE_REDIRECT, location = "/${resource.urlNamespace}/${resource.id}/${resource.slug}"),
                    @Result(name = ERROR, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" }),
                    @Result(name = INPUT, type = TdarActionSupport.FREEMARKERHTTP, location = "/WEB-INF/content/errors/error.ftl",
                            params = { "status", "500" })
            })
    @PostOnly
    @WriteableSession
    @HttpsOnly
    public String processAccessRequest() throws TdarActionException {
        // checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        if (getPermission() == null) {
            addActionError("requestPermissionsController.specify_permission");
            return ERROR;
        }
        resourceCollectionService.addUserToInternalCollection(getResource(), requestor, getPermission());
        addActionMessage(getText("requestPermissionsController.success", Arrays.asList(requestor.getProperName(), permission.getLabel())));
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
