/**
 * 
 * @author Adam Brin
 *
 */

package org.tdar.web;

import org.junit.Test;
import org.tdar.core.configuration.TdarConfiguration;

public class HomepageWebITCase extends AbstractAnonymousWebTestCase {

    @Test
    public void testSuccessfulHomepageLoad() throws Exception {
        gotoPage("/");
        logger.debug(getPageText());
        assertTextPresentInPage(TdarConfiguration.getInstance().getSiteName());
    }
}
