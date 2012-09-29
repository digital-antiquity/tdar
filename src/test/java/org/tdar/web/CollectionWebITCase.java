package org.tdar.web;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;


public class CollectionWebITCase  extends AbstractAdminAuthenticatedWebTestCase{

    @Autowired
    GenericService genericService;
    
    @Autowired
    EntityService entityService;
    
    //formats for form element names
    public static final String FMT_AUTHUSERS_ID = "authorizedUsers[%s].user.id";
    public static final String FMT_AUTHUSERS_LASTNAME = "authorizedUsers[%s].user.lastName";
    public static final String FMT_AUTHUSERS_FIRSTNAME = "authorizedUsers[%s].user.firstName";
    public static final String FMT_AUTHUSERS_EMAIL = "authorizedUsers[%s].user.email";
    public static final String FMT_AUTHUSERS_INSTITUTION = "authorizedUsers[%s].user.institution.name";
    public static final String FMT_AUTHUSERS_PERMISSION = "authorizedUsers[%s].generalPermission";
    
    
    
    @Test
    //crate a collection with some resources,  then edit it by adding some authorized users and removing a few resources
    public void testCreateThenEditCollection() {
        assertNotNull(genericService);
        gotoPage("/collection/add");
        String name = "my fancy collection";
        String desc = "description goes here";
        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);
        
        List<? extends Resource> someResources = getSomeResources();
        
        for(int i = 0 ; i < someResources.size(); i++) {
            Resource resource = someResources.get(i);
            //FIXME: we don't set id's in the form this way but setInput() doesn't understand 'resources.id' syntax.   fix it so that it can.
            String fieldName = "resources[" + i + "].id";
            String fieldValue = "" + resource.getId();
            logger.debug("setting  fieldName:{}\t value:{}", fieldName, fieldValue);
            createInput("hidden", "resources.id", fieldValue);
        }
        
        submitForm();
        assertTextPresent(name);
        assertTextPresent(desc);
        logger.trace(getHtmlPage().asText());
        String currentUrlPath = getCurrentUrlPath();
        for(Resource resource : someResources) {
            assertTextPresent(resource.getTitle());
        }
        
        //now go back to the edit page, add some users and remove some of the resources
        List<Person> registeredUsers = getSomePeople();
        clickLinkWithText("edit");
        int i = 1; //start at row '2' of the authorized user list, leaving the first entry blank.
        for(Person user : registeredUsers) {
            createInput("hidden", String.format(FMT_AUTHUSERS_ID, i), user.getId());
            createInput("text", String.format(FMT_AUTHUSERS_LASTNAME, i), user.getLastName());
            createInput("text", String.format(FMT_AUTHUSERS_FIRSTNAME, i), user.getFirstName());
            createInput("text", String.format(FMT_AUTHUSERS_EMAIL, i), user.getEmail());
            createInput("text", String.format(FMT_AUTHUSERS_INSTITUTION, i), user.getInstitutionName());
            createInput("text", String.format(FMT_AUTHUSERS_PERMISSION, i), GeneralPermissions.VIEW_ALL.toString());
            i++;
        }
        
        //remove the first 2 resources
        int removeCount = 2;
        Assert.assertTrue("this test needs at least 2 resources in the test DB", someResources.size() > removeCount);
        List<Resource> removedResources = new ArrayList<Resource>();
        for(i = 0; i <  removeCount; i++) {
            htmlPage.getElementById("hdnResourceId" + someResources.get(i).getId()).remove();
            removedResources.add(someResources.remove(i));
        }
        
        submitForm();
        
        //assert all the added names are on the view page
        for(Person user : registeredUsers) {
            assertTextPresent(user.toString()); //let's assume the view page uses tostring to format the user names.
        }
        
        //assert the removed resources are *not* present on the view page
        for(Resource resource : removedResources) {
            assertTextNotPresent(resource.getTitle());
        }
        
        logout();
        
        gotoPage(currentUrlPath);
        assertTextNotPresent("collection is not accessible");
    }
    
    //@Test
    public void  testRemoveResourceCollectionFromResourcePage() {
        
    }
    
    private List<? extends Resource> getSomeResources() {
        List<? extends Resource> alldocs = genericService.findAll(Document.class);
        List<? extends Resource> somedocs = alldocs.subList(0, Math.min(10, alldocs.size())); //get no more than 10 docs, pls
        return somedocs;
    }
    
    private List<Person> getSomePeople() {
        //let's only get authorized users
        List<Person> allRegisteredUsers = entityService.findAllRegisteredUsers();
        List<Person> someRegisteredUsers = allRegisteredUsers.subList(0, Math.min(10,  allRegisteredUsers.size()));
        return someRegisteredUsers;
    }
    
}
