package org.tdar.functional;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AlertSeleniumWebITCase extends AbstractSeleniumWebITCase {

    /**
     * This test was mainly setup to be able to consistently handle an alert in a way that produces an error (so we can avoid them)
     * 
     */
    @Test
    public void test() {
        gotoPage("file:///Users/abrin/Desktop/alert.html");
    }
}
