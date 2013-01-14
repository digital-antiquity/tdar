/**
 * 
 */
package org.tdar.web;

import org.junit.Before;

/**
 * @author Adam Brin
 *
 */
public abstract class AbstractAdminAuthenticatedWebTestCase extends AbstractAuthenticatedWebTestCase {
   
    @Before
    public void setUp() {
    	loginAdmin();
    }

}
