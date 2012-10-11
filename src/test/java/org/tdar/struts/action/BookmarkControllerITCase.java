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
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.configuration.TdarConfiguration;

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
        document.markUpdated(getUser());
        document.setTitle("test");
        document.setDescription("bacd");
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            Creator copyrightHolder = genericService.find(Person.class, 1L);
            document.setCopyrightHolder(copyrightHolder );
        }
        genericService.save(document);
        return document;
    }

}
