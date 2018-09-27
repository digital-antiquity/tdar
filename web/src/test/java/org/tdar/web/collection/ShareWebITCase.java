package org.tdar.web.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.Permissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.StatusCode;
import org.tdar.utils.TestConfiguration;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class ShareWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private void gotoEdit(String url_) {
        String url = url_;
        url = url.substring(0, url.lastIndexOf("/"));
        String id = org.apache.commons.lang3.StringUtils.substringAfterLast(url, "/");
        gotoPage("/collection/" + id + "/edit");
    }

    private static final String RETAIN_COLLECTION = "My Test Retain Collection";
    private static final TestConfiguration TEST = TestConfiguration.getInstance();
    private static final String RETAIN_COLLECTION_2 = "My test dataset revoke collection";
    private Long idFromCurrentURL;

    @Test
    public void testCreateEditDocumentBlankCollection() {
        gotoPage("/image/add");
        setInput("image.title", "test title");
        setInput("status", Status.DRAFT.name());
        setInput("image.date", "2000");
        setInput("image.description", "test description of a image with edit rights by user");
        setInput("shares[0].name", RETAIN_COLLECTION);
        submitForm();
        assertTextPresentInPage(RETAIN_COLLECTION);
        clickLinkWithText(CollectionWebITCase.PERMISSIONS);
        setInput("proxies[0].id", TEST.getUserId());
        setInput("proxies[0].permission", Permissions.MODIFY_RECORD.name());
        setInput("proxies[0].displayName", "test user");
        logger.debug(getPageText());
        submitForm();

        String pageUrl = getCurrentUrlPath();
        clickLinkWithText(RETAIN_COLLECTION);
        clickLinkWithText("edit");
        setInput("resourceCollection.hidden", "true");
        submitForm();
        assertTextPresentInPage("true");
        logout();
        login(TEST.getUsername(), TEST.getPassword());
        gotoPage(pageUrl);
        // difference between stable and default, where we list the shares at the bottom in the rights section
        String beforeAccessPermissions = StringUtils.substringBefore(getPageText(), "Access Permissions");
        assertFalse(beforeAccessPermissions.contains(RETAIN_COLLECTION));
        clickLinkWithText("edit");

        // the collection should be blank, but we should have a warning about the collection not being visible
        assertEquals("", getInput("shares[0].name").getAttribute("value"));
        // assertTextPresentInCode("effectiveCollectionsVisible");
        submitForm();
        beforeAccessPermissions = StringUtils.substringBefore(getPageText(), "Access Permissions");
        assertFalse(beforeAccessPermissions.contains(RETAIN_COLLECTION));
        assertTextNotPresent("the resource you requested is");
        logout();
        loginAdmin();
        gotoPage(pageUrl);
        assertTextPresentInPage(RETAIN_COLLECTION);
    }

    @Test
    public void testCreateEditDocumentRetainShared() {
        gotoPage("/dataset/add");
        setInput("dataset.title", "test title");
        setInput("status", Status.DRAFT.name());
        setInput("dataset.date", "2000");
        setInput("dataset.description", "test description of a dataset with edit rights by user");
        // setInput("authorizedUsers[0].user.id", TEST.getUserId());
        // setInput("authorizedUsers[0].generalPermission", GeneralPermissions.MODIFY_RECORD.name());
        // setInput("authorizedUsersFullNames[0]", "test user");
        setInput("shares[0].name", RETAIN_COLLECTION_2);
        submitForm();
        assertTextPresentInPage(RETAIN_COLLECTION_2);
        String pageUrl = getCurrentUrlPath();
        clickLinkWithText(RETAIN_COLLECTION_2);
        clickLinkWithText("edit");
        setInput("resourceCollection.hidden", "true");
        submitForm();
        clickLinkWithText(CollectionWebITCase.PERMISSIONS);
        setInput(String.format(FMT_AUTHUSERS_ID, 0), TEST.getUserId()); // leave the id blank
        setInput(String.format(FMT_AUTHUSERS_PERMISSION, 0), Permissions.MODIFY_RECORD.name());
        submitForm();
        assertTextPresentInPage("true");
        logout();
        login(TEST.getUsername(), TEST.getPassword());
        gotoPage(pageUrl);
        assertTextPresentInCode("test user:MODIFY_RECORD");
        assertTextPresent(RETAIN_COLLECTION_2);
        clickLinkWithText("edit");

        // the collection should be blank, but we should have a warning about the collection not being visible
        assertEquals("", getInput("shares[0].name").getAttribute("value"));
        // assertTextPresentInCode("effectiveCollectionsVisible");

        submitForm();
        assertTextPresent(RETAIN_COLLECTION_2);
        assertTextNotPresent("the resource you requested is");
        logout();
        loginAdmin();
        gotoPage(pageUrl);
        assertTextPresentInPage(RETAIN_COLLECTION_2);
    }

    @Test
    // crate a collection with some resources, then edit it by adding some authorized users and removing a few resources
    public void testCreateThenEditCollection() {
        String name = "my fancy collection: " + System.currentTimeMillis();
        String desc = "description goes here: " + System.currentTimeMillis();
        List<Document> someResources = new ArrayList<>();
        someResources.add(createDocument());
        someResources.add(createDocument());
        someResources.add(createDocument());
        createTestCollection(CollectionResourceSection.MANAGED, name, desc, someResources);
        assertTextPresent(name);
        assertTextPresent(desc);
        logger.trace(getHtmlPage().asText());
        String currentUrlPath = getCurrentUrlPath();
        gotoPage(currentUrlPath);
        for (Resource resource : someResources) {
            if ((resource.getStatus() == Status.ACTIVE) || (resource.getStatus() == Status.DRAFT)) {
                assertTextPresent(resource.getTitle());
            }
        }

        // now go back to the edit page, add some users and remove some of the resources
        List<TdarUser> registeredUsers = getSomeUsers();

        gotoEdit(currentUrlPath);

        // remove the first 2 resources
        int removeCount = 2;
        Assert.assertTrue("this test needs at least 2 resources in the test DB", someResources.size() > removeCount);
        List<Resource> removedResources = new ArrayList<Resource>();
        for (int i = 0; i < removeCount; i++) {
            createInput("hidden", "toRemoveManaged[" + i + "]", someResources.get(i).getId());
            // htmlPage.getElementById("hrid" + someResources.get(i).getId()).remove();
            removedResources.add(someResources.remove(i));
        }

        submitForm();
        clickLinkOnPage(CollectionWebITCase.PERMISSIONS);
        logger.debug("adding users: {}", registeredUsers);
        int i = 1; // start at row '2' of the authorized user list, leaving the first entry blank.
        for (Person user : registeredUsers) {
            if (StringUtils.containsIgnoreCase(user.getProperName(), "user")) {
                continue;
            }
            createUserWithPermissions(i, user, Permissions.VIEW_ALL);
            i++;
        }
        submitForm();

        // we should be on the view page now
        logger.trace("now on page {}", getCurrentUrlPath());
        logger.trace("page contents: {}", getPageText());
        // assert all the added names are on the view page
        for (Person user : registeredUsers) {
            if (StringUtils.containsIgnoreCase(user.getProperName(), "user")) {
                continue;
            }
            assertTextPresent(user.getProperName()); // let's assume the view page uses tostring to format the user names.
        }

        // we're having inconsistent failures on this assertion.
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // assert the removed resources are *not* present on the view page
        for (Resource resource : removedResources) {
            assertTextNotPresent(resource.getTitle());
        }
        idFromCurrentURL = extractTdarIdFromCurrentURL();
        logout();

        gotoPage(currentUrlPath);
        assertTextNotPresent("collection is not accessible");
    }

    private Document createDocument() {
        String title1 = "ctec" + System.currentTimeMillis();
        Long id1 = createResourceFromType(ResourceType.DOCUMENT, title1);
        Document doc1 = new Document();
        doc1.setId(id1);
        doc1.setTitle(title1);
        return doc1;
    }

    @Test
    // crate a collection with some resources, then edit it by adding some authorized users and removing a few resources
    public void testDeleteCollection() {
        String name = "my fancy collection: " + System.currentTimeMillis();
        String desc = "description goes here: " + System.currentTimeMillis();
        List<? extends Resource> someResources = getSomeResources();
        createTestCollection(CollectionResourceSection.MANAGED, name, desc, someResources);
        assertTextPresent(name);
        assertTextPresent(desc);
        logger.trace(getHtmlPage().asText());
        String currentUrlPath = getCurrentUrlPath();
        logger.debug(currentUrlPath);
        for (Resource resource : someResources) {
            assertTextPresent(resource.getTitle());
        }
        logout();
        gotoPage(currentUrlPath);
        gotoPage(currentUrlPath);
        loginAdmin();
        gotoPage(currentUrlPath);
        clickLinkOnPage("delete");
        submitForm("delete");
        logger.debug("currentPage: " + currentUrlPath);
        gotoPageWithoutErrorCheck(currentUrlPath);
        logger.debug("{}", internalPage);
        assertTrue(getPageCode().contains("my fancy collection"));
        assertTrue(getPageCode().toLowerCase().contains("deleted"));

    }

    // assign a parent collection, then go back to dashboard
    @Test
    public void testCreateChildCollection() {
        // get a shared collection id - we don't have one in init-db so just rerun the previous test
        testCreateThenEditCollection();
        Long id2 = idFromCurrentURL;
        // previous test logged us out
        loginAdmin();
        Long parentId = 1575L;

        gotoPage("/collection/add");
        String name = "testCreateChildCollection";
        String desc = "lame child colllection";

        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);
        setInput("parentId", "" + parentId);
        setInput("alternateParentId", "" + id2);
        submitForm();
        assertTextPresentInPage(name);
        assertTextPresentInPage(desc);
        assertNoErrorTextPresent();
        assertFalse(getCurrentUrlPath().contains("save"));
        assertFalse(getCurrentUrlPath().contains("edit"));
        assertFalse(getCurrentUrlPath().contains("add"));
    }

    @Test
    public void testAssignNonUserToCollection() {
        // try to create a collection and assign it to a person that is not a registered user.
        gotoPage("/collection/add");

        // first lets start populating the person fields with a person that does not yet exist. tDAR should not create the person record on the fly, and
        // should not assign to the collection.
        String name = "my fancy collection";
        String desc = "description goes here";
        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);

        List<? extends Resource> someResources = getSomeResources();
        for (int i = 0; i < someResources.size(); i++) {
            Resource resource = someResources.get(i);
            // FIXME: we don't set id's in the form this way but setInput() doesn't understand 'resources.id' syntax. fix it so that it can.
            String fieldName = "resources[" + i + "].id";
            String fieldValue = "" + resource.getId();
            logger.debug("setting  fieldName:{}\t value:{}", fieldName, fieldValue);
            createInput("hidden", "resources.id", fieldValue);
        }

        Person user = new Person("joe", "blow", "testAssignNonUserToCollection@tdar.net");
        submitForm();
        clickLinkWithText(CollectionWebITCase.PERMISSIONS);
        createUserFields(1, user, Permissions.VIEW_ALL, null);
        submitFormWithoutErrorCheck();

        // assertTrue("we should be on the INPUT page. current page: " + getCurrentUrlPath(), getCurrentUrlPath().contains("/collection/save.action"));

        // Person person = entityService.findByEmail(user.getEmail());
        // assertNull("person from form should not be persisted", person);
    }

    @Test
    public void testAssignNonUserToCollection2() {
        gotoPage("/collection/add");
        String name = "my fancy collection";
        String desc = "description goes here";
        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);

        List<? extends Resource> someResources = getSomeResources();

        for (int i = 0; i < someResources.size(); i++) {
            Resource resource = someResources.get(i);
            // FIXME: we don't set id's in the form this way but setInput() doesn't understand 'resources.id' syntax. fix it so that it can.
            String fieldName = "resources[" + i + "].id";
            String fieldValue = "" + resource.getId();
            logger.debug("setting  fieldName:{}\t value:{}", fieldName, fieldValue);
            createInput("hidden", "resources.id", fieldValue);
        }
        submitForm();
        clickLinkWithText(CollectionWebITCase.PERMISSIONS);
        List<Person> nonUsers = getSomePeople();
        int i = 1; // start at row '2' of the authorized user list, leaving the first entry blank.
        for (Person person : nonUsers) {
            if (StringUtils.containsIgnoreCase(person.getProperName(), "user")) {
                continue;
            }
            createUserFields(i, person, Permissions.VIEW_ALL, person.getId());
            i++;
        }

        submitFormWithoutErrorCheck();
        logger.debug(getPageText());
        assertTrue(getPageText().contains("User does not exist"));
        assertTrue(getCurrentUrlPath().contains("rights-save"));

        assertTextPresent("my fancy collection");
    }

    @SuppressWarnings("unused")
    @Test
    public void testCollectionRightsRevoke() {
        // create test collection with basic user having adminGroup rights
        gotoPage("/collection/add");
        String name = "my fancy collection";
        String desc = "description goes here";
        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);

        submitForm();
        String url = getCurrentUrlPath();
        Long id = extractTdarIdFromCurrentURL();
        gotoPage("/collection/" + id + "/rights");
        setInput(String.format(FMT_AUTHUSERS_ID, 0), CONFIG.getUserId());
        setInput(String.format(FMT_AUTHUSERS_PERMISSION, 0), Permissions.ADMINISTER_COLLECTION.toString());
        submitForm();
        logout();

        // logout and login as that user, remove self
        login(CONFIG.getUsername(), CONFIG.getPassword());
        gotoPage(url);
        assertTextPresent("my fancy collection");
        clickLinkWithText(CollectionWebITCase.PERMISSIONS);
        assertTextPresent("my fancy collection");
        removeElementsByName(String.format(FMT_AUTHUSERS_ID, 0));
        removeElementsByName(String.format(FMT_AUTHUSERS_PERMISSION, 0));
        String path = getCurrentUrlPath();
        submitForm();

        // assert that we can no longer edit that collection
        int status = gotoPageWithoutErrorCheck(path);
        assertEquals(StatusCode.FORBIDDEN.getHttpStatusCode(), status);
        logout();
        // logout / login, try again (assert not allowd)
        login(CONFIG.getUsername(), CONFIG.getPassword());
        status = gotoPageWithoutErrorCheck(path);
        assertEquals(StatusCode.FORBIDDEN.getHttpStatusCode(), status);

    }
}
