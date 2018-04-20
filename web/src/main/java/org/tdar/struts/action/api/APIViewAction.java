package org.tdar.struts.action.api;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.ObfuscationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;

@Namespace("/api")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class APIViewAction extends AbstractApiController {

    private static final long serialVersionUID = 539604938603061219L;

    @Autowired
    private transient ObfuscationService obfuscationService;
    @Autowired
    private transient ResourceService resourceService;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Action(value = "view", results = {
            @Result(name = SUCCESS, type = TdarActionSupport.XMLDOCUMENT) })
    public String view() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(getId())) {
            Resource resource = resourceService.find(getId());
            if (resource == null) {
                getLogger().debug("could not find resource: {}", getId());
                return INPUT;
            }
            if (!isAdministrator() && !authorizationService.canEdit(getAuthenticatedUser(), resource)) {
                obfuscationService.obfuscate(resource, getAuthenticatedUser());
            }
            logMessage("API VIEWING", resource.getClass(), resource.getId(), resource.getTitle());
            getResultObject().setResult(resource);
            return SUCCESS;
        }
        return INPUT;
    }

}
