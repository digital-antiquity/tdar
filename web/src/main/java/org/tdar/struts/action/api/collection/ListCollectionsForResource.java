package org.tdar.struts.action.api.collection;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.json.JsonLookupFilter;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_USERS)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
public class ListCollectionsForResource extends AbstractJsonApiAction implements Preparable, Validateable {

	private static final long serialVersionUID = 1344077793459231299L;

	@Autowired
	private transient AuthorizationService authorizationService;

	@Autowired
	private transient ResourceCollectionService resourceCollectionService;

	private Resource resource;
	
    private Long resourceId;

    public Class<?> getJsonView(){
    	return JsonLookupFilter.class;
    }
    
	@Action(value = "resourcecollections", results = { @Result(name = SUCCESS, type = "jsonresult") })
	@Transactional(readOnly=true)
	public String view() throws Exception {
		TdarUser user = getAuthenticatedUser();
		List<ResourceCollection> list = new ArrayList<ResourceCollection>();
		
		for(ResourceCollection resourceCollection : resource.getManagedResourceCollections()){
			getLogger().debug("Checking collection {}",resourceCollection.getName());
			if(authorizationService.canEdit(user, resourceCollection)){
				getLogger().debug("Adding collection {}",resourceCollection.getName());
				list.add(resourceCollection);
			}
		}
		
		for(ResourceCollection resourceCollection : resource.getUnmanagedResourceCollections()){
			getLogger().debug("Checking collection {}",resourceCollection.getName());
			if(authorizationService.canEdit(user, resourceCollection)){
				getLogger().debug("Adding collection {}",resourceCollection.getName());
				list.add(resourceCollection);
			}
		}
		
		setResultObject(list);

		return SUCCESS;
	}

	@Override
	public void validate() {
		super.validate();
		
		if (PersistableUtils.isNullOrTransient(resource) || !authorizationService.canView(getAuthenticatedUser(), resource)) {
			 addActionError("cannot edit resource");
		}
	}

	@Override
	public void prepare() throws Exception {
		resource = getGenericService().find(Resource.class, resourceId);
	}

	public ResourceCollectionService getResourceCollectionService() {
		return resourceCollectionService;
	}

	public void setResourceCollectionService(ResourceCollectionService resourceCollectionService) {
		this.resourceCollectionService = resourceCollectionService;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

}
