package org.tdar.web;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class EntityManagementWebITCase extends AbstractAuthenticatedWebTestCase {

    private static final String ENTITY_VIEW = "/browse/creators/";
    private static final String ENTITY_PERSON_EDIT = "/entity/user/%s/edit";
    String personEditUrl;
    String personViewUrl;

    @Test
    public void testPersonSelfLoggedIn() {
        Long id = CONFIG.getUserId();
        String personViewUrl = ENTITY_VIEW + id;

        // assert the logged in user can go to the person view page
        gotoPage(personViewUrl);
        assertTextPresent("Last Login");

        // assert that the person logged in can see the person view page
        clickLinkWithText("edit");
        assertButtonPresentWithText("Save");

    }

    @Test
    public void testViewMyProfilePage() {
        gotoPage("/entity/user/myprofile");
        assertTextPresent("test user");
    }

    @Test
    public void testPersonLoggedOut() {
        Long id = CONFIG.getUserId();
        String personEditUrl = String.format(ENTITY_PERSON_EDIT, id);
        String personViewUrl = ENTITY_VIEW + id;

        logout();
        gotoPage(personViewUrl);
        // this text should only be present when you are logged in and you are a)an editor or b)looking at yourself
        assertTextNotPresent("Last Login");

        gotoPage(personEditUrl);
        String path = internalPage.getUrl().getPath().toLowerCase();
        ;
        assertFalse("We should not be on the edit page right now", path.endsWith(personEditUrl));
    }

}
