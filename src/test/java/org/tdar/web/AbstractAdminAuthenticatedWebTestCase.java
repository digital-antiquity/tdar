/**
 * 
 */
package org.tdar.web;

import static org.junit.Assert.fail;

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

    //fixme: this woudl be handy to have one level up but we need to be an admin... 
    protected void reindex() {
        gotoPage("/admin/searchindex/build");
        gotoPage("/admin/searchindex/checkstatus");
        logger.info(getPageCode());
        int count = 0;
        while (!getPageCode().contains("\"percentDone\" : 100")) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail("InterruptedException during reindex.  sorry.");
            }
            gotoPage("/admin/searchindex/checkstatus");
            logger.info(getPageCode());
            if (count == 1000) {
                fail("we went through 1000 iterations of waiting for the search index to build... assuming something is wrong");
            }
            count++;
        }
    }
    
    
}
