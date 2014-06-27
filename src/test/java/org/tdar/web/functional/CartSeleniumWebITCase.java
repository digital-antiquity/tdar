package org.tdar.web.functional;

import org.apache.struts2.interceptor.validation.SkipValidation;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.struts.data.UserRegistration;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringEndsWith.endsWith;

/**
 * Created by jimdevos on 6/25/14.
 */
public class CartSeleniumWebITCase extends AbstractSeleniumWebITCase {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Assert that user is logged out.
     */
    private void assertLoggedOut() {
        List<WebElement> selection = find(By.linkText("LOG IN")).toList();
        assertThat("login button exists", selection, is(not(empty())));
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
//        find("#confirmEmail").val(person.getEmail());
        find("#confirmEmail").val(reg.getConfirmEmail());
        find("#password").val(reg.getPassword());
        find("#confirmPassword").val(reg.getConfirmPassword());
        find("#username").val(person.getUsername());
        if(reg.isAcceptTermsOfUse() != find("#tou-id").isSelected()) {
            find("#tou-id").click();
        }
        if(reg.isRequestingContributorAccess() != find("#contributor-id").isSelected() ) {
            find("#contributor-id").click();
        }
    }

    @Test
    //ideal walk-through of purchase process for a visitor with no mistakes along the way.
    public void testVisitorPurchase() {
        //start at the cart page, and click one of the suggested packages
        gotoPage("/cart/new");
        assertLoggedOut();
        find("#divlarge button").click();
        String windowMain = getDriver().getWindowHandle();

        //now we are on the review form (w/ registration/login forms)
        //fill out required user registration fields and submit form
        assertThat(getCurrentUrl(), endsWith("cart/review"));
        UserRegistration reg = createUserRegistration("bob");
        fillOut(reg);
        submitForm();


        //now we are on the "choose billing account" page. just click through to next page
        waitForPageload();
        assertThat(getCurrentUrl(), endsWith("cart/choose-billing-account"));
        submitForm();

        //now we are on the process payment page.  click on the button to fire up a new window
        assertThat(getCurrentUrl(), endsWith("cart/process-payment-request"));
        find("#btnOpenPaymentWindow").click();


        //sanity check: assert that selenium didn't implicitly switch to popup window (this might be a osx-only thing)
        assertThat(windowMain, equalTo(getDriver().getWindowHandle()));

        switchToNextWindow();
        //popup window is active now.  assuming it is the fake payment processor,  all we need to do is submit the form to "pay" for the invoice
        waitFor("[type=submit]");
        submitForm("[type=submit]");

        //close the popup window
        find("#btnCloseWindow").click();
        assertThat("assert that popup window is no longer open", getDriver().getWindowHandles().size(), equalTo(1));

        //even though the popup window is gone, we still need to switch back to the main window
        getDriver().switchTo().window(windowMain);

        //if successful, we are sent to the dashboard
        waitFor("body.dashboard");
    }

    @Test
    //ideal walkthrough of purchase process for logged-out-user process with no mistakes
    public void testLoginPurchase() {
    }
}
