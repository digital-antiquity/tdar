package org.tdar.struts.action.api.collection;

import java.io.ByteArrayInputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@HttpForbiddenErrorResponseOnly
@HttpsOnly
@Results(value = {
        @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.JSONRESULT, params = { "stream", "jsonInputStream" }),
        @Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.JSONRESULT, params = { "stream", "jsonInputStream", "statusCode", "500" })
})
public class RemoveResourceFromCollectionAction extends AbstractJsonApiAction implements Preparable {

    private static final long serialVersionUID = -4463769039427602490L;
    private Long resourceId;
    private Long collectionId;
    private Resource resource;
    private VisibleCollection collection;

    @Autowired
    protected transient SerializationService serializationService;
    
    @Autowired
    protected transient ResourceCollectionService resourceCollectionService;

    @Autowired
    private AuthorizationService authorizationService;
    
    @Override
    public void validate() {
        super.validate();
        if (PersistableUtils.isNullOrTransient(resource) || !authorizationService.canEdit(getAuthenticatedUser(), resource)) {
            addActionError("cannot edit resource");
        }
        if (PersistableUtils.isNullOrTransient(collection) || !authorizationService.canEdit(getAuthenticatedUser(), collection)) {
            addActionError("cannot edit to colection");
        }
    }
    
    @Override
    @WriteableSession
    @PostOnly
    @Action(value="removeResource")
    public String execute() throws Exception {
        resourceCollectionService.removeResourceFromCollection(resource, collection, getAuthenticatedUser());
        setJsonInputStream(new ByteArrayInputStream("{\"status\":\"success\"}".getBytes()));
        return super.execute();
    }


    @Override
    public void prepare() throws Exception {
        this.resource = getGenericService().find(Resource.class, resourceId);
        this.collection = getGenericService().find(VisibleCollection.class, collectionId);
        
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
        this.collectionId = collectionId;
    }
    
}
