package org.tdar.struts.action.api.collection;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.tdar.core.service.EntityService;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;
import com.sun.tools.doclets.formats.html.SectionName;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
@HttpForbiddenErrorResponseOnly
@HttpsOnly
@Results(value = { @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.JSONRESULT),
		@Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.JSONRESULT, params = { "stream",
				"jsonInputStream", "statusCode", "500" }) })
public class AddResourceToCollectionAction extends AbstractJsonApiAction implements Preparable, Validateable {

	private static final long serialVersionUID = 1344077793459231299L;

	@Autowired
	private transient AuthorizationService authorizationService;

	@Autowired
	private transient ResourceCollectionService resourceCollectionService;

	private Resource resource;

	private Long resourceId;

	private Long collectionId;

	private Boolean addAsManagedResource = false;

	private ResourceCollection resourceCollection;
	
	private Map<String, Object> jsonResult = new HashMap<String, Object>();
	

	@Action(value = "addtocollection", results = { @Result(name = SUCCESS, type = TdarActionSupport.JSONRESULT) })
	@WriteableSession
	@PostOnly
	public String view() throws Exception {
		//verify they have permissions to the resource
		getJsonResult().put("status", "failure");
		
		//TODO change to TdarMessage
		getJsonResult().put("reason", "no permission to edit resource");
		
		//if they want to add as managed resource
		if (addAsManagedResource && authorizationService.canEdit(getAuthenticatedUser(), resource)){
				resourceCollection.getManagedResources().add(resource);
				getGenericService().saveOrUpdate(resourceCollection.getManagedResources());
				getJsonResult().put("status", "success");
				getJsonResult().put("reason", "");
				getJsonResult().put("resourceId", resourceId);
				getJsonResult().put("collectionId", collectionId);
		}
		//verify that they can add it to the requested collection
		else if(authorizationService.canAddToCollection(getAuthenticatedUser(), resourceCollection)) {
				resourceCollection.getUnmanagedResources().add(resource);
				getGenericService().saveOrUpdate(resourceCollection.getUnmanagedResources());
				getJsonResult().put("status", "success");
				getJsonResult().put("reason", "");
				getJsonResult().put("resourceId", resourceId);
				getJsonResult().put("collectionId", collectionId);
		}
		
		return SUCCESS;
	}

	@Override
	public void validate() {
		super.validate();

		if (PersistableUtils.isNullOrTransient(resource)
				|| !authorizationService.canView(getAuthenticatedUser(), resource)) {
			addActionError("cannot edit resource");
		}

		if (PersistableUtils.isNullOrTransient(resourceCollection)
				|| !authorizationService.canView(getAuthenticatedUser(), resourceCollection)) {
			addActionError("no access to collection");
		}
	
		if(!authorizationService.canAddToCollection(getAuthenticatedUser(), resourceCollection )){
			addActionError("can't add items to collection");
		}
	}

	@Override
	public void prepare() throws Exception {
		super.prepare();
		resource = getGenericService().find(Resource.class, resourceId);
		resourceCollection = getGenericService().find(ResourceCollection.class, collectionId);
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

	public Long getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}

	public Boolean getAddAsManagedResource() {
		return addAsManagedResource;
	}

	public void setAddAsManagedResource(Boolean addAsManagedResource) {
		this.addAsManagedResource = addAsManagedResource;
	}

	public Map<String, Object> getJsonResult() {
		return jsonResult;
	}

	public void setJsonResult(Map<String, Object> jsonResult) {
		this.jsonResult = jsonResult;
	}

}
