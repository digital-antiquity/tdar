package org.tdar.web;

import org.junit.Test;
import org.tdar.TestConstants;
import org.tdar.web.resource.EditWebITCase;

/**
 * @author Adam Brin
 * 
 */
public class AdminLoginWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private static final String ENTITY_INSTITUTION_EDIT = "/entity/institution/%s/edit";

    @Test
    public void testInstitutionEdit() {
        gotoPage(String.format(ENTITY_INSTITUTION_EDIT, TestConstants.TEST_INSTITUTION_ID));
        submitForm();
    }

    @Test
    public void testAdminLogin() {
        assertTextPresentInCode("Admin");
    }

    @Test
    public void testRecentActivity() {
        gotoPage("/admin/activity");
        assertTextPresentInPage("Recent Activity");
    }

    @Test
    public void testPageStats() {
        gotoPage("/project/" + EditWebITCase.PROJECT_ID);
        clickLinkOnPage("ADMIN");
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
    public void testAdminActivity() {
        gotoPage("/admin/internal");
        clickLinkOnPage("System Activity");
        assertTextPresentInPage("Recent System Activity");
    }

    @Test
    public void testAuthManagement() {
        gotoPage("/admin/authority/index");
        assertTextPresentInPage("Merge Duplicates");
    }

    @Test
    public void testIndexResources() {
        gotoPage("/admin/searchindex/build");
        assertTextPresentInPage("Build Index");
    }

}
