package org.tdar.web;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.junit.MultipleTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleTdarConfigurationRunner.class)
public class RegistrationWebITCase extends AbstractWebTestCase {

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testRegisterNormalUser() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "user");
        personmap.put("requestingContributorAccess", "false");
        testLogin(personmap, true, false, true);
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
        assertPageTitleEquals("Unauthorized");
        gotoPage("/logout");
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testRegisterContributorWithTOS() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributorrr");
        testLogin(personmap, true, true, true);

    }

    @Test
    public void testInvalidView() {
        gotoPage("/account/view?personId=1");
        assertCurrentUrlContains("/login");
        gotoPage("/account/welcome?personId=1");
        assertCurrentUrlContains("/login");
    }
}
