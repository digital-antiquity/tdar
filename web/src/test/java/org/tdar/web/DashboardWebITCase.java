package org.tdar.web;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.web.collection.CollectionWebITCase;

public class DashboardWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String MICHELLE_ELLIOT = "Michelle Elliott";

    @Test
    public void testRightsPage() {
        gotoPage("/dashboard");
        clickLinkOnPage("Export");
    }

    @Test
    public void testCollectionsAndRightsPage() {
        Long id = createResourceFromType(ResourceType.GEOSPATIAL, "test geospatial");
        clickLinkOnPage(CollectionWebITCase.PERMISSIONS);
        setInput("proxies[0].id", "121");
        setInput("proxies[1].id", "5349");
        setInput("proxies[0].permission", Permissions.MODIFY_RECORD.name());
        setInput("proxies[1].permission", Permissions.VIEW_ALL.name());
        setInput("proxies[0].displayName", MICHELLE_ELLIOT);
        setInput("proxies[1].displayName", "Joshua Watts");
        submitForm();
        logger.debug(getPageText());
        gotoPage("/dashboard");
        clickLinkOnPage("Collections");
        logger.debug(getPageText());
        assertTrue("page contains margeret", getPageText().toLowerCase().contains("margeret"));
        assertTrue("page contains beatrice", getPageText().toLowerCase().contains("beatrice"));
        clickElementWithId("p12550");
    }

    @Test
    public void testBookmarksPage() {
        gotoPage("/dashboard");
        clickLinkOnPage("Bookmarks");
    }

    @Test
    public void testProfilePage() {
        gotoPage("/dashboard");
        clickLinkOnPage("My Profile");
    }
}