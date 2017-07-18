package org.tdar.struts.action.resource;

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
import org.tdar.URLConstants;
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
@Namespace("/resource")
@Component
@Scope("prototype")
public class BookmarkResourceController extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -5396034976314292120L;

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
        success = bookmarkResource();
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

    @Action(value = "bookmark",
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = URLConstants.BOOKMARKS)
            })
    public String bookmarkResourceAction() {
        success = bookmarkResource();
        return SUCCESS;
    }

    @Action(value = "removeBookmarkAjax", results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }) })
    @PostOnly
    @HttpForbiddenErrorResponseOnly
    public String removeBookmarkAjaxAction() {
        success = removeBookmark();
        processResultToJson();
        return SUCCESS;
    }

    @Action(value = "removeBookmark",
            results = {
                    @Result(name = SUCCESS, type = TDAR_REDIRECT, location = URLConstants.BOOKMARKS)
            })
    public String removeBookmarkAction() {
        success = removeBookmark();
        return SUCCESS;
    }

    private boolean bookmarkResource() {
        getLogger().debug("checking if resource is already bookmarked for resource:" + resource.getId());
        return bookmarkedResourceService.bookmarkResource(resource, person);
    }

    private boolean removeBookmark() {
        getLogger().trace("removing bookmark for resource: " + resource.getId());
        return bookmarkedResourceService.removeBookmark(resource, person);
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
