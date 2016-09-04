package org.tdar.functional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.functional.util.WebElementSelection;
import org.tdar.utils.TestConfiguration;

public class CollectionSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {

    private static final String _139 = "139";
    private static final String TITLE = "Selenium Collection Test";
    private static final String DESCRIPTION = "This is a simple description of a page....";
    private static final String RUDD_CREEK_ARCHAEOLOGICAL_PROJECT = "Rudd Creek Archaeological Project";
    private static final String HARP_FAUNA_SPECIES_CODING_SHEET = "HARP Fauna Species Coding Sheet";
    private static final String _2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT = "2008 New Philadelphia Archaeology Report";
    private static final String TAG_FAUNAL_WORKSHOP = "TAG Faunal Workshop";
    private Logger logger = LoggerFactory.getLogger(getClass());

    List<String> titles = Arrays.asList(HARP_FAUNA_SPECIES_CODING_SHEET, TAG_FAUNAL_WORKSHOP, _2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT);

    @Test
    public void testCollectionPermissionsAndVisible() {
        TestConfiguration config = TestConfiguration.getInstance();
        // setup a collection with 3 resources in it
        String url = setupCollectionForTest(TITLE + " (permissions visible)",titles, true, CollectionType.SHARED);
        logger.debug("URL: {}", url);
        logout();
        // make sure basic user cannot see restricted page
        login();
        setIgnorePageErrorChecks(true);
        gotoPage(url);
        // assert that the page has the resources
        assertPageNotViewable(titles);
        logout();
        // add basic user
        loginAdmin();
        gotoEdit(url, CollectionType.SHARED);
        applyEditPageHacks();
        addUserWithRights(config, url, GeneralPermissions.VIEW_ALL);
        submitForm();
        logout();
        // make sure unauthenticated user cannot see resources on the page
        setIgnorePageErrorChecks(true);
        gotoPage(url);
        assertPageNotViewable(titles);
        // make sure unauthenticated user can now see
        login();
        gotoPage(url);
        assertPageViewable(titles);
        logout();
        // change view permission
        loginAdmin();
        gotoEdit(url, CollectionType.SHARED);
        applyEditPageHacks();
        setFieldByName("resourceCollection.hidden", "false");
        submitForm();
        logout();
        // check that anonymous user can see
        gotoPage(url);
        assertPageViewable(titles);
    }

    private void addUserWithRights(TestConfiguration config, String url, GeneralPermissions permissions) {
        WebElementSelection addAnother = find(By.id("accessRightsRecordsAddAnotherButton"));
        addAnother.click();
        addAnother.click();
        addAuthuser("authorizedUsersFullNames[2]", "authorizedUsers[2].generalPermission", "test user", config.getUsername(),
                "person-" + config.getUserId(),
                permissions);
    }

    @SuppressWarnings("unused")
    @Test
    public void testCollectionRemoveElement() {
        TestConfiguration config = TestConfiguration.getInstance();
        String url = setupCollectionForTest(TITLE + " (remove edit)",titles, true, CollectionType.LIST);
        gotoEdit(url, CollectionType.LIST);
        applyEditPageHacks();

        WebElementSelection select = find(By.id("collection-selector"));
        url = getCurrentUrl();
        logger.debug("url:{}", url);

        String id = url.substring(0, url.lastIndexOf("/edit"));
        id = id.substring(id.lastIndexOf("/") + 1);
        logger.debug("id: {}, url: {}", id, url);
        find("#btnToggleFilters").click();
        waitFor(ExpectedConditions.visibilityOf(select.first()));
        select.val(id);
        clearPageCache();
        Assert.assertTrue(getText().contains(TAG_FAUNAL_WORKSHOP));
        clearPageCache();
        Assert.assertTrue(getText().contains(HARP_FAUNA_SPECIES_CODING_SHEET));
        Assert.assertTrue(getText().contains(_2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT));
        removeResourceFromCollection(TAG_FAUNAL_WORKSHOP);
        takeScreenshot();
        submitForm();
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Assert.assertFalse(getText().contains(TAG_FAUNAL_WORKSHOP));
        Assert.assertTrue(getText().contains(HARP_FAUNA_SPECIES_CODING_SHEET));
        Assert.assertTrue(getText().contains(_2008_NEW_PHILADELPHIA_ARCHAEOLOGY_REPORT));

    }

