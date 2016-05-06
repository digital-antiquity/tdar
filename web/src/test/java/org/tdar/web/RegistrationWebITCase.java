package org.tdar.web;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.tdar.MultipleWebTdarConfigurationRunner;
import org.tdar.junit.RunWithTdarConfiguration;

/**
 * @author Adam Brin
 * 
 */
@RunWith(MultipleWebTdarConfigurationRunner.class)
public class RegistrationWebITCase extends AbstractWebTestCase {

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testRegisterNormalUser() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "user" + System.currentTimeMillis());
        testRegister(personmap, TERMS.TOS);
        assertCurrentUrlContains("dashboard");
        assertTextNotPresentIgnoreCase("new project");
        logout();
    }

    @Test
    public void testRegisterContributor() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributor" + System.currentTimeMillis());
        testRegister(personmap, TERMS.BOTH);
        assertCurrentUrlContains("dashboard");
        assertTextPresentIgnoreCase("Start a new Project");
        clickLinkWithText("UPLOAD");
        assertPageTitleEquals("add a new resource");
        logout();
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testRegisterContributorWithTOS() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributorrr" + System.currentTimeMillis());
        testRegister(personmap, TERMS.BOTH);

    }

}
