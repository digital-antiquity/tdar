package org.tdar.web.entity;

import org.junit.Test;
import org.tdar.web.AbstractAuthenticatedWebTestCase;

public class AddressWebITCase extends AbstractAuthenticatedWebTestCase {

    private static final String ENTITY_VIEW = "/browse/creators/";
    String personEditUrl;
    String personViewUrl;

    @Test
    public void testAddAddress() {
        Long id = CONFIG.getUserId();
        String personViewUrl = ENTITY_VIEW + id;

        // assert the logged in user can go to the person view page
        gotoPage(personViewUrl);
        assertTextPresent("Last Login");

        // assert that the person logged in can see the person view page
        clickLinkWithText("edit");
        clickLinkWithText("Add Address");
        setInput("address.street1", "street1");
        setInput("address.street2", "street2");
        setInput("address.city", "Tempe");
        setInput("address.state", "Arizona");
        setInput("address.postal", "85287");
        setInput("address.country", "usa");
        assertButtonPresentWithText("Save");
        submitForm();
        assertTextPresentInPage("street1");
        clickLinkWithText("edit");
        setInput("address.street2", "street3");
        submitForm();
        assertTextPresentInPage("street3");
    }

}
