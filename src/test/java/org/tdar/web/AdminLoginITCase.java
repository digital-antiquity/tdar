package org.tdar.web;

import org.junit.Test;

/**
 * @author Adam Brin
 *
 */
public class AdminLoginITCase extends AbstractAdminAuthenticatedWebTestCase {
	
	@Test
	public void testAdminLogin() {
		assertTextPresentInPage("Admin");
	}
	
	@Test
	public void testContributorRequests() {
		gotoPage("/admin/contributors");
		assertTextPresentInPage("Contributor Requests");
	}

// deprecated 
//	@Test
//	public void testListAllProjects() {
//		gotoPage("/admin/projects");
//		assertTextPresentInPage("Administrator Dashboard: All Projects");
//	}

	@Test
	public void testRecentActivity() {
		gotoPage("/admin/activity");
		assertTextPresentInPage("Recent Activity");
	}

	@Test
	public void testIndexResources() {
		gotoPage("/searchindex/build");
		assertTextPresentInPage("Build Index");
	}

	

}
