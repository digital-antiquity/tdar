package org.tdar.web;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;

/**
 * @author Adam Brin
 * 
 */
public class RegistrationWebITCase extends AbstractWebTestCase {

    @Autowired
    private AuthenticationAndAuthorizationService authService;

    public void testLogin(Map<String, String> values, boolean deleteFirst) {

        if (deleteFirst) {
            Person p = new Person();
            p.setUsername(values.get("person.username"));
            authService.getAuthenticationProvider().deleteUser(p);
        }
        gotoPage("/");
        clickLinkOnPage("Register");
        for (String key : values.keySet()) {
            setInput(key, values.get(key));
        }
        setInput("timeCheck", Long.toString(System.currentTimeMillis() - 10000));
        submitForm("Save");

    }

    @Test
    public void testRegisterNormalUser() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap,"user");
        personmap.put("requestingContributorAccess", "false");
        testLogin(personmap, true);
        assertTextNotPresent("Create a new project");
        gotoPage("/logout");
    }

    @Test
    public void testRegisterContributor() {
        Map<String, String> personmap = new HashMap<String, String>();
        setupBasicUser(personmap,"contributor");
        testLogin(personmap, true);
        assertTextPresent("Create a new project");
        clickLinkWithText("Upload");
        gotoPage("/account/view?personId=1");
        assertPageTitleEquals("Page not found");
        gotoPage("/logout");
    }

    @Test
    public void testInvalidView() {
        gotoPage("/account/view?personId=1");
        assertCurrentUrlContains("/account/new");
    }

    private void setupBasicUser(Map<String, String> personmap,String prefix) {
        personmap.put("person.firstName", prefix + "firstName");
        personmap.put("person.lastName", prefix + "lastName");
        personmap.put("person.email", prefix + "aaaaa@bbbbb.com");
        personmap.put("confirmEmail", prefix + "aaaaa@bbbbb.com");
        personmap.put("person.username", prefix + "aaaaa@bbbbb.com");
        personmap.put("password", "secret");
        personmap.put("confirmPassword", "secret");
        personmap.put("institutionName", "institution");
        personmap.put("person.phone", "1234567890");
        personmap.put("person.contributorReason", "there is a reason");
//        personmap.put("person.rpaNumber", "1234567890");
        personmap.put("requestingContributorAccess", "true");
    }

}
