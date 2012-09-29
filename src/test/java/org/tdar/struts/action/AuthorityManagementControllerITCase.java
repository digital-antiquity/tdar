package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.DedupeableType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.external.EmailService;

public class AuthorityManagementControllerITCase extends AbstractAdminControllerITCase{
    
    
    private AuthorityManagementController controller;
    
    @Autowired
    private GenericService genericService;
    
    @Autowired 
    EmailService emailService;
    
    
    @Before 
    public void setup() {
        controller = generateNewInitializedController(AuthorityManagementController.class);
        
        //replace mailsender with stub that does nothing
        emailService.setMailSender(new MailSender() {

			@Override
			public void send(SimpleMailMessage simpleMessage)
					throws MailException {
			}

			@Override
			public void send(SimpleMailMessage[] simpleMessages)
					throws MailException {
			}});
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
    
    
    @Test
    @Rollback
    public void testProtectedPersonRecordsCannotBeDeduped() {
        setIgnoreActionErrors(true);
        Person person1 = createAndSaveNewPerson("person1@mailinator.com", "person1");
        Person protectedRecord1 = createAndSaveNewPerson("protectedRecord1@mailinator.com", "protectedRecord1");
        Person protectedRecord2 = createAndSaveNewPerson("protectedRrecord2@mailinator.com", "protectedRecord2");
        protectedRecord1.setRegistered(true);
        protectedRecord2.setRegistered(true);
        genericService.saveOrUpdate(protectedRecord1);
        genericService.saveOrUpdate(protectedRecord2);
        controller.setEntityType(DedupeableType.PERSON);
        controller.getSelectedDupeIds().addAll(Arrays.asList(person1.getId(), protectedRecord1.getId(), protectedRecord2.getId()));
        controller.prepare();
        controller.validate();
        controller.selectAuthority();
        assertTrue("expecting protected record error", controller.getActionErrors().contains(AuthorityManagementController.ERROR_TOO_MANY_PROTECTED_RECORDS));
    }
    
    @Test
    @Rollback
    public void testProtectedPersonRecordsCannotBeDeduped2() {
        setIgnoreActionErrors(true);
        Person person1 = createAndSaveNewPerson("person1@mailinator.com", "person1");
        Person protectedRecord = createAndSaveNewPerson("protectedRecord1@mailinator.com", "protectedRecord1");
        protectedRecord.setRegistered(true);
        genericService.saveOrUpdate(protectedRecord);
        controller.setEntityType(DedupeableType.PERSON);
        controller.getSelectedDupeIds().addAll(Arrays.asList(person1.getId(), protectedRecord.getId()));
        controller.setAuthorityId(person1.getId());
        controller.prepare();
        controller.validate();
        controller.mergeDuplicates();
        assertTrue("expecting protected record error", controller.getActionErrors().contains(AuthorityManagementController.ERROR_CANNOT_DEDUPE_PROTECTED_RECORDS));
    }
    
    @Test
    @Rollback
    //when you dedupe something, it should become a synonym of the authority record that took its place
    public void testSynonyms() throws InstantiationException, IllegalAccessException {
        Person authority = new Person("authority", "record", "authrec@tdar.org");
        Person dupe = new Person("dee", "duped", "deduped@tdar.org");
        Person dupe2 = new Person("reed", "undant", "redundant@tdar.org");
        genericService.save(authority);
        genericService.save(dupe);
        genericService.save(dupe2);
        controller.setEntityType(DedupeableType.PERSON);
        controller.getSelectedDupeIds().add(dupe.getId());
        controller.getSelectedDupeIds().add(dupe2.getId());
        controller.setAuthorityId(authority.getId());
        controller.prepare();
        controller.validate();
        controller.mergeDuplicates();
        
        //make sure the synonyms were persisted
        Person authority2 = genericService.find(Person.class, authority.getId());
        assertTrue("authority record should have duplicates", authority2.getSynonyms().size() > 0);
        
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
