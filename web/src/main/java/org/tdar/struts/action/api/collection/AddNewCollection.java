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
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
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
// @HttpForbiddenErrorResponseOnly
// @HttpsOnly
@Results(value = { @Result(name = TdarActionSupport.SUCCESS, type = TdarActionSupport.JSONRESULT),
		@Result(name = TdarActionSupport.INPUT, type = TdarActionSupport.JSONRESULT, params = { "stream",
				"jsonInputStream", "statusCode", "500" }) })
public class AddNewCollection extends AbstractJsonApiAction implements Preparable, Validateable {

	private static final long serialVersionUID = 1344077793459231299L;

	@Autowired
	private transient AuthorizationService authorizationService;

	@Autowired
	private transient ResourceCollectionService resourceCollectionService;

	@Autowired
	private transient EntityService entityService;

	private String collectionName;

	private Long collectionId;

	@Action(value = "newcollection", results = { @Result(name = SUCCESS, type = "jsonresult") })
	public String view() throws Exception {
		
		ResourceCollection rc = new ResourceCollection(collectionName, "", getAuthenticatedUser());
		rc.markUpdated(getAuthenticatedUser());
		getGenericService().saveOrUpdate(rc);
		
		collectionId = rc.getId();
		
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("status", "success");
		result.put("name", getCollectionName());
		result.put("id", collectionId);
		
		ObjectMapper mapper = new ObjectMapper();
		setJsonInputStream(new ByteArrayInputStream(mapper.writeValueAsString(result).getBytes()));
		return SUCCESS;
	}

	@Override
	public void validate() {
		super.validate();
		if(getCollectionName() == null || getCollectionName().trim().equals("")){
			addActionError("no name provided");
		}
	}

	@Override
	public void prepare() throws Exception {
	}

	public ResourceCollectionService getResourceCollectionService() {
		return resourceCollectionService;
	}

	public void setResourceCollectionService(ResourceCollectionService resourceCollectionService) {
		this.resourceCollectionService = resourceCollectionService;
	}

	public Long getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
}
