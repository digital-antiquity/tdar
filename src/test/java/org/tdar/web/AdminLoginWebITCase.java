package org.tdar.web;

import org.junit.Test;

/**
 * @author Adam Brin
 *
 */
public class AdminLoginWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    @Test
    public void testAdminLogin() {
        assertTextPresentInPage("Admin");
    }

    @Test
    public void testContributorRequests() {
        gotoPage("/admin/contributors");
        assertTextPresentInPage("Contributor Requests");
    }

    @Test
    public void testRecentActivity() {
        gotoPage("/admin/activity");
        assertTextPresentInPage("Recent Activity");
    }

    @Test
    public void testStatistics() {
        gotoPage("/admin/internal");
        assertTextPresentInPage("Admin Pages");

        gotoPage("/admin/resource");
        assertTextPresentInPage("Resource Statistics");

        gotoPage("/admin/user");
        assertTextPresentInPage("User Statistics");

        gotoPage("/admin/keyword-stats");
        assertTextPresentInPage("Pick a Keyword Type");

        gotoPage("/admin/keyword-stats?keywordType=all");
        assertTextPresentInPage("Controlled Culture Keywords");
    }

    @Test
    public void testAuthManagement() {
        gotoPage("/admin/authority-management/index");
        assertTextPresentInPage("Merge Duplicates");
    }

    @Test
    public void testIndexResources() {
        gotoPage("/admin/searchindex/build");
        assertTextPresentInPage("Build Index");
    }


}
