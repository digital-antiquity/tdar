package org.tdar.web.functional;


import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.search.query.SortOption;
import org.tdar.utils.TestConfiguration;

public class CollectionSeleniumWebITCase extends AbstractAdminSeleniumWebITCase {

    private static final String TITLE = "Selenium Collection Test";
    private static final String DESCRIPTION = "This is a simple description of a page....";
    private static final String HARP_FAUNA_SPECIES_CODING_SHEET = "HARP Fauna Species Coding Sheet";
    private static final String _2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT = "2008 New Philadelphia Archaeology Report";
    private static final String TAG_FAUNAL_WORKSHOP = "TAG Faunal Workshop";
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void setup() {
        reindexOnce();
    }

   
    @Test
    public void testCollectionPermissionsAndVisible() {
        TestConfiguration config = TestConfiguration.getInstance();
        List<String> titles = Arrays.asList(HARP_FAUNA_SPECIES_CODING_SHEET,TAG_FAUNAL_WORKSHOP,_2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT);
        String url = setupCollectionForTest(titles, false);
        logout();
        // make sure editor can see restricted page
        loginEditor();
        gotoPage(url);
        assertPageViewable(titles);
        logout();
        // make sure basic user cannot see restricted page
        login();
        gotoPage(url);
        assertPageNotViewable(titles);
        // add basic user
        loginAdmin();
        gotoEdit(url);
        WebElementSelection addAnother = find(By.id("accessRightsRecordsAddAnotherButton"));
        addAnother.click();
        addAuthuser("authorizedUsers[2].user.tempDisplayName", "authorizedUsers[2].generalPermission", "test user", config.getUsername(),"person-"+config.getUserId(),
                GeneralPermissions.VIEW_ALL);
        submitForm();
        logout();
        // make sure unauthenticated user cannot see
        gotoPage(url);
        assertPageNotViewable(titles);
        // make sure unauthenticated user can now see
        login();
        gotoPage(url);
        assertPageViewable(titles);
        logout();
        // change view permission
        loginAdmin();
        gotoEdit(url);
        setFieldByName("resourceCollection.visible", "true");
        submitForm();
        logout();
        // check that anonymous user can see
        gotoPage(url);
        assertPageViewable(titles);
    }

    @Test
    public void testCollectionRemoveElement() {
        TestConfiguration config = TestConfiguration.getInstance();
        List<String> titles = Arrays.asList(HARP_FAUNA_SPECIES_CODING_SHEET,
                TAG_FAUNAL_WORKSHOP,
                _2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT);
        String url = setupCollectionForTest(titles, false);
        gotoEdit(url);
        removeResourceFromCollection(TAG_FAUNAL_WORKSHOP);
        submitForm();
        Assert.assertFalse(getText().contains(TAG_FAUNAL_WORKSHOP));
        Assert.assertTrue(getText().contains(HARP_FAUNA_SPECIES_CODING_SHEET));
        Assert.assertTrue(getText().contains(_2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT));
        
    }

    @Test
    public void testCollectionOrientiationOptions() {
        TestConfiguration config = TestConfiguration.getInstance();
        List<String> titles = Arrays.asList(HARP_FAUNA_SPECIES_CODING_SHEET,
                TAG_FAUNAL_WORKSHOP,
                _2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT);
        String url = setupCollectionForTest(titles, false);
        for (DisplayOrientation orient : DisplayOrientation.values()) {
            gotoEdit(url);
            setFieldByName("resourceCollection.orientation", orient.name());
            submitForm();
            assertPageViewable(titles);
        }

        for (SortOption option : SortOption.getOptionsForResourceCollectionPage()) {
            gotoEdit(url);
            setFieldByName("resourceCollection.sortBy", option.name());
            submitForm();
            assertPageViewable(titles);
        }
}



    private String setupCollectionForTest(List<String> titles, Boolean visible) {
        gotoPage("/dashboard");
        find(By.linkText("UPLOAD")).click();
        waitForPageload();
        find(By.linkText("Collection")).click();
        waitForPageload();
        TestConfiguration config = TestConfiguration.getInstance();
        
        Assert.assertTrue(find(By.tagName("h1")).getText().contains("New Collection"));
        setFieldByName("resourceCollection.name", TITLE);
        setFieldByName("resourceCollection.description", DESCRIPTION);

        WebElementSelection addAnother = find(By.id("accessRightsRecordsAddAnotherButton"));
        addAnother.click();
        addAnother.click();
        setFieldByName("resourceCollection.visible", visible.toString().toLowerCase());
        addAuthuser("authorizedUsers[1].user.tempDisplayName", "authorizedUsers[1].generalPermission", "editor user", config.getEditorUsername(),"person-"+config.getEditorUserId(),
                GeneralPermissions.MODIFY_RECORD);
        addAuthuser("authorizedUsers[0].user.tempDisplayName", "authorizedUsers[0].generalPermission", "admin user", config.getAdminUsername(),"person-"+config.getAdminUserId(),
                GeneralPermissions.MODIFY_RECORD);
        addResourceToCollection("139");
        for (String title : titles) {
            addResourceToCollection(title);
        }
        submitForm();
        assertPageViewable(titles);
        String url = getCurrentUrl();
        return url;
    }


    private void gotoEdit(String url) {
        gotoPage(url);
        find(By.linkText("edit")).click();
        waitForPageload();
    }


    private void assertPageNotViewable(List<String> titles) {
        String text = getText();
        for (String title : titles) {
            Assert.assertFalse("view page contains title", text.contains(title));
        }
        Assert.assertTrue(text.contains(TITLE));
        Assert.assertFalse(text.contains(DESCRIPTION));
    }


    private void assertPageViewable(List<String> titles) {
        String text = getText();
        logger.trace(text);
        for (String title : titles) {
            Assert.assertTrue("view page contains title", text.contains(title));
        }
        Assert.assertTrue(text.contains(TITLE));
        Assert.assertTrue(text.contains(DESCRIPTION));
    }
    
    public void addResourceToCollection(String title) {
        setFieldByName("_tdar.query",title);
        waitFor(TestConfiguration.getInstance().getWaitInt());
        for (int i=0;i<20;i++) {
            String text = find("#resource_datatable").getText();
            logger.debug(text);
            if (text.contains(title)) {
                break;
            } else {
                waitFor(TestConfiguration.getInstance().getWaitInt());
            }
        }
        boolean found = false;
        for (WebElement tr : find("#resource_datatable tbody tr")) {
            if (tr.getText().contains(title)) {
                tr.findElement(By.cssSelector(".datatable-checkbox")).click();
                found = true;
                break;
            }
        }
        Assert.assertTrue("should have found at least one checkbox with matching title: " + title, found);
    }

    private void removeResourceFromCollection(String title) {
        boolean found = false;
        for (WebElement tr : find("#tblCollectionResources tbody tr")) {
            logger.debug(tr.getText());
            if (tr.getText().contains(title)) {
                tr.findElement(By.tagName("button")).click();
                found = true;
                break;
            }
        }
        Assert.assertTrue("should have found at least one remove button with matching title: " + title, found);
    }
}
