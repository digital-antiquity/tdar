/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.Document;

/**
 * @author Adam Brin
 * 
 */
public class BookmarkControllerITCase extends AbstractAdminControllerITCase {

    @Override
    protected TdarActionSupport getController() {
        return new BookmarkResourceAction();
    }

    @Test
    @Rollback
    public void testBookmarkedResource() {
        Document document = createNewDocument();
        bookmarkResource(document);
        int size = document.getBookmarks().size();
        assertTrue(size > 0);
        bookmarkResource(document);
        assertEquals("something wrong, cannot bookmark item twice", size, document.getBookmarks().size());
    }

    @Test
    @Rollback
    public void testUnBookmarkedResource() {
        Document document = createNewDocument();
        bookmarkResource(document);
        int size = document.getBookmarks().size();
        assertTrue(size > 0);
        removeBookmark(document);
        assertFalse("something wrong, cannot bookmark item twice", size == document.getBookmarks().size());
    }

    @Test
    @Rollback
    public void testAjaxBookmarkedResource() {
        Document document = createNewDocument();
        bookmarkResource(document, true);
        int size = document.getBookmarks().size();
        assertTrue(size > 0);
        bookmarkResource(document);
        assertEquals("something wrong, cannot bookmark item twice", size, document.getBookmarks().size());
    }

    @Test
    @Rollback
    public void testAjaxRemoveBookmarkedResource() {
        Document document = createNewDocument();
        bookmarkResource(document, true);
        int size = document.getBookmarks().size();
        assertTrue(size > 0);
        removeBookmark(document);
        assertFalse("something wrong, cannot bookmark item twice", size == document.getBookmarks().size());
    }

    public Document createNewDocument() {
        Document document = new Document();
        document.markUpdated(getTestPerson());
        document.setTitle("test");
        document.setDescription("bacd");
        genericService.save(document);
        return document;
    }

}
