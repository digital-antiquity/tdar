package org.tdar.struts.action.api.collection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_USERS)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
@Results(value = { @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.JSONRESULT),
		@Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.JSONRESULT, params = { "stream",
				"jsonInputStream", "statusCode", "500" }) })
public class RemoveResourceFromCollectionAction extends AbstractJsonApiAction implements Preparable, Validateable  {

    private static final long serialVersionUID = -4463769039427602490L;
    private Long resourceId;
    private Long collectionId;
    private Resource resource;
    private ResourceCollection collection;
    private CollectionResourceSection type;

    @Autowired
    protected transient SerializationService serializationService;
    
    @Autowired
    protected transient ResourceCollectionService resourceCollectionService;

    @Autowired
    private AuthorizationService authorizationService;
    
    @Override
    public void prepare() throws Exception {
        this.resource = getGenericService().find(Resource.class, resourceId);
        this.collection = getGenericService().find(ResourceCollection.class, collectionId);
    }
    
    @Override
   public void validate() {
        super.validate();
        
        //If the resource is null  - or -
        //if the user doesn't have permission to edit the resource, and its being removed from a managed collection.
        //Then add the action error. (Unmanaged side doesn't necessarily need resource permision, so the check shouldn't apply there).

        //require a valid resource and collection, and the ability to remove from the collection.        
        if(PersistableUtils.isNullOrTransient(resource) || PersistableUtils.isNullOrTransient(collection) || 
        		!authorizationService.canRemoveFromCollection(getAuthenticatedUser(),collection )) {
        	addActionError("addResourceToCollectionAction.no_edit_permission");
        }
        else {
	        if(type == CollectionResourceSection.MANAGED){
	        	if (!authorizationService.canEdit(getAuthenticatedUser(), resource)) {
		            addActionError("addResourceToCollectionAction.no_edit_permission");
		        }
	        	else {
	        		List<Long> ids = resource.getManagedResourceCollections().stream().map( ResourceCollection::getId).collect(Collectors.toList());
	        		if(!ids.contains(collectionId)){
	            		addActionError("RemoveResourceFromCollectionAction.resource_not_in_collection");
	            	}
	        	}
	        }
	        
	        //Verify the 
	        else if(type==CollectionResourceSection.UNMANAGED){
	        	getLogger().debug("RemoveResourceFromCollectionAction: Removing unmanaged resource");
        		List<Long> ids = resource.getUnmanagedResourceCollections().stream().map( ResourceCollection::getId).collect(Collectors.toList());
            	if(!ids.contains(collectionId)){
            		addActionError("RemoveResourceFromCollectionAction.resource_not_in_collection");
            	}
	        }
        }
    }
    
    @WriteableSession
    @PostOnly
    @Action(value="removefromcollection", results = { @Result(name = SUCCESS, type = TdarActionSupport.JSONRESULT) })
    public String removeResourceFromCollection() throws Exception {
        Map<String, Object> jsonResult = new HashMap<String, Object>();
    	try { 
    		resourceCollectionService.removeResourceFromCollection(resource, collection, getAuthenticatedUser(), type);
    		jsonResult.put("status", "success");
    		jsonResult.put("collectionId", collectionId);
    		jsonResult.put("resourceId", resourceId);
    		jsonResult.put("type", type);
    		
    		setResultObject(jsonResult);
    		return SUCCESS;
    	}
    	catch(Throwable e){
    		addActionErrorWithException(e.getMessage(), e);
    		return INPUT;
    	} 
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
