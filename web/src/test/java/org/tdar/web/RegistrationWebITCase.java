package org.tdar.web;

import static org.junit.Assert.assertFalse;

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
        personmap.remove("registration.contributorReason");
        testRegister(personmap, TERMS.TOS,true);
        assertCurrentUrlContains("dashboard");
        assertTextNotPresentIgnoreCase("new project");
        logout();
    }

    @Test
    public void testRegisterContributor() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributor" + System.currentTimeMillis());
        testRegister(personmap, TERMS.BOTH, true);
        assertCurrentUrlContains("dashboard");
        assertTextPresentIgnoreCase("Start a new Project");
        clickLinkWithText("UPLOAD");
        assertPageTitleEquals("add a new resource");
        logout();
    }

    @Test
    public void testRegisterContributorInvalid() {
        Map<String, String> personmap = new HashMap<String, String>();
        personmap.put("h.timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        testRegister(personmap, TERMS.TOS, false);
        assertFalse("not on dashboard", getCurrentUrlPath().contains("dashboard"));
    }

    @Test
    public void testRegisterTimeoutInvalid() {
        Map<String, String> personmap = new HashMap<String, String>();
        personmap.put("registration.contributorReason", "1");
        testRegister(personmap, TERMS.TOS, false);
        assertFalse("not on dashboard", getCurrentUrlPath().contains("dashboard"));
    }

    @Test
    @RunWithTdarConfiguration(runWith = { RunWithTdarConfiguration.TOS_CHANGE })
    public void testRegisterContributorWithTOS() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap, "contributorrr" + System.currentTimeMillis());
        testRegister(personmap, TERMS.BOTH, true);

    }


}
