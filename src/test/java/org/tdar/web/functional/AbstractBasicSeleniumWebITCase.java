package org.tdar.web.functional;

import org.junit.After;
import org.junit.Before;

public class AbstractBasicSeleniumWebITCase extends AbstractSeleniumWebITCase {

    @Before
    @Override
    public void login() {
        super.reindexOnce();
        super.login();
    }
    
    @After
    @Override
    public void logout() {
        super.logout();
    }
    
}
