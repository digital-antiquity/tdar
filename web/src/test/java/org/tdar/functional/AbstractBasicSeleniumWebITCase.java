package org.tdar.functional;

import org.junit.After;
import org.junit.Before;

public class AbstractBasicSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Before
    @Override
    public void login() {
        setScreenshotsAllowed(false);
        if (testRequiresLucene()) {
            super.reindexOnce();
        }
        super.login();
        setIgnoreModals(false);
        setScreenshotsAllowed(true);
    }

    @After
    @Override
    public void logout() {
        // if we're shutting things down after aborted/failed test, don't bug me with formnavigate popups
        setIgnoreModals(true);
        setScreenshotsAllowed(false);
        // the test is over, so screenshots at this point aren't helpful
        super.logout();
    }

}
