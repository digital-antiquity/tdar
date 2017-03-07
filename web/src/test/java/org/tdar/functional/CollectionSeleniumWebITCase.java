package org.tdar.functional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.functional.util.WebElementSelection;
import org.tdar.utils.TestConfiguration;

public class CollectionSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {

    private static final String SHARE = "/collection/";
    private static final String LISTCOLLECTION = "/listcollection/";
    TestConfiguration config = TestConfiguration.getInstance();

    //safeguard to avoid confusion when passing boolean arguments
    private enum CollectionVisibility {
        VISIBLE,
        HIDDEN;
        boolean isHidden(){return this == HIDDEN;}
    }

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
        // setup a collection with 3 resources in it
        String url = setupCollectionForTest(TITLE + " (permissions visible)",titles, CollectionVisibility.HIDDEN);
        logger.debug("URL: {}", url);
        logout();
        // make sure basic user cannot see restricted page
        UserRegistration registration = createUserRegistration("collectionPermissions");
        
        gotoPage("/register");
        TdarUser person = registration.getPerson();
        registration.setRequestingContributorAccess(true);
        registration.setContributorReason("beacuse");
        fillOutRegistration(registration);
        //fixme: it would be better to execute a javascript snippet that deducts the timecheck by 5000 millis;
        waitFor(5);
        submitForm();
//        login(person.getUsername(), registration.getPassword());
        setIgnorePageErrorChecks(true);
        gotoPage(url);
        // assert that the page has the resources
        assertPageNotViewable(titles);
        logout();
        // add basic user
        loginAdmin();
        gotoEdit(url, CollectionType.SHARED);
        applyEditPageHacks();
        submitForm();
        find(".toolbar-rights").click();
        addUserWithRights(person.getProperName(), person.getUsername(),  person.getId(), GeneralPermissions.VIEW_ALL);
        submitForm();

