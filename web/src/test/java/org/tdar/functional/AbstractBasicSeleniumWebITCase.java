package org.tdar.functional;

import java.io.IOException;

import org.junit.Before;

public class AbstractBasicSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Before
    public void runBefore() throws IOException {
        super.beforeTest();
        login();
    }

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

    @Override
    public void logout() {
        // if we're shutting things down after aborted/failed test, don't bug me with formnavigate popups
        setIgnoreModals(true);
        setScreenshotsAllowed(false);
        // the test is over, so screenshots at this point aren't helpful
        super.logout();
    }

}
