package org.tdar.struts.action;

import static org.junit.Assert.assertNotNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ObjectUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.BookmarkedResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.struts.action.api.resource.BookmarkApiController;
import org.tdar.struts.action.resource.BookmarkResourceController;

import com.opensymphony.xwork2.ActionSupport;

public interface TestBookmarkHelper {

    final Logger logger_ = LoggerFactory.getLogger(TestBookmarkHelper.class);

    default void bookmarkResource(Resource r, TdarUser user) throws Exception {
        bookmarkResource(r, false, user);
    }

    default void removeBookmark(Resource r, TdarUser user) throws Exception {
        removeBookmark(r, false, user);
    }

    default void bookmarkResource(Resource r_, boolean ajax, TdarUser user) throws Exception {
        Resource r = r_;
        if (ajax) {
            BookmarkApiController bookmarkController = generateNewInitializedController(BookmarkApiController.class);
            logger_.info("bookmarking " + r.getTitle() + " (" + r.getId() + ")");
            bookmarkController.setResourceId(r.getId());
            bookmarkController.prepare();
            bookmarkController.bookmarkResourceAjaxAction();
        } else {
            BookmarkResourceController bookmarkController = generateNewInitializedController(BookmarkResourceController.class);
            logger_.info("bookmarking " + r.getTitle() + " (" + r.getId() + ")");
            bookmarkController.setResourceId(r.getId());
            bookmarkController.prepare();
            bookmarkController.bookmarkResourceAction();
        }
        r = getGenericService().find(Resource.class, r.getId());
        assertNotNull(r);
        getGenericService().refresh(user);
        boolean seen = false;
        for (BookmarkedResource b : getEntityService().getBookmarkedResourcesForUser(user)) {
            if (ObjectUtils.equals(b.getResource(), r)) {
                seen = true;
            }
        }
        Assert.assertTrue("should have seen resource in bookmark list", seen);
    }

    @SuppressWarnings("deprecation")
    default void removeBookmark(Resource r, boolean ajax, TdarUser user_) throws Exception {
        TdarUser user = user_;
        boolean seen = false;
        for (BookmarkedResource b : getEntityService().getBookmarkedResourcesForUser(user)) {
            if (ObjectUtils.equals(b.getResource(), r)) {
                seen = true;
            }
        }

        Assert.assertTrue("should have seen resource in bookmark list", seen);
        logger_.info("removing bookmark " + r.getTitle() + " (" + r.getId() + ")");
        if (ajax) {
            BookmarkApiController bookmarkController = generateNewInitializedController(BookmarkApiController.class);
            bookmarkController.setResourceId(r.getId());
            bookmarkController.prepare();
            bookmarkController.removeBookmarkAjaxAction();
        } else {
            BookmarkResourceController bookmarkController = generateNewInitializedController(BookmarkResourceController.class);
            bookmarkController.setResourceId(r.getId());
            bookmarkController.prepare();
            bookmarkController.removeBookmarkAction();
        }
        seen = false;
        getGenericService().synchronize();
        user = getGenericService().find(TdarUser.class, user.getId());
        for (BookmarkedResource b : getEntityService().getBookmarkedResourcesForUser(user)) {
            if (ObjectUtils.equals(b.getResource(), r)) {
                seen = true;
            }
        }
        Assert.assertFalse("should not see resource", seen);
    }

    SessionData getSessionData();

    HttpServletRequest getServletRequest();

    HttpServletRequest getServletPostRequest();

    HttpServletResponse getServletResponse();

    <T extends ActionSupport> T generateNewInitializedController(Class<T> controllerClass, TdarUser user);

    <T extends ActionSupport> T generateNewInitializedController(Class<T> class1);

    EntityService getEntityService();

    GenericService getGenericService();

}
