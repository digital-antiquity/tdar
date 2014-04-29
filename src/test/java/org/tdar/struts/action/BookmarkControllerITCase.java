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
        return new BookmarkResourceController();
    }

    @Test
    @Rollback
    public void testBookmarkedResource() {
        Document document = createNewDocument();
        bookmarkResource(document,getUser());
        bookmarkResource(document,getUser());
        assertTrue("something wrong, cannot bookmark item twice", getUser().getBookmarkedResources().size() == 1);
    }

    @Test
    @Rollback
    public void testUnBookmarkedResource() {
        Document document = createNewDocument();
        Person user = genericService.find(Person.class, getUserId());
        bookmarkResource(document, user);
        removeBookmark(document, user);
        user = genericService.find(Person.class, getUserId());
        assertTrue("something wrong, cannot bookmark item twice", user.getBookmarkedResources().size() == 0);
    }

    @Test
    @Rollback
    public void testAjaxBookmarkedResource() {
        Document document = createNewDocument();
        bookmarkResource(document, true, getUser());
        bookmarkResource(document, getUser());
        assertTrue("something wrong, cannot bookmark item twice", getUser().getBookmarkedResources().size() == 1);
   }

    @Test
    @Rollback
    public void testAjaxRemoveBookmarkedResource() {
        Document document = createNewDocument();
        Person user = genericService.find(Person.class, getUserId());
        bookmarkResource(document, true, user);
        removeBookmark(document, true, user);
        user = genericService.find(Person.class, getUserId());
        assertTrue("something wrong, cannot bookmark item twice", user.getBookmarkedResources().size() == 0);
    }

    public Document createNewDocument() {
        Document document = new Document();
        document.markUpdated(getUser());
        document.setTitle("test");
        document.setDescription("bacd");
        if (TdarConfiguration.getInstance().getCopyrightMandatory()) {
            Creator copyrightHolder = genericService.find(Person.class, 1L);
            document.setCopyrightHolder(copyrightHolder);
        }
        genericService.save(document);
        return document;
    }

}
