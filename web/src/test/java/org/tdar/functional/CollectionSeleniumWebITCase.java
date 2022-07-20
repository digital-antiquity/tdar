package org.tdar.functional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.functional.util.WebElementSelection;
import org.tdar.utils.TestConfiguration;

import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.openqa.selenium.By.*;

public class CollectionSeleniumWebITCase extends AbstractEditorSeleniumWebITCase {

    private static final String DIV_ACCESS_RIGHTS_ADD_ANOTHER_BUTTON = "#divAccessRightsAddAnotherButton";
    private static final String SHARE = "/collection/";
    private static final String LISTCOLLECTION = "/collection/";
    TestConfiguration config = TestConfiguration.getInstance();

    // safeguard to avoid confusion when passing boolean arguments
    private enum CollectionVisibility {
        VISIBLE,
        HIDDEN;
        boolean isHidden() {
            return this == HIDDEN;
        }
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
        String url = setupCollectionForTest(TITLE + " (permissions visible)", titles, CollectionVisibility.HIDDEN);
        logger.debug("URL: {}", url);
        logout();
        // make sure basic user cannot see restricted page
        UserRegistration registration = createUserRegistration("collectionPermissions");

        gotoPage("/register");
        TdarUser person = registration.getPerson();
        registration.setRequestingContributorAccess(true);
        registration.setContributorReason("beacuse");
        fillOutRegistration(registration);
        // fixme: it would be better to execute a javascript snippet that deducts the timecheck by 5000 millis;
        waitFor(5);
        submitForm();
        // login(person.getUsername(), registration.getPassword());
        setIgnorePageErrorChecks(true);
        gotoPage(url);
        // assert that the page has the resources
        assertPageNotViewable(titles);
        logout();
        // add basic user
        loginAdmin();
        gotoEdit(url);
        applyEditPageHacks();
        submitForm();
        waitFor(".toolbar-permissions").click();
        addUserWithRights(person.getProperName(), person.getUsername(), person.getId(), Permissions.VIEW_ALL);
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
        gotoEdit(url);
        applyEditPageHacks();
        waitFor(By.name("resourceCollection.hidden")).val(true);
        submitForm();
        logout();
        // check that anonymous user can see
        gotoPage(url);
        // shares are hidden, so nothing to see
        assertPageNotViewable(titles);
    }

    private void addUserWithRights(String name, String username, Long userId, Permissions permissions) {
        WebElementSelection find = waitFor(DIV_ACCESS_RIGHTS_ADD_ANOTHER_BUTTON);
        find.click();
        find.click();
        waitFor(By.name("proxies[2].displayName"));
        addAuthuser("proxies[2].displayName", "proxies[2].permission", name, username, "person-" + userId, permissions);
    }

