package org.tdar.web.functional;

import static org.tdar.core.bean.entity.permissions.GeneralPermissions.MODIFY_RECORD;
import static org.tdar.core.bean.entity.permissions.GeneralPermissions.VIEW_ALL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.utils.TestConfiguration;

/**
 * Created by jimdevos on 3/12/14.
 */
public class CollectionSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Before
    public void setup() {
        reindexOnce();
    }

   
    @Test
    public void testCollectionCreate() {
        gotoPage("/dashboard");
        find(By.linkText("UPLOAD")).click();
        waitForPageload();
        find(By.linkText("Collection")).click();
        waitForPageload();
        TestConfiguration config = TestConfiguration.getInstance();
        
        Assert.assertTrue(find(By.tagName("h1")).getText().contains("New Collection"));
        setFieldByName("resourceCollection.name", "Selenium Collection Test");
        setFieldByName("resourceCollection.description", "This is a simple description of a page.... ");

        setFieldByName("resourceCollection.visible", "false");
        WebElementSelection addAnother = find(By.id("accessRightsRecordsAddAnotherButton"));
        addAnother.click();
        addAnother.click();
        addAuthuser("authorizedUsers[0].user.tempDisplayName", "authorizedUsers[0].generalPermission", "test user", config.getUsername(),"person-"+config.getUserId(),
                MODIFY_RECORD);
        addAuthuser("authorizedUsers[1].user.tempDisplayName", "authorizedUsers[1].generalPermission", "Joshua Watts", "joshua.watts@asu.edu", "person-5349", VIEW_ALL);
        addResourceToCollection("HARP Fauna Species Coding Sheet");
        addResourceToCollection("TAG Faunal Workshop");
        addResourceToCollection("139");
        addResourceToCollection("2008 New Philadelphia Archaeology Report");
        submitForm();
    }
    
    public void addResourceToCollection(String title) {
        setFieldByName("_tdar.query",title);
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
        for (WebElement tr : find("#resource_datatable tr")) {
            if (tr.getText().contains(title)) {
                tr.findElement(By.cssSelector(".datatable-checkbox")).click();
                found = true;
            }
        }
        Assert.assertTrue("should have found at least one checkbox with matching title: " + title, found);
    }
}
