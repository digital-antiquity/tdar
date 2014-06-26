package org.tdar.web.functional;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.struts.data.UserRegistration;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringEndsWith.endsWith;

/**
 * Created by jimdevos on 6/25/14.
 */
public class CartSeleniumWebITCase extends AbstractSeleniumWebITCase {

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
     * @param userPrefix
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
     * @param reg
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

        //now we are on the review form (w/ registration/login forms)
        //fill out required user registration fields and submit form
        assertThat(getCurrentUrl(), endsWith("cart/review"));
        UserRegistration reg = createUserRegistration("bob");
        fillOut(reg);
        submitForm();


        //now we are on the "choose billing account" page. just click through to next page
        waitForPageload();
        assertThat(getCurrentUrl(), endsWith("cart/choose-billing-account"));
        //FIXME: look for type=submit in submitForm()
        submitForm("[type=submit]");

        //now we are on the process payment page.  click on the button to fire up a new window
        assertThat(getCurrentUrl(), endsWith("cart/process-payment-request"));
        find("#btnOpenPaymentWindow").click();


        //popup window is active now.  assuming it is the fake payment processor,  all we need to do is submit the form to "pay" for the invoice
        String popupTitle = getDriver().getTitle();
        assertThat(popupTitle, equalToIgnoringCase("fake-payment-form"));
        submitForm();

        //close the popup window
        find("#btnCloseWindow").click();

    }

}
