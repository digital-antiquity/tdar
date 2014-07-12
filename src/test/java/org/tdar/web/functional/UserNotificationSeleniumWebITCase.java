package org.tdar.web.functional;

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
        assertFalse(getText().contains(RELEASE_ANNOUNCE));
        logout();
        loginEditor();
        assertFalse(getText().contains(RELEASE_ANNOUNCE));
    }

}
