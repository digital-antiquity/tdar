package org.tdar.functional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openqa.selenium.By;

public class UserNotificationSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {

    private static final String RELEASE_ANNOUNCE = "lithic.announce";

    @Test
    public void testDismiss() throws InterruptedException {
        gotoPage("/dashboard");
        assertTrue(getText().contains(RELEASE_ANNOUNCE));
        find(By.id("close_note_1")).click();
        clearPageCache();
        logger.debug(getText());
        assertFalse(getText().contains(RELEASE_ANNOUNCE));
        logout();
        loginEditor();
        clearPageCache();
        logger.debug(getText());
        // we were getting an alert, likely when the browser moved away from the page, prior to a "success" being returned
        Thread.sleep(1000);
        assertFalse(getText().contains(RELEASE_ANNOUNCE));
    }

}
