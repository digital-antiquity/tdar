package org.tdar.web;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.tdar.junit.RunWithTdarConfiguration;

/**
 * @author Adam Brin
 * 
 */
public class RegistrationWebITCase extends AbstractWebTestCase {

    @Test
    public void testRegisterNormalUser() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "user");
        personmap.put("requestingContributorAccess", "false");
        testLogin(personmap, true);
        assertTextNotPresent("Create a new project");
        gotoPage("/logout");
    }

    @Test
    public void testRegisterContributor() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributor");
        testLogin(personmap, true);
        assertTextPresent("Create a new project");
        clickLinkWithText("UPLOAD");
        gotoPage("/account/view?personId=1");
        assertPageTitleEquals("Page not found");
        gotoPage("/logout");
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testRegisterContributorWithTOS() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributor");
        testLogin(personmap, true);
        
    }
    
    @Test
    public void testInvalidView() {
        gotoPage("/account/view?personId=1");
        assertCurrentUrlContains("/login");
        gotoPage("/account/welcome?personId=1");
        assertCurrentUrlContains("/login");
    }
}