    @SuppressWarnings("unused")
    @Test
    public void testCollectionRemoveElement() {
        String url = setupListForTest(TITLE + " (remove edit)", titles, CollectionVisibility.VISIBLE);
        gotoEdit(url);
        applyEditPageHacks();

        WebElementSelection select = waitFor(id("collection-selector"));
        url = getCurrentUrl();
        logger.debug("url:{}", url);

        String id = url.substring(0, url.lastIndexOf("/edit"));
        id = id.substring(id.lastIndexOf("/") + 1);
        logger.debug("id: {}, url: {}", id, url);

        // The filter toggle buttons are no longer on the Remove resource datatable.
        // find("#btnToggleFilters").click();
        // waitFor(ExpectedConditions.visibilityOf(select.first()));
        // select.val(id);
        // clearPageCache();

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
        String url = setupCollectionForTest(TITLE + " (collection retain)", titles, CollectionVisibility.HIDDEN);
        gotoEdit(url);
        submitForm();
        waitFor(".toolbar-permissions").click();
        waitForPageload();

        addUserWithRights("test user", config.getUsername(), config.getUserId(), Permissions.ADMINISTER_COLLECTION);
        submitForm();

        gotoPage("/project/" + _139 + "/edit");
        applyEditPageHacks();
        setFieldByName("status", Status.DELETED.name());
        submitForm();
        logout();
        login();
        gotoEdit(url);
        // gotoEdit(url, CollectionType.SHARED);
        // removeResourceFromCollection(TAG_FAUNAL_WORKSHOP);
        Assert.assertFalse(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
        submitForm();
        Assert.assertFalse(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
        logout();
        loginAdmin();
        gotoPage("/project/" + _139 + "/edit");
        setFieldByName("status", Status.ACTIVE.name());
        submitForm();
        reloadUntilFound(getCurrentUrl(), RUDD_CREEK_ARCHAEOLOGICAL_PROJECT, 10);
        Assert.assertTrue(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
        logout();
        gotoPage(url);
        // share are hidden, so should not see it
        Assert.assertFalse(getText().contains(RUDD_CREEK_ARCHAEOLOGICAL_PROJECT));
    }
    

    /**
     * Perform a "quick search" - that is, enter the specified search text into the quicksearch box and
     * submit the form by hitting the RETURN key.
     * @param searchTerm
     */
    protected void performQuickSearch(String searchTerm) {
        waitFor(".contextsearchbox")
                .click() // get focus on text box
                .val(searchTerm) // enter search term
                .sendKeys(Keys.RETURN); // press return key to submit form

    }


    /**
     * Perform a "contextual quick search", which is a quicksearch with the added stipulation that
     * this method asserts the presence of the 'search within this collection/project?' checkbox' and,
     * if present, checks the box.
     * @param searchTerm the string you want to search for
     */
    protected void performContextSearch(String searchTerm) {
        WebElementSelection searchBox = waitFor(".contextsearchbox").click();
        takeScreenshot("really-expecting-a-checkbox");
        try {
            waitFor("#cbctxid").val(true);
        } catch (TimeoutException tex) {
            fail("Expected to see a 'search within this project/collection' checkbox in the quicksearch form");
        }
        searchBox.val(searchTerm).sendKeys(Keys.RETURN);
    }


    @Test
    public void testCollectionInGeneralSearch() {
        List<String> titles = Arrays.asList(HARP_FAUNA_SPECIES_CODING_SHEET);
        String url = setupListForTest(TITLE + " (general search)", titles, CollectionVisibility.VISIBLE);
        logout();
        gotoPage(url);
        assertThat(getText(), containsString(TITLE));
        gotoPage("/");
        performQuickSearch( "Selenium");

        clearPageCache();
        logger.debug(getCurrentUrl());
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

        // List<String> urls = new ArrayList<>();
        // for (WebElement el : find(".media-body a")) {
        // urls.add(el.getAttribute("href"));
        // }
        // logger.debug("urls: {}", urls);
        // for (String link : urls) {
        // gotoPage(url);
        // gotoPage(link);
        // assertTitlesSeen(titles);
        // }
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
        Assert.assertEquals("should see every title on each page: " + titles + "", titles.size(), seen);
    }

    // fixme: probably better to just accept a transient collection object
    private String setupListForTest(String title_, List<String> titles, CollectionVisibility visibility) {
        gotoPage("/dashboard");
        gotoPage(LISTCOLLECTION + "add");

        applyEditPageHacks();

        Assert.assertTrue(find(By.tagName("h1")).getText().contains("New Collection"));
        setFieldByName("resourceCollection.name", title_);
        setFieldByName("resourceCollection.description", DESCRIPTION);

        waitFor(By.name("resourceCollection.hidden")).val(visibility.isHidden());
        setupResourceLookupSection();
        takeScreenshot("show-100-entries");
        addResourceToCollection(_139);
        for (String title : titles) {
            addResourceToCollection(title);
        }
        submitForm();
        assertPageViewable(titles);
        String url = getCurrentUrl();
        WebElementSelection button = waitFor(By.partialLinkText(RIGHTS));
        try {
            button.click();
        } catch (Throwable t) {
            logger.error("{}", t, t);
        }
        // waitForPageload();
        logger.debug(getPageCode());
        WebElementSelection find = waitFor(DIV_ACCESS_RIGHTS_ADD_ANOTHER_BUTTON);
        find.click();
        find.click();
        find.click();

        Permissions permission = Permissions.REMOVE_FROM_COLLECTION;
        addAuthuser("proxies[2].displayName", "proxies[2].permission", "editor user", config.getEditorUsername(),
                "person-" + config.getEditorUserId(), permission);
        addAuthuser("proxies[1].displayName", "proxies[1].permission",
                "michelle elliott", "Michelle Elliott", "person-121", permission);
        submitForm();
        return url;
    }

    /**
     * Assert that we properly render the "refine search" form when the search contains a collection filter.
     */
    @Test
    public void testRefineSearchWithCollectionFilter() {
        String url = setupCollectionForTest(TITLE + " (permissions visible)", titles, CollectionVisibility.HIDDEN);
        gotoPage("/search");
        waitFor("select.searchType").val("COLLECTION");
        selectAutocompleteValue(waitFor(id("groups_0__shares_0__name")).first(), TITLE, TITLE, null);
        submitForm();

        // hopefully we are on the results page - look for "refine this search" link and click it.
        waitFor(partialLinkText("Refine your search")).click();
        waitForPageload();
        assertThat(waitFor(id("searchGroups_groups_0__shares_0__name")).val(), containsString(TITLE));
        assertThat(waitFor(id("group0searchType_0_")).val(), is("COLLECTION"));
    }

    // fixme: probably better to just accept a transient collection object
    private String setupCollectionForTest(String title_, List<String> resourceTitles, CollectionVisibility visibility) {
        gotoPage("/dashboard");
        waitFor(By.linkText("CREATE")).click();
        waitForPageload();
        gotoPage(SHARE + "add");
        waitForPageload();
        applyEditPageHacks();
        String text = waitFor(By.tagName("h1")).getText();
        logger.debug(text);
        Assert.assertTrue(text.contains("New Collection"));
        setFieldByName("resourceCollection.name", title_);
        setFieldByName("resourceCollection.description", DESCRIPTION);

        waitFor(By.name("resourceCollection.hidden")).val(visibility.isHidden());
        setupResourceLookupSection();
        takeScreenshot("show-100-entries");
        addResourceToCollection(_139);
        for (String title : resourceTitles) {
            addResourceToCollection(title);
        }
        submitForm();
        assertPageViewable(resourceTitles);
        String url = getCurrentUrl();
        waitForPageload();
        assertEquals("count of edit buttons", 1, waitFor(By.partialLinkText(RIGHTS)).size());
        WebElementSelection button = waitFor(By.partialLinkText(RIGHTS));
        try {
            button.click();
        } catch (Throwable t) {
            logger.error("{}", t, t);
        }
        // waitForPageload();
        logger.debug(getPageCode());
        WebElementSelection find = waitFor(DIV_ACCESS_RIGHTS_ADD_ANOTHER_BUTTON);
        find.click();
        find.click();

        Permissions permission = Permissions.MODIFY_RECORD;
        addAuthuser("proxies[2].displayName", "proxies[2].permission", "editor user", config.getEditorUsername(),
                "person-" + config.getEditorUserId(), permission);
        addAuthuser("proxies[1].displayName", "proxies[1].permission",
                "michelle elliott", "Michelle Elliott", "person-121", permission);
        submitForm();
        return url;
    }

    private void gotoEdit(String url_) {
        String url = url_;
        url = url.substring(0, url.lastIndexOf("/"));
        logger.debug(getCurrentUrl());
        String id = StringUtils.substringAfterLast(url, "/");
        gotoPage(SHARE + id + "/edit");
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


    /**
     * Set the various options for the resource lookup control on the collection edit page (e.g.
     * the "Resources" section that includes the resource lookup data table.
     */
    public void setupResourceLookupSection() {
        // ensure that we return enough results such that the result we want doesn't spill into another page
        waitFor("[name=resource_datatable_length]").val("100");
    }

    // TODO -- Change this checkbox to buttons, since the new one renders those instead.
    public void addResourceToCollection(final String title) {

        // click the add resources tab.
        // String tabSelector = "#addResourceTab";
        // WebElement addResourceTab = findFirst(tabSelector);
        // addResourceTab.click();

        // wait until datatable loads new content
        String selector = "#resource_datatable tbody tr";
        WebElement origRow = findFirst(selector);

        waitFor(By.name("_tdar.query")).val(title);
        waitFor(ExpectedConditions.stalenessOf(origRow));

        // wait for new results to appear
        waitFor(ExpectedConditions.textToBePresentInElement(
                waitFor("#resource_datatable").first(), title));

        // get the checkbox of the matching row
        String rowId = findByIdWithText(title, selector);
        assertNotNull(rowId);
        // some searches may yield more than one result. just pick the first.
        // waitFor(stabilityOfElement(("#resource_datatable tbody tr")));
        // WebElementSelection checkboxes = find("#resource_datatable tbody tr")
        // .any(tr -> tr.getText().contains(title))
        // .find(".datatable-checkbox");
        // assertThat("expecting one or more matches", checkboxes.size(), is(greaterThan(0)));

        retryingFindClick(By.cssSelector("#" + rowId + " button"));

        // reset the search
        waitFor(By.name("_tdar.query")).val("");
        // waitFor(ExpectedConditions.stalenessOf(origRow));

    }

    private String findByIdWithText(final String title, String selector) {
        int attempts = 0;
        while (attempts < 2) {
            try {
                Iterator<WebElement> it = waitFor(selector).iterator();
                String rowId = null;
                while (it.hasNext()) {
                    WebElement tr = it.next();
                    if (tr.getText().contains(title)) {
                        rowId = tr.getAttribute("id");
                        return rowId;
                    }
                }
            } catch (Throwable e) {
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
        while (attempts < 2) {
            try {
                getDriver().findElement(by).click();
                result = true;
                break;
            } catch (Throwable e) {
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
        WebElementSelection rows = waitFor("#existing_resources_datatable tr");
        logger.debug("rows: {}", rows);
        String id = "";
        for (WebElement tr : rows) {
            logger.debug(tr.getText());
            if (tr.getText().contains(title)) {
                WebElement findElement = tr.findElement(By.tagName("button"));
                // Assert.assertTrue("checkbox should already be checked", findElement.isSelected());
                findElement.click();
                id = findElement.getAttribute("id");
                found = true;
                break;
            }
        }
        logger.debug(id);
        Assert.assertTrue("should have found at least one remove button with matching title: " + title, found);

        // The checkboxes aren't rendered for removals anymore.
        /*
         * if (StringUtils.isNotBlank(id)) {
         * if (rows.find(id(id)).isSelected()) {
         * rows.find(id(id)).click();
         * }
         * Assert.assertFalse(rows.find(id(id)).isSelected());
         * }
         */
    }

    @Override
    public boolean testRequiresLucene() {
        return true;
    }
}
