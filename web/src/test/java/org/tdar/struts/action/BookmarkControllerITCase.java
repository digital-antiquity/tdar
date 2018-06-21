/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Document;

/**
 * @author Adam Brin
 * 
 */
public class BookmarkControllerITCase extends AbstractAdminControllerITCase implements TestBookmarkHelper {

    @Test
    @Rollback
    public void testBookmarkedResource() throws Exception {
        Document document = createNewDocument();
        bookmarkResource(document, getUser());
        bookmarkResource(document, getUser());
        assertTrue("something wrong, cannot bookmark item twice", entityService.getBookmarkedResourcesForUser(getUser()).size() == 1);
    }

    @Test
    @Rollback
    public void testUnBookmarkedResource() throws Exception {
        Document document = createNewDocument();
        TdarUser user = genericService.find(TdarUser.class, getUserId());
        bookmarkResource(document, user);
        removeBookmark(document, user);
        user = genericService.find(TdarUser.class, getUserId());
        assertTrue("something wrong, cannot bookmark item twice", entityService.getBookmarkedResourcesForUser(user).size() == 0);
    }

    @Test
    @Rollback
    public void testAjaxBookmarkedResource() throws Exception {
        Document document = createNewDocument();
        bookmarkResource(document, true, getUser());
        bookmarkResource(document, getUser());
        assertTrue("something wrong, cannot bookmark item twice", entityService.getBookmarkedResourcesForUser(getUser()).size() == 1);
    }

    @Test
    @Rollback
    public void testAjaxRemoveBookmarkedResource() throws Exception {
        Document document = createNewDocument();
        TdarUser user = genericService.find(TdarUser.class, getUserId());
        bookmarkResource(document, true, user);
        removeBookmark(document, true, user);
        user = genericService.find(TdarUser.class, getUserId());
        assertTrue("something wrong, cannot bookmark item twice", entityService.getBookmarkedResourcesForUser(user).size() == 0);
    }

    public Document createNewDocument() {
        Document document = new Document();
        document.markUpdated(getUser());
        document.setTitle("test");
        document.setDescription("bacd");
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            TdarUser copyrightHolder = genericService.find(TdarUser.class, 1L);
            document.setCopyrightHolder(copyrightHolder);
        }
        genericService.save(document);
        return document;
    }

}
