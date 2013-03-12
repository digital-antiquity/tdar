/**
 * 
 */
package org.tdar.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Before;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Resource;

/**
 * @author Adam Brin
 *
 */
public abstract class AbstractAdminAuthenticatedWebTestCase extends AbstractAuthenticatedWebTestCase {
   
    @Before
    public void setUp() {
    	loginAdmin();
    }

    //fixme: this woudl be handy to have one level up but we need to be an admin... 
    protected void reindex() {
        gotoPage("/admin/searchindex/build");
        gotoPage("/admin/searchindex/checkstatus");
        logger.info(getPageCode());
        int count = 0;
        while (!getPageCode().contains("\"percentDone\" : 100")) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                fail("InterruptedException during reindex.  sorry.");
            }
            gotoPage("/admin/searchindex/checkstatus");
            logger.info(getPageCode());
            if (count == 1000) {
                fail("we went through 1000 iterations of waiting for the search index to build... assuming something is wrong");
            }
            count++;
        }
    }

    public void createTestCollection(String name, String desc, List<? extends Resource> someResources) {
        assertNotNull(genericService);
        gotoPage("/collection/add");
        setInput("resourceCollection.name", name);
        setInput("resourceCollection.description", desc);
        
        for(int i = 0 ; i < someResources.size(); i++) {
            Resource resource = someResources.get(i);
            //FIXME: we don't set id's in the form this way but setInput() doesn't understand 'resources.id' syntax.   fix it so that it can.
            String fieldName = "resources[" + i + "].id";
            String fieldValue = "" + resource.getId();
            logger.debug("setting  fieldName:{}\t value:{}", fieldName, fieldValue);
            createInput("hidden", "resources.id", fieldValue);
        }
        submitForm();
    }

    protected List<? extends Resource> getSomeResources() {
        List<? extends Resource> alldocs = genericService.findAll(Document.class);
        List<? extends Resource> somedocs = alldocs.subList(0, Math.min(10, alldocs.size())); //get no more than 10 docs, pls
        return somedocs;
    }
    
    protected List<Person> getSomeUsers() {
        //let's only get authorized users
        List<Person> allRegisteredUsers = entityService.findAllRegisteredUsers(null);
        List<Person> someRegisteredUsers = allRegisteredUsers.subList(0, Math.min(10,  allRegisteredUsers.size()));
        return someRegisteredUsers;
    }
    
    protected List<Person> getSomePeople() {
        List<Person> allNonUsers = entityService.findAll();
        allNonUsers.removeAll(entityService.findAllRegisteredUsers(null));
        List<Person> someNonUsers = allNonUsers.subList(0, Math.min(10,  allNonUsers.size()));
        return someNonUsers;
    }
    
}
