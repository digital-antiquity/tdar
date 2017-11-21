package org.tdar.struts.action.api.collection;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.api.AbstractApiController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class CollectionApiViewAction extends AbstractApiController implements Preparable, Validateable {


    private static final long serialVersionUID = 1344077793459231299L;
    @Autowired
    private transient AuthorizationService authorizationService;

    private ResourceCollection resource;

    @Action(value = "view", results = {
            @Result(name = SUCCESS, type = TdarActionSupport.XMLDOCUMENT) })
    public String view() throws Exception {
        if (PersistableUtils.isNullOrTransient(getId()) || PersistableUtils.isNullOrTransient(resource)) {
            getLogger().debug("input");
                return INPUT;
        }
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNullOrTransient(resource) || !authorizationService.canView(getAuthenticatedUser(), resource)) {
            addActionError("addResourceToCollectionAction.no_edit_permission");
        }
    }


    @Override
    public void prepare() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(getId())) {
            resource = getGenericService().find(ResourceCollection.class, getId());
            if (resource == null) {
                getLogger().debug("could not find resource: {}", getId());
            }
            String title = "no title";
            if (resource instanceof ResourceCollection) {
                title = ((ResourceCollection) resource).getTitle();
            }
            logMessage("API VIEWING", resource.getClass(), resource.getId(), title);
            getResultObject().setCollectionResult(resource);
        }
        
    }

}
