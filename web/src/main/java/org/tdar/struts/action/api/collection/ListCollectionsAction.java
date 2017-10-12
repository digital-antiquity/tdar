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
public class ListCollectionsAction extends AbstractJsonApiAction implements Preparable, Validateable {

	private static final long serialVersionUID = 1344077793459231299L;

	@Autowired
	private transient AuthorizationService authorizationService;

	@Autowired
	private transient ResourceCollectionService resourceCollectionService;

	@Autowired
	private transient EntityService entityService;

	private ResourceCollection resource;

	private class JsonCollection {
		private Long id;
		private String name;

		public JsonCollection(Long id, String name) {
			this.setId(id);
			this.setName(name);
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
	}

	@Action(value = "tree", results = { @Result(name = SUCCESS, type = "jsonresult") })
	public String view() throws Exception {

		/*
		 * if (PersistableUtils.isNullOrTransient(getId()) ||
		 * PersistableUtils.isNullOrTransient(resource)) {
		 * getLogger().debug("input"); return INPUT; }
		 */

		Map<Long, String> map = new HashMap<Long, String>();

		getLogger().trace("parent/ owner collections");
		for (ResourceCollection rc : resourceCollectionService.findParentOwnerCollections(getAuthenticatedUser())) {
			if (rc.isTopLevel()) {
				map.put(rc.getId(),rc.getName());
			}
		}

		getLogger().trace("accessible collections");
		for (ResourceCollection rc : entityService.findAccessibleResourceCollections(getAuthenticatedUser())) {
			if (rc instanceof ResourceCollection) {
				map.put(rc.getId(),rc.getName());
			}
		}

		ObjectMapper mapper = new ObjectMapper();

		List<JsonCollection> list = new ArrayList<JsonCollection>();

		for (Long key : map.keySet()) {
			JsonCollection js = new JsonCollection(key, map.get(key));
			list.add(js);
		}

		setJsonInputStream(new ByteArrayInputStream(mapper.writeValueAsString(list).getBytes()));

		return SUCCESS;
	}

	@Override
	public void validate() {
		super.validate();
		if (PersistableUtils.isNullOrTransient(resource)
				|| !authorizationService.canView(getAuthenticatedUser(), resource)) {
			// addActionError("cannot edit resource");
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

}
