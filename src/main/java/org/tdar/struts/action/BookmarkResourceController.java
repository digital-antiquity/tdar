package org.tdar.struts.action;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.URLConstants;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Resource;
import org.tdar.struts.interceptor.annotation.WriteableSession;

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
@WriteableSession
public class BookmarkResourceController extends AuthenticationAware.Base {

    private static final long serialVersionUID = -5396034976314292120L;

    private Long resourceId;
    private Boolean success = Boolean.FALSE;
    private String callback;

    @Action(
            value = "bookmarkAjax",
            results = {
                    @Result(name = "success", type = "freemarker", location = "bookmark.ftl", params = { "contentType", "application/json" })
            }
            )
            public String bookmarkResourceAjaxAction() {
        success = bookmarkResource();
        return SUCCESS;
    }

    @Action(
            value = "bookmark",
            results = {
                    @Result(name = "success", type = "redirect", location = URLConstants.BOOKMARKS)
            }
            )
            public String bookmarkResourceAction() {
        success = bookmarkResource();
        return SUCCESS;
    }

    @Action(
            value = "removeBookmarkAjax",
            results = {
                    @Result(name = "success", type = "freemarker", location = "bookmark.ftl", params = { "contentType", "application/json" })
            }
            )
            public String removeBookmarkAjaxAction() {
        success = removeBookmark();
        return SUCCESS;
    }

    @Action(
            value = "removeBookmark",
            results = {
                    @Result(name = "success", type = "redirect", location = URLConstants.BOOKMARKS)
            }
            )
            public String removeBookmarkAction() {
        success = removeBookmark();
        return SUCCESS;
    }

    private boolean bookmarkResource() {
        Resource resource = getResourceService().find(resourceId);
        if (resource == null) {
            getLogger().trace("no resource with id: " + resourceId);
            return false;
        }
        TdarUser person = getAuthenticatedUser();
        getLogger().debug("checking if resource is already bookmarked for resource:" + resource.getId());
        return getBookmarkedResourceService().bookmarkResource(resource, person);
    }

    private boolean removeBookmark() {
        Resource resource = getResourceService().find(resourceId);
        if (resource == null) {
            getLogger().warn("no resource with id: " + resourceId);
            return false;
        }
        getLogger().trace("removing bookmark for resource: " + resource.getId());
        TdarUser person = getAuthenticatedUser();
        return getBookmarkedResourceService().removeBookmark(resource, person);
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

}
