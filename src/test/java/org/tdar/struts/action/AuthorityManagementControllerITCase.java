package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.utils.authoritymanagement.DedupeableType;

import com.opensymphony.xwork2.Preparable;

public class AuthorityManagementControllerITCase extends AbstractAdminControllerITCase{
    
    
    private AuthorityManagementController controller;
    
    @Autowired
    private GenericService genericService;
    
    @Before 
    public void setup() {
        controller = generateNewInitializedController(AuthorityManagementController.class);
    }
    
    @Override
    protected TdarActionSupport getController() {
        return controller;
    }
    
    @Test
    public void testIndex() {
        //not much to do here, other than check out the getters and make sure we are skipping validation
        assertTrue(CollectionUtils.isNotEmpty(controller.getDedupeableTypes()));
    }
    
    @Test
    public void testSelectAuthorityNoRequestVariables() {
        setIgnoreActionErrors(true);
        controller.validate();
        controller.selectAuthority();
        assertEquals("should be only one action error.  contents:"  + controller.getActionErrors(), 2, controller.getActionErrors().size());
        assertTrue("Expecting no dupes error ", controller.getActionErrors().contains(AuthorityManagementController.ERROR_NO_DUPLICATES));
        assertTrue("Expecting 'select a type' error", controller.getActionErrors().contains(AuthorityManagementController.ERROR_NO_ENTITY_TYPE));
    }
    
    @Test
    public void testSelectAuthorityNotEnoughDupes() {
        setIgnoreActionErrors(true);
        controller.setEntityType(DedupeableType.INSTITUTION);
        controller.getSelectedDupeIds().add(1L);
        controller.validate();
        controller.selectAuthority();
        assertTrue("expecting not enough dupes ", controller.getActionErrors().contains(AuthorityManagementController.ERROR_NOT_ENOUGH_DUPLICATES));
        
    }
    
    @Test
    @Rollback
    public void testMergeDuplicatesNoAuthority() {
        setIgnoreActionErrors(true);
        controller.setEntityType(DedupeableType.INSTITUTION);
        controller.getSelectedDupeIds().add(1L);
        controller.validate();
        controller.mergeDuplicates();
        assertTrue("no authority", controller.getActionErrors().contains(AuthorityManagementController.ERROR_NO_AUTHORITY_RECORD));
    }
    
    @Test
    @Rollback
    public void testMergeDuplicatesIntitutions() throws Exception {
        Institution authority = new Institution("Sports Authority");
        Institution dupe = new Institution("Department of Redundancy Department");
        makeSomeInstitutionReferences(authority, dupe);
        Long authorityId = authority.getId();
        Long dupeId = dupe.getId();
        controller.getSelectedDupeIds().addAll(Arrays.asList(authorityId, dupeId));
        controller.setAuthorityId(authorityId);
        controller.setEntityType(DedupeableType.INSTITUTION);
        controller.validate();
        dupe = genericService.find(Institution.class, dupeId);
        assertNotNull(dupe);
        controller.mergeDuplicates();
        
        //assert that the dupe is gone.
        dupe = genericService.find(Institution.class, dupeId);
        assertNull(dupe);

        //this syncronize is necessary (apparently) because we need to ensure that any pending deletes that may throw key violations fire
        //before this test terminates.   
        genericService.synchronize();   
    }
    
    //make two institutions (auth and dupe), and then associate it w/ all manner of tdar stuff
    private void makeSomeInstitutionReferences(Institution authority, Institution dupe) throws Exception {
        entityService.save(authority);
        genericService.save(dupe);
        
        
        //reference via document.resourceProviderInstitution
        Document document = createAndSaveNewInformationResource(Document.class);
        document.setResourceProviderInstitution(dupe);
        genericService.saveOrUpdate(document);
        
        //reference via resourceCreator.creator
        ResourceCreator rc = new ResourceCreator();
        rc.setCreator(dupe);
        rc.setResource(document);
        rc.setRole(ResourceCreatorRole.COLLABORATOR);
        rc.setSequenceNumber(1);
        genericService.saveOrUpdate(rc);
        
        //reference via person.institution
        Person person = new Person("john", "doe", "johndoe123@mailinator.com");
        person.setInstitution(dupe);
        genericService.saveOrUpdate(person);
        
        
    }
    
    
    //TODO: test for max dupe size.

    
}
