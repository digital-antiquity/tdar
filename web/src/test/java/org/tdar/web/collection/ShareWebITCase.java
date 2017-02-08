package org.tdar.web.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.StatusCode;
import org.tdar.web.AbstractAdminAuthenticatedWebTestCase;

public class ShareWebITCase extends AbstractAdminAuthenticatedWebTestCase {

    private void gotoEdit(String url_) {
        String url = url_;
        url = url.substring(0, url.lastIndexOf("/"));
        String id = org.apache.commons.lang3.StringUtils.substringAfterLast(url, "/");
        gotoPage("/share/"+ id +"/edit");
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
        createTestCollection(CollectionType.SHARED, name, desc, someResources);
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

        logger.debug("adding users: {}" , registeredUsers);
        int i = 1; // start at row '2' of the authorized user list, leaving the first entry blank.
        for (Person user : registeredUsers) {
            if (StringUtils.containsIgnoreCase(user.getProperName(), "user")) {
                continue;
            }
            createUserWithPermissions(i, user, GeneralPermissions.VIEW_ALL);
            i++;
        }

        // remove the first 2 resources
        int removeCount = 2;
        Assert.assertTrue("this test needs at least 2 resources in the test DB", someResources.size() > removeCount);
        List<Resource> removedResources = new ArrayList<Resource>();
        for (i = 0; i < removeCount; i++) {
            createInput("hidden", "toRemove[" + i + "]", someResources.get(i).getId());
            // htmlPage.getElementById("hrid" + someResources.get(i).getId()).remove();
            removedResources.add(someResources.remove(i));
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
        createTestCollection(CollectionType.SHARED, name, desc, someResources);
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
        logger.debug("{}",internalPage);
        assertTrue(getPageCode().contains("my fancy collection"));
        assertTrue(getPageCode().contains("Deleted"));

    }

    // assign a parent collection, then go back to dashboard
    @Test
    public void testCreateChildCollection() {
        // get a shared collection id - we don't have one in init-db so just rerun the previous test
        testCreateThenEditCollection();
        // previous test logged us out
        loginAdmin();
        Long parentId = 1575L;

        gotoPage("/share/add");
        String name = "testCreateChildCollection";
        String desc = "lame child colllection";

        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);
        setInput("parentId", "" + parentId);
        submitForm();
        assertTextPresentInPage(name);
        assertTextPresentInPage(desc);

        // now look for the collection on the dashboard (implicitly test encoding errors also)
        gotoPage("/manage");
        assertTextPresentInPage(name);
    }

    @Test
    public void testAssignNonUserToCollection() {
        // try to create a collection and assign it to a person that is not a registered user.
        gotoPage("/share/add");

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

        createInput("hidden", String.format(FMT_AUTHUSERS_ID, 1), ""); // leave the id blank
        createInput("text", String.format(FMT_AUTHUSERS_LASTNAME, 1), user.getLastName());
        createInput("text", String.format(FMT_AUTHUSERS_FIRSTNAME, 1), user.getFirstName());
        createInput("text", String.format(FMT_AUTHUSERS_EMAIL, 1), user.getEmail());
        createInput("text", String.format(FMT_AUTHUSERS_PERMISSION, 1), GeneralPermissions.VIEW_ALL.toString());

        submitFormWithoutErrorCheck();

        // assertTrue("we should  be on the INPUT page. current page: " + getCurrentUrlPath(), getCurrentUrlPath().contains("/collection/save.action"));

//        Person person = entityService.findByEmail(user.getEmail());
//        assertNull("person from form should not be persisted", person);
    }

    @Test
    public void testAssignNonUserToCollection2() {
        gotoPage("/share/add");
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

        List<Person> nonUsers = getSomePeople();
        int i = 1; // start at row '2' of the authorized user list, leaving the first entry blank.
        for (Person person : nonUsers) {
            if (StringUtils.containsIgnoreCase(person.getProperName(), "user")) {
                continue;
            }
            createInput("hidden", String.format(FMT_AUTHUSERS_ID, i), person.getId());
            createInput("text", String.format(FMT_AUTHUSERS_LASTNAME, i), person.getLastName());
            createInput("text", String.format(FMT_AUTHUSERS_FIRSTNAME, i), person.getFirstName());
            if (StringUtils.isNotBlank(person.getEmail())) {
                createInput("text", String.format(FMT_AUTHUSERS_EMAIL, i), person.getEmail());
            } else {
                createInput("text", String.format(FMT_AUTHUSERS_EMAIL, i), "");
            }
            if (StringUtils.isNotBlank(person.getInstitutionName())) {
                createInput("text", String.format(FMT_AUTHUSERS_INSTITUTION, i), person.getInstitutionName());

            } else {
                createInput("text", String.format(FMT_AUTHUSERS_INSTITUTION, i), "");
            }
            createInput("text", String.format(FMT_AUTHUSERS_PERMISSION, i), GeneralPermissions.VIEW_ALL.toString());
            i++;
        }

        submitFormWithoutErrorCheck();

        assertTrue(getPageText().contains("User does not exist"));
        assertTrue(getCurrentUrlPath().contains("/share/save"));

        assertTextPresent("my fancy collection");
    }
    

    @SuppressWarnings("unused")
    @Test
    public void testCollectionRightsRevoke() {
        //create test collection with basic user having adminGroup rights
        gotoPage("/share/add");
        String name = "my fancy collection";
        String desc = "description goes here";
        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);


        submitForm();
        String url = getCurrentUrlPath();
        Long id = extractTdarIdFromCurrentURL();
        gotoPage("/share/" + id + "/rights");
        setInput(String.format(FMT_AUTHUSERS_ID, 0), CONFIG.getUserId());
        setInput(String.format(FMT_AUTHUSERS_PERMISSION, 0), GeneralPermissions.ADMINISTER_SHARE.toString());
        submitForm();
        logout();
        
        // logout and login as that user, remove self
        login(CONFIG.getUsername(), CONFIG.getPassword());
        gotoPage(url);
        assertTextPresent("my fancy collection");
        clickLinkWithText("Rights");
        assertTextPresent("my fancy collection");
        removeElementsByName(String.format(FMT_AUTHUSERS_ID, 0));
        removeElementsByName(String.format(FMT_AUTHUSERS_PERMISSION, 0));
        String path = getCurrentUrlPath();
        submitForm();
        
        // assert that we can no longer edit that collection
        int status = gotoPageWithoutErrorCheck(path);
        assertEquals(StatusCode.FORBIDDEN.getHttpStatusCode(),status);
        logout();
        // logout / login, try again (assert not allowd)
        login(CONFIG.getUsername(), CONFIG.getPassword());
        status = gotoPageWithoutErrorCheck(path);
        assertEquals(StatusCode.FORBIDDEN.getHttpStatusCode(),status);
        
    }
}
