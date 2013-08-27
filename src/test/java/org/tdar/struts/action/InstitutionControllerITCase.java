package org.tdar.struts.action;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.struts.action.entity.InstitutionController;

public class InstitutionControllerITCase extends AbstractAdminControllerITCase {
    
    InstitutionController controller;
    

    @Override
    protected TdarActionSupport getController() {
        // TODO Auto-generated method stub
        return controller;
    }
    
    @Before
    public void setup() {
        controller = generateNewInitializedController(InstitutionController.class);
    }
    
    
    
    @Test
    @Rollback
    public void testSavingInstitution() throws Exception {
        Institution inst = entityService.findAllInstitutions().iterator().next();
        String oldName = inst.getName();
        String newName = oldName.concat(" updated");
        controller.setId(inst.getId());
        controller.prepare();
        controller.edit();
        Assert.assertEquals(inst, controller.getInstitution());
        
        //simulate the save
        setup();
        controller.setId(inst.getId());
        controller.prepare();
        controller.setName(newName);
        controller.setServletRequest(getServletPostRequest());
        controller.validate();
        controller.save();
        
        //ensure stuff was changed
        setup();
        controller.setId(inst.getId());
        controller.prepare();
        Assert.assertEquals(newName, controller.getInstitution().getName());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    //non-curators should not be able to edit an institution
    public void testEditByNonAdmin() {
        Institution inst = genericService.findAll(Institution.class).iterator().next();
        setIgnoreActionErrors(true);
        final Long id = inst.getId();
        String oldName = inst.getName();
        final String newName = oldName.concat(" updated");
        controller = generateNewInitializedController(InstitutionController.class, getBasicUser());
        logger.info("{} -- {} ", controller.getAuthenticatedUser() , inst);
        controller.setId(id);
        controller.prepare();
        controller.getInstitution().setName(newName);
        try {
        	controller.edit();
        	Assert.fail("edit request from non-admin should have thrown an exception");
        }
        catch (TdarActionException expected) {
        	logger.debug("expected {}", expected);
        }
        setVerifyTransactionCallback(new TransactionCallback<Institution>() {
			@Override
			public Institution doInTransaction(TransactionStatus arg0) {
		        Institution institution = genericService.find(Institution.class, id);
		        Assert.assertFalse("institution shoudn't have been updated", newName.equals(institution.getName()));
		        return institution;
			}
        });
    }

}
