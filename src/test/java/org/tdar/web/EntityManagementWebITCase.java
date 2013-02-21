package org.tdar.web;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.tdar.TestConstants;

public class EntityManagementWebITCase extends AbstractAuthenticatedWebTestCase {

    private static final String ENTITY_VIEW = "/browse/creators/";
    private static final String ENTITY_PERSON_EDIT = "/entity/person/%s/edit";
    String personEditUrl;
    String personViewUrl;

    @Test
    public void testPersonSelfLoggedIn() {
        Long id = getUserId();
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
        gotoPage("/entity/person/myprofile");
        assertTextPresent(getUser().getProperName());
    }

    @Test
    public void testPersonLoggedOut() {
        Long id = entityService.findByEmail(TestConstants.USERNAME).getId();
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
