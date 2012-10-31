package org.tdar.struts.action;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.exception.StatusCode;
import org.tdar.struts.action.entity.PersonController;

public class PersonControllerITCase extends AbstractAdminControllerITCase {

    @Override
    protected TdarActionSupport getController() {
        return controller;
    }
    
    PersonController controller;

    @Before
    public void before() {
        controller = generateNewInitializedController(PersonController.class);
        
    }
    
    @Test
    @Rollback
    public void testSavingPerson() throws Exception {
        //simulate the edit
        controller.setId(1L);
        controller.prepare();
        controller.edit();
        Assert.assertEquals(controller.getPersistable().getFirstName().toLowerCase(), "allen");
        
        //simulate the save()
        controller = generateNewInitializedController(PersonController.class);
        controller.setId(1L);
        controller.prepare();
        Person p = controller.getPerson();
        p.setFirstName("bill");
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        
        //ensure stuff was changed.
        p = null;
        p = genericService.find(Person.class, 1L);
        Assert.assertEquals("bill", p.getFirstName().toLowerCase());
    }
    
    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testEditingPersonByNonAdmin() throws Exception {
        setIgnoreActionErrors(true);
        //simulate a basicuser trying to edit the adminuser record
        controller = generateNewController(PersonController.class);
        init(controller, getBasicUser());
        controller.setId(getAdminUserId());
        controller.prepare();
        
        //first off, ensure they can't even get to the edit page
        StatusCode code = null;
        try {
         controller.edit();
        } catch (TdarActionException e) {
            code = e.getResponseStatusCode();
        }
        Assert.assertEquals(StatusCode.FORBIDDEN,code);
        
        
        //so far so good - now ensure they can't spoof a save request
        controller = generateNewController(PersonController.class);
        init(controller, getBasicUser());
        controller.setId(getAdminUserId());
        controller.prepare();

        String oldLastName = getAdminUser().getLastName();
        final String newLastName = oldLastName.concat(" updated");
        controller.getPerson().setLastName(newLastName);
        controller.setServletRequest(getServletPostRequest());
        code = null;
        try {
         controller.save();
        } catch (TdarActionException e) {
            code = e.getResponseStatusCode();
        }
        Assert.assertEquals(StatusCode.FORBIDDEN,code);
        
        
        
        //did hibernate save the person record anyway?
        genericService.synchronize();
        runInNewTransaction(new TransactionCallback<Person>() {
            @Override
            public Person doInTransaction(TransactionStatus status) {
                Person admin = entityService.find(getAdminUserId());
                Assert.assertFalse("name shouldn't have been changed", admin.getLastName().equals(newLastName));
                return admin;
            }
            
        });
    }
    
    

}
