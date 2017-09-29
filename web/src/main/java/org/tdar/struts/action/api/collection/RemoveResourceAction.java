package org.tdar.struts.action.api.collection;

import java.io.ByteArrayInputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class RemoveResourceAction extends AbstractJsonApiAction implements Preparable {

    /**
     * 
     */
    private static final long serialVersionUID = -5973935361171387952L;
    private Long resourceId;
    private Long collectionId;
    private Resource resource;
    private CollectionResourceSection type;
    private ResourceCollection collection;

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
    

    @PostOnly
    @Action(value="removeResource")
    public String remove() throws Exception {
        
        resourceCollectionService.removeResourceFromCollection(resource, collection, getAuthenticatedUser(), getType());
        setJsonInputStream(new ByteArrayInputStream("SUCCESS".getBytes()));
        return super.execute();
    }

    @Override
    public void prepare() throws Exception {
        this.resource = getGenericService().find(Resource.class, resourceId);
        this.collection = getGenericService().find(ResourceCollection.class, collectionId);
        
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


    public CollectionResourceSection getType() {
        return type;
    }


    public void setType(CollectionResourceSection type) {
        this.type = type;
    }
    
}
