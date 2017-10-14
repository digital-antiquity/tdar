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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.utils.PersistableUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Namespace("/api/collection")
@Component
@Scope("prototype")
@ParentPackage("secured")
@RequiresTdarUserGroup(TdarGroup.TDAR_API_USER)
// @HttpForbiddenErrorResponseOnly
// @HttpsOnly
public class ListCollectionsForResource extends AbstractJsonApiAction implements Preparable, Validateable {

	private static final long serialVersionUID = 1344077793459231299L;

	@Autowired
	private transient AuthorizationService authorizationService;

	@Autowired
	private transient ResourceCollectionService resourceCollectionService;

	@Autowired
	private transient EntityService entityService;

	private Resource resource;
	
    private Long resourceId;
	

	private class JsonCollection {
		private Long id;
		private String name;
		private boolean owned; 

		public JsonCollection(Long id, String name, boolean owned) {
			this.setId(id);
			this.setName(name);
			this.setOwned(owned);
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isOwned() {
			return owned;
		}

		public void setOwned(boolean owned) {
			this.owned = owned;
		}
	}

	@Action(value = "resourcecollections", results = { @Result(name = SUCCESS, type = "jsonresult") })
	public String view() throws Exception {

		/*
		 * if (PersistableUtils.isNullOrTransient(getId()) ||
		 * PersistableUtils.isNullOrTransient(resource)) {
		 * getLogger().debug("input"); return INPUT; }
		 */

		Map<Long, JsonCollection> map = new HashMap<Long, JsonCollection>();

		getLogger().trace("parent/ owner collections");
		for (ResourceCollection rc : resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser())) {
			if (rc.isTopLevel()) {
					if(rc.getResourceIds().contains(resourceId) || rc.getUnmanagedResourceIds().contains(resourceId)) {
						map.put(rc.getId(), new JsonCollection(rc.getId(), rc.getName(), authorizationService.canEdit(getAuthenticatedUser(), rc)));
					}
			}
		}
		
		getLogger().trace("accessible collections");
		for (ResourceCollection rc : entityService.findAccessibleResourceCollections(getAuthenticatedUser())) {
			if (rc instanceof ResourceCollection && !map.containsKey(rc.getId())) {
				if(rc.getResourceIds().contains(resourceId) || rc.getUnmanagedResourceIds().contains(resourceId)) {
					map.put(rc.getId(), new JsonCollection(rc.getId(), rc.getName(), authorizationService.canEdit(getAuthenticatedUser(), rc)));
				}
			}
		}

		ObjectMapper mapper = new ObjectMapper();
		List<JsonCollection> list = new ArrayList<JsonCollection>();
		
		list.addAll(map.values());
		
		setJsonInputStream(new ByteArrayInputStream(mapper.writeValueAsString(list).getBytes()));
		return SUCCESS;
	}

	@Override
	public void validate() {
		super.validate();
		resource = getGenericService().find(Resource.class, resourceId);
		
		if (PersistableUtils.isNullOrTransient(resource) || !authorizationService.canView(getAuthenticatedUser(), resource)) {
			 addActionError("cannot edit resource");
		}
	}

	@Override
	public void prepare() throws Exception {
		/*
		 * if (PersistableUtils.isNotNullOrTransient(getId())) { resource =
		 * getGenericService().find(ResourceCollection.class, getId()); if
		 * (resource == null) { getLogger().debug("could not find resource: {}",
		 * getId()); } String title = "no title"; if (resource instanceof
		 * ResourceCollection) { title = ((ResourceCollection)
		 * resource).getTitle(); } logMessage("API VIEWING",
		 * resource.getClass(), resource.getId(), title);
		 * getXmlResultObject().setCollectionResult(resource); }
		 */

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
