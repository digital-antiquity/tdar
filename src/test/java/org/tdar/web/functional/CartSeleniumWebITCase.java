package org.tdar.web.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringEndsWith.endsWith;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.struts.data.UserRegistration;

import com.opensymphony.xwork2.interceptor.annotations.After;

/**
 * Created by jimdevos on 6/25/14.
 */
public class CartSeleniumWebITCase extends AbstractSeleniumWebITCase {

    Logger logger = LoggerFactory.getLogger(getClass());

    // handle of window created at beginning of test
    String startWindow = null;

    @Before
    public void cartTestBefore() {
        startWindow = getDriver().getWindowHandle();
        force1024x768();
    }
    
    @After
    public void cleanup() {
        resetSize();
    }
    
    /**
     * Assert that user is logged out.
     */
    private void assertLoggedOut() {
        List<WebElement> selection = find(By.linkText("LOG IN")).toList();
        logger.debug(getCurrentUrl());
        assertThat("login button is missing", selection, is(not(empty())));
    }

    /**
     * think up values for use on a registration attempt that satisfy minimum required fields
     * @return
     */
    private TdarUser createUser(String prefix) {
        TdarUser user = new TdarUser();
        String uuid = prefix + UUID.randomUUID().toString();
        user.setEmail(uuid + "@mailinator.com");
        user.setFirstName("firstname");
        user.setLastName("lastname");
        user.setUsername(uuid);
        return user;
    }

    /**
     * create user-registration info with random username,email that satisfies minimum required fields
     * @param userPrefix prefix applied to username, email, firstname, and lastname
     * @return
     */
    private UserRegistration createUserRegistration(String userPrefix) {
        UserRegistration reg = new UserRegistration();
        TdarUser user = createUser(userPrefix);
        reg.setPerson(user);
        reg.setPassword("testPassword");
        reg.setConfirmPassword(reg.getPassword());
        reg.setConfirmEmail(user.getEmail());
        reg.setRequestingContributorAccess(true);
        reg.setAcceptTermsOfUse(true);
        return reg;
    }

    /**
     * fill out the user registration fields on the cart/review page.
     * @param reg user registration information
     */
    private void fillOut(UserRegistration reg) {
        //on firefox, autofoxus occurs after pageload(bugzilla: 717361). so we wait
        waitForPageload();
        TdarUser person = reg.getPerson();
        find("#firstName").val(person.getFirstName());
        find("#lastName").val(person.getLastName());
        find("#emailAddress").val(person.getEmail());

        assertThat(find("#confirmEmail").toList().size(), is(equalTo(1)));
        find("#confirmEmail").val(reg.getConfirmEmail());
        find("#password").val(reg.getPassword());
        find("#confirmPassword").val(reg.getConfirmPassword());
        find("#username").val(person.getUsername());
        if(reg.isAcceptTermsOfUse() != find("#tou-id").isSelected()) {
            find("#tou-id").click();
        }
//        if(reg.isRequestingContributorAccess() != find("#contributor-id").isSelected() ) {
//            find("#contributor-id").click();
//        }
    }

    @Test
    //ideal walk-through of purchase process for a visitor with no mistakes along the way.
    public void testVisitorPurchase() {
        //start at the cart page, and click one of the suggested packages
        gotoPage("/cart/new");
        assertLoggedOut();
        find("#divlarge button").click();

        //now we are on the review form (w/ registration/login forms)
        //fill out required user registration fields and submit form
        assertThat(getCurrentUrl(), endsWith("cart/review"));
        UserRegistration reg = createUserRegistration("bob");
        fillOut(reg);
        submitForm("#registrationForm .submitButton");


        //now we are on the "choose billing account" page. just click through to next page
        waitForPageload();
        assertThat(getCurrentUrl(), endsWith("cart/choose-billing-account"));
        int size = find(withLabel("account name")).size();
        assertThat(size, equalTo(1));
        //make sure that the form fields are present

        submitForm();

        //now we are on the process payment page.  click on the button to fire up a new window
        assertThat(getCurrentUrl(), endsWith("cart/process-payment-request"));
        find("#btnOpenPaymentWindow").click();


        //sanity check: assert that selenium didn't implicitly switch to popup window (this might be a osx-only thing)
        assertThat(startWindow, equalTo(getDriver().getWindowHandle()));

        switchToNextWindow();
        //popup window is active now.  assuming it is the fake payment processor,  all we need to do is submit the form to "pay" for the invoice
        waitFor("[type=submit]");
        submitForm();

        //close the popup window
        find("#btnCloseWindow").click();
        assertThat("nelnet window should be closed / only one window remains", getDriver().getWindowHandles().size(), equalTo(1));

        //even though the popup window is gone, we still need to switch back to the main window
        getDriver().switchTo().window(startWindow);

        //if successful, we are sent to the dashboard
        waitFor("body.dashboard");
    }

    @Test
    //ideal walkthrough of purchase process for logged-out-user process with no mistakes
    //todo: create By.buttonWithLabel (finds submit input with matching value -or- button with matching text node)
    //todo: create By.inputWithLabel  (finds element referred by for-attribute or child elements)
    public void testLoginPurchase() {
        // Starting page
        // go to the cart page and make sure we are logged out
        gotoPage("/cart/new");
        assertLoggedOut();
        // choose the large package
        find("#divlarge button").click();

        // review
        assertThat(getCurrentUrl(), endsWith("cart/review"));
        find("#loginUsername").val(CONFIG.getUsername());
        find("#loginPassword").val(CONFIG.getPassword());
        submitForm("#loginForm [type=submit]");

        // choose billing account
        assertThat(getCurrentUrl(), endsWith("cart/choose-billing-account"));
        // we aren't testing billing account customization,  so we just advance to the next step
        submitForm();

        // process payment
        assertThat(getCurrentUrl(), endsWith("cart/process-payment-request"));
        // open the popup window
        find("#btnOpenPaymentWindow").click();
        switchToNextWindow();
        waitFor("[type=submit]");
        submitForm();

        //close the popup window
        waitFor("#btnCloseWindow").click();
        assertThat("nelnet window should be closed / only one window remains", getDriver().getWindowHandles().size(), equalTo(1));

        // switch back to polling page
        getDriver().switchTo().window(startWindow);
        waitFor("body.dashboard");
    }

}
