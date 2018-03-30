package org.tdar.struts.action.api.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.HttpForbiddenErrorResponseOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Bookmarks resource actions
 * 
 * @author Matt Cordial
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/api/resource")
@Component
@Scope("prototype")
public class BookmarkApiController extends AbstractAuthenticatableAction implements Preparable {

    /**
     * 
     */
    private static final long serialVersionUID = -852207996698591723L;

    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient SerializationService serializationService;

    private Long resourceId;
    private Boolean success = Boolean.FALSE;
    private String callback;
    private InputStream resultJson;

    private Resource resource;

    private TdarUser person;

    @Action(value = "bookmarkAjax", results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }) })
    @HttpForbiddenErrorResponseOnly
    @PostOnly
    public String bookmarkResourceAjaxAction() {
        getLogger().debug("checking if resource is already bookmarked for resource:" + resource.getId());
        success = bookmarkedResourceService.bookmarkResource(resource, person);
        processResultToJson();
        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {
        resource = resourceService.find(resourceId);
        person = getAuthenticatedUser();
        if (resource == null) {
            addActionError(getText("bookmarkResourceController.no_resource"));
        }
        if (person == null) {
            addActionError(getText("bookmarkResourceController.no_user"));
        }
    }

    private void processResultToJson() {
        Map<String, Object> result = new HashMap<>();
        result.put(SUCCESS, success);
        setResultJson(new ByteArrayInputStream(serializationService.convertFilteredJsonForStream(result, null, callback).getBytes()));
    }

    @Action(value = "removeBookmarkAjax", results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }) })
    @PostOnly
    @HttpForbiddenErrorResponseOnly
    public String removeBookmarkAjaxAction() {
        getLogger().trace("removing bookmark for resource: " + resource.getId());
        success = bookmarkedResourceService.removeBookmark(resource, person);
        processResultToJson();
        return SUCCESS;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getCallback() {
        return callback;
    }

    public InputStream getResultJson() {
        return resultJson;
    }

    public void setResultJson(InputStream resultJson) {
        this.resultJson = resultJson;
    }

}