    @Test
    public void testCollectionRetain() {
        TestConfiguration config = TestConfiguration.getInstance();
        String url = setupCollectionForTest(TITLE + " (collection retain)",titles, false, CollectionType.SHARED);
        gotoEdit(url, CollectionType.SHARED);
        addUserWithRights(config, url, GeneralPermissions.ADMINISTER_GROUP);
        submitForm();

        gotoPage("/project/" + _139 + "/edit");
        applyEditPageHacks();
        setFieldByName("status", Status.DELETED.name());
        submitForm();
        logout();
        login();
        gotoEdit(url, CollectionType.SHARED);
        gotoEdit(url, CollectionType.SHARED);
        // removeResourceFromCollection(TAG_FAUNAL_WORKSHOP);
        Assert.assertFalse(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
        submitForm();
        Assert.assertFalse(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
        logout();
        loginAdmin();
        gotoPage("/project/" + _139 + "/edit");
        setFieldByName("status", Status.ACTIVE.name());
        submitForm();
        logout();
        gotoPage(url);
        Assert.assertTrue(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
    }

    @Test
    public void testCollectionInGeneralSearch() {
        List<String> titles = Arrays.asList(HARP_FAUNA_SPECIES_CODING_SHEET);
        String url = setupCollectionForTest(TITLE + " (general search)", titles, false, CollectionType.LIST);
        logout();
        gotoPage(url);
        Assert.assertTrue(getText().contains(TITLE));
        gotoPage("/");
        find(".searchbox").val("Selenium").sendKeys(Keys.RETURN);
        waitForPageload();
        clearPageCache();
        logger.debug(getText());
        Assert.assertTrue(getText().contains(TITLE));

    }

    @Test
    public void testCollectionOrientiationOptions() {
        // test display orientation
        List<String> titles = Arrays.asList("this is a test");
        String url = "/collection/1000";
        gotoPage(url);
        assertTitlesSeen(titles);
        gotoPage("/collection/1001");
        assertTitlesSeen(titles);
        gotoPage("/collection/1002");
        assertTitlesSeen(titles);
        gotoPage("/collection/1003");
        assertTitlesSeen(titles);

        // for (SortOption option : SortOption.getOptionsForResourceCollectionPage()) {
        // gotoEdit(url);
        // setFieldByName("resourceCollection.sortBy", option.name());
        // submitForm();
        // assertPageViewable(titles);
        // }

        List<String> urls = new ArrayList<>();
        for (WebElement el : find(".media-body a")) {
            urls.add(el.getAttribute("href"));
        }
        logger.debug("urls: {}", urls);
        for (String link : urls) {
            gotoPage(url);
            gotoPage(link);
            assertTitlesSeen(titles);
        }
    }

    private void assertTitlesSeen(List<String> titles) {
        String text = getText();
        int seen = 0;
        for (String title : titles) {
            if (text.contains(title)) {
                seen++;
            }
        }
        logger.debug("seen:{} total:{}", seen, titles.size());
        Assert.assertEquals("should see every title on each page", titles.size(), seen);
    }

    private String setupCollectionForTest(String title_, List<String> titles, Boolean visible, CollectionType type) {
        gotoPage("/dashboard");
        find(By.linkText("UPLOAD")).click();
        waitForPageload();
        if (type == CollectionType.LIST) {
            find(By.linkText("Collection")).click();
        } else {
            gotoPage("/share/add");
        }
        waitForPageload();
        applyEditPageHacks();
        TestConfiguration config = TestConfiguration.getInstance();

        Assert.assertTrue(find(By.tagName("h1")).getText().contains("New Collection"));
        setFieldByName("resourceCollection.name", title_);
        setFieldByName("resourceCollection.description", DESCRIPTION);

        WebElementSelection addAnother = find(By.id("accessRightsRecordsAddAnotherButton"));
        addAnother.click();
        addAnother.click();
        setFieldByName("resourceCollection.hidden", visible.toString().toLowerCase());
        addAuthuser("authorizedUsersFullNames[1]", "authorizedUsers[1].generalPermission", "editor user", config.getEditorUsername(), 
        		"person-"+ config.getEditorUserId(), GeneralPermissions.MODIFY_RECORD);
        addAuthuser("authorizedUsersFullNames[0]", "authorizedUsers[0].generalPermission",
        		"michelle elliott",  "Michelle Elliott", "person-121", GeneralPermissions.MODIFY_RECORD);
        addResourceToCollection(_139);
        for (String title : titles) {
            addResourceToCollection(title);
        }
        submitForm();
        assertPageViewable(titles);
        String url = getCurrentUrl();
        return url;
    }

    private void gotoEdit(String url_, CollectionType type) {
        String url = url_;
        url = url.substring(0, url.lastIndexOf("/"));
        logger.debug(getCurrentUrl());
        String id = StringUtils.substringAfterLast(url, "/");
        if (type == CollectionType.LIST) {
            gotoPage("/collection/"+ id +"/edit");
        }
        if (type == CollectionType.SHARED) {
            gotoPage("/share/"+ id +"/edit");
        }
        logger.debug(getCurrentUrl());
        // find(By.linkText(" edit")).click();
        waitForPageload();
    }

    private void assertPageNotViewable(List<String> titles) {
        String text = getText();
        for (String title : titles) {
            Assert.assertFalse("view page contains title", text.contains(title));
        }
        Assert.assertFalse(text.contains(DESCRIPTION));
    }

    private void assertPageViewable(List<String> titles2) {
        int seen = assertSeenTitles(titles2);
        if (seen > 0) {
            gotoPage(getCurrentUrl());
        }
        // just in case indexing is slow
        seen = assertSeenTitles(titles2);
        if (seen > 0) {
            fail("page should have all titles: " + titles2);
        }
        
    }

    private int assertSeenTitles(List<String> titles2) {
       int seen = titles2.size();
        String text = getText();
        logger.debug(text);
        for (String title : titles2) {
            if (text.contains(title)) {
                seen--;
                Assert.assertTrue("view page contains title: " + title, text.contains(title));
            }
        }
        Assert.assertTrue(text.contains(TITLE));
        Assert.assertTrue(text.contains(DESCRIPTION));
        return seen;
    }

    public void addResourceToCollection(final String title) {
        // wait until datatable loads new content
        String selector = "#resource_datatable tbody tr";
        WebElement origRow = findFirst(selector);

        find(By.name("_tdar.query")).val(title);
        waitFor(ExpectedConditions.stalenessOf(origRow));

        // wait for new results to appear
        waitFor(ExpectedConditions.textToBePresentInElement(
                find("#resource_datatable").first(), title));

        // get the checkbox of the matching row
        String rowId = findByIdWithText(title, selector);
        assertNotNull(rowId);
        //some searches may yield more than one result. just pick the first.
        
        retryingFindClick(By.cssSelector("#" + rowId + " .datatable-checkbox"));

        //reset the search
        find(By.name("_tdar.query")).val("");
//        waitFor(ExpectedConditions.stalenessOf(origRow));

    }

    private String findByIdWithText(final String title, String selector) {
        int attempts = 0;
        while(attempts < 2) {
            try {
                Iterator<WebElement> it = find(selector).iterator();
                String rowId = null;
                while (it.hasNext()) {
                    WebElement tr = it.next();
                    if (tr.getText().contains(title)) {
                        rowId = tr.getAttribute("id");
                        return rowId;
                    }
                }
            } catch(Throwable e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                }
            }
            attempts++;
        }
        return null;
    }

    public boolean retryingFindClick(By by) {
        boolean result = false;
        int attempts = 0;
        while(attempts < 2) {
            try {
                getDriver().findElement(by).click();
                result = true;
                break;
            } catch(Throwable e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                }
            }
            attempts++;
        }
        return result;
}
    private void removeResourceFromCollection(String title) {
        boolean found = false;
        WebElementSelection rows = find("#resource_datatable tr");
        logger.debug("rows: {}", rows);
        String id = "";
        for (WebElement tr : rows) {
            logger.debug(tr.getText());
            if (tr.getText().contains(title)) {
                WebElement findElement = tr.findElement(By.className("datatable-checkbox"));
                Assert.assertTrue("checkbox should already be checked", findElement.isSelected());
                findElement.click();
                id  = findElement.getAttribute("id");
                found = true;
                break;
            }
        }
        logger.debug(id);
        Assert.assertTrue("should have found at least one remove button with matching title: " + title, found);
        if (StringUtils.isNotBlank(id)) {
        	if (rows.find(By.id(id)).isSelected()) {
        		rows.find(By.id(id)).click();
        	}
        	Assert.assertFalse(rows.find(By.id(id)).isSelected());
        }
    }

    @Override
    public boolean testRequiresLucene() {
        return true;
    }
}