        logout();
        // make sure unauthenticated user cannot see resources on the page
        setIgnorePageErrorChecks(true);
        gotoPage(url);
        assertPageNotViewable(titles);
        // make sure unauthenticated user can now see
        login(person.getUsername(), registration.getPassword());
        gotoPage(url);
        assertPageViewable(titles);
        logout();
        // change view permission
        loginAdmin();
        gotoEdit(url, CollectionType.SHARED);
        applyEditPageHacks();
        find(By.name("resourceCollection.hidden")).val(true);
        submitForm();
        logout();
        // check that anonymous user can see
        gotoPage(url);
        // shares are hidden, so nothing to see
        assertPageNotViewable(titles);
    }

    private void addUserWithRights(String name, String username, Long userId, GeneralPermissions permissions) {
        WebElementSelection addAnother = find(By.id("accessRightsRecordsAddAnotherButton"));
        addAnother.click();
        addAnother.click();
        waitFor(By.name("authorizedUsersFullNames[2]"));
        addAuthuser("authorizedUsersFullNames[2]", "authorizedUsers[2].generalPermission", name, username, "person-" + userId, permissions);
    }

    @SuppressWarnings("unused")
    @Test
    public void testCollectionRemoveElement() {
        String url = setupListForTest(TITLE + " (remove edit)",titles, CollectionVisibility.VISIBLE);
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
        String url = setupCollectionForTest(TITLE + " (collection retain)",titles, CollectionVisibility.HIDDEN);
        gotoEdit(url, CollectionType.SHARED);
        submitForm();
        find(".toolbar-rights").click();
        waitForPageload();

        addUserWithRights("test user", config.getUsername(), config.getUserId(), GeneralPermissions.ADMINISTER_SHARE);
        submitForm();

        gotoPage("/project/" + _139 + "/edit");
        applyEditPageHacks();
        setFieldByName("status", Status.DELETED.name());
        submitForm();
        logout();
        login();
        gotoEdit(url, CollectionType.SHARED);
//        gotoEdit(url, CollectionType.SHARED);
        // removeResourceFromCollection(TAG_FAUNAL_WORKSHOP);
        Assert.assertFalse(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
        submitForm();
        Assert.assertFalse(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
        logout();
        loginAdmin();
        gotoPage("/project/" + _139 + "/edit");
        setFieldByName("status", Status.ACTIVE.name());
        submitForm();
        gotoPage(url);
        if (!getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT)) {
            gotoPage(url);
        }
        Assert.assertTrue(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
        logout();
        gotoPage(url);
        // share are hidden, so should not see it
        Assert.assertFalse(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
    }

    @Test
    public void testCollectionInGeneralSearch() {
        List<String> titles = Arrays.asList(HARP_FAUNA_SPECIES_CODING_SHEET);
        String url = setupListForTest(TITLE + " (general search)", titles, CollectionVisibility.VISIBLE);
        logout();
        gotoPage(url);
        assertThat(getText(), containsString(TITLE));
        gotoPage("/");
        find(".searchbox").val("Selenium").sendKeys(Keys.RETURN);
        waitForPageload();
        clearPageCache();
        assertThat(getText(), containsString(TITLE));
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

//        List<String> urls = new ArrayList<>();
//        for (WebElement el : find(".media-body a")) {
//            urls.add(el.getAttribute("href"));
//        }
//        logger.debug("urls: {}", urls);
//        for (String link : urls) {
//            gotoPage(url);
//            gotoPage(link);
//            assertTitlesSeen(titles);
//        }
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
        Assert.assertEquals("should see every title on each page: "+titles+"", titles.size(), seen);
    }

    //fixme:  probably better to just accept a transient collection object
    private String setupListForTest(String title_, List<String> titles, CollectionVisibility visibility) {
        gotoPage("/dashboard");
        gotoPage(LISTCOLLECTION + "add");

        applyEditPageHacks();

        Assert.assertTrue(find(By.tagName("h1")).getText().contains("New Collection"));
        setFieldByName("resourceCollection.name", title_);
        setFieldByName("resourceCollection.description", DESCRIPTION);

        find(By.name("resourceCollection.hidden")).val(visibility.isHidden());
        GeneralPermissions permission = GeneralPermissions.REMOVE_FROM_COLLECTION;
        addResourceToCollection(_139);
        for (String title : titles) {
            addResourceToCollection(title);
        }
        submitForm();
        assertPageViewable(titles);
        String url = getCurrentUrl();
        find(By.partialLinkText("RIGHTS")).click();
        WebElementSelection addAnother = find(By.id("accessRightsRecordsAddAnotherButton"));
        addAnother.click();
        addAnother.click();

        addAuthuser("authorizedUsersFullNames[1]", "authorizedUsers[1].generalPermission", "editor user", config.getEditorUsername(),
                "person-"+ config.getEditorUserId(), permission);
        addAuthuser("authorizedUsersFullNames[0]", "authorizedUsers[0].generalPermission",
                "michelle elliott",  "Michelle Elliott", "person-121", permission);
        submitForm();
        return url;
    }


    //fixme:  probably better to just accept a transient collection object
    private String setupCollectionForTest(String title_, List<String> resourceTitles, CollectionVisibility visibility) {
        gotoPage("/dashboard");
        find(By.linkText("UPLOAD")).click();
        waitForPageload();
        gotoPage(SHARE + "add");
        waitForPageload();
        applyEditPageHacks();
        String text = find(By.tagName("h1")).getText();
        logger.debug(text);
        Assert.assertTrue(text.contains("New Collection"));
        setFieldByName("resourceCollection.name", title_);
        setFieldByName("resourceCollection.description", DESCRIPTION);

        find(By.name("resourceCollection.hidden")).val(visibility.isHidden());
        GeneralPermissions permission = GeneralPermissions.MODIFY_RECORD;
        addResourceToCollection(_139);
        for (String title : resourceTitles) {
            addResourceToCollection(title);
        }
        submitForm();
        assertPageViewable(resourceTitles);
        String url = getCurrentUrl();
        find(By.partialLinkText("RIGHTS")).click();
        WebElementSelection addAnother = find(By.id("accessRightsRecordsAddAnotherButton"));
        addAnother.click();
        addAnother.click();

        addAuthuser("authorizedUsersFullNames[1]", "authorizedUsers[1].generalPermission", "editor user", config.getEditorUsername(), 
                "person-"+ config.getEditorUserId(), permission);
        addAuthuser("authorizedUsersFullNames[0]", "authorizedUsers[0].generalPermission",
                "michelle elliott",  "Michelle Elliott", "person-121", permission);
        submitForm();
        return url;
    }

    private void gotoEdit(String url_, CollectionType type) {
        String url = url_;
        url = url.substring(0, url.lastIndexOf("/"));
        logger.debug(getCurrentUrl());
        String id = StringUtils.substringAfterLast(url, "/");
        if (type == CollectionType.LIST) {
            gotoPage(LISTCOLLECTION+ id +"/edit");
        }
        if (type == CollectionType.SHARED) {
            gotoPage(SHARE+ id +"/edit");
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
//        waitFor(stabilityOfElement(("#resource_datatable tbody tr")));
//        WebElementSelection checkboxes = find("#resource_datatable tbody tr")
//                .any(tr -> tr.getText().contains(title))
//                .find(".datatable-checkbox");
//        assertThat("expecting one or more matches", checkboxes.size(), is(greaterThan(0)));

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
