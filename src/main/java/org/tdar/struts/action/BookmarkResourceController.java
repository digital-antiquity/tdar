package org.tdar.struts.action;

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
import org.tdar.core.service.XmlService;
import org.tdar.core.service.resource.ResourceService;

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
public class BookmarkResourceController extends AuthenticationAware.Base {

    private static final long serialVersionUID = -5396034976314292120L;

    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient XmlService xmlService;

    private Long resourceId;
    private Boolean success = Boolean.FALSE;
    private String callback;
    private InputStream resultJson;

    @Action(value = "bookmarkAjax", results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }) })
    public String bookmarkResourceAjaxAction() {
        success = bookmarkResource();
        processResultToJson();
        return SUCCESS;
    }

    private void processResultToJson() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        setResultJson(new ByteArrayInputStream(xmlService.convertFilteredJsonForStream(result, null, callback).getBytes()));
    }

    @Action(value = "bookmark",
            results = {
                    @Result(name = "success", type = "redirect", location = URLConstants.BOOKMARKS)
            })
    public String bookmarkResourceAction() {
        success = bookmarkResource();
        return SUCCESS;
    }

    @Action(value = "removeBookmarkAjax", results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "resultJson" }) })
    public String removeBookmarkAjaxAction() {
        success = removeBookmark();
        processResultToJson();
        return SUCCESS;
    }

    @Action(value = "removeBookmark",
            results = {
                    @Result(name = "success", type = "redirect", location = URLConstants.BOOKMARKS)
            })
    public String removeBookmarkAction() {
        success = removeBookmark();
        return SUCCESS;
    }

    private boolean bookmarkResource() {
        Resource resource = resourceService.find(resourceId);
        if (resource == null) {
            getLogger().trace("no resource with id: " + resourceId);
            return false;
        }
        TdarUser person = getAuthenticatedUser();
        getLogger().debug("checking if resource is already bookmarked for resource:" + resource.getId());
        return bookmarkedResourceService.bookmarkResource(resource, person);
    }

    private boolean removeBookmark() {
        Resource resource = resourceService.find(resourceId);
        if (resource == null) {
            getLogger().warn("no resource with id: " + resourceId);
            return false;
        }
        getLogger().trace("removing bookmark for resource: " + resource.getId());
        TdarUser person = getAuthenticatedUser();
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
