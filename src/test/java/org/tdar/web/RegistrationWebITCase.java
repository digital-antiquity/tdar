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
        testRegister(personmap, TERMS.TOS);
        assertCurrentUrlContains("dashboard");
        assertTextNotPresentIgnoreCase("new project");
        gotoPage("/logout");
    }

    @Test
    public void testRegisterContributor() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributor");
        testRegister(personmap, TERMS.BOTH);
        assertCurrentUrlContains("dashboard");
        assertTextPresentIgnoreCase("Start a new Project");
        clickLinkWithText("UPLOAD");
        assertPageTitleEquals("add a new resource");
        gotoPage("/logout");
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testRegisterContributorWithTOS() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributorrr");
        testRegister(personmap, TERMS.BOTH);

    }

}
