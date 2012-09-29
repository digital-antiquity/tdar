/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import org.junit.Test;


public class HomepageITCase extends AbstractAnonymousWebTestCase {

	@Test
	public void testSuccessfulLogin() throws Exception {
		assertTextPresentInPage("the Digital Archaeological Record");
	}
}
