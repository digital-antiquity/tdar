package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.DedupeableType;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.admin.authority.AuthorityManagementController;
import org.tdar.utils.MessageHelper;

public class AuthorityManagementControllerITCase extends AbstractAdminControllerITCase {

    private AuthorityManagementController controller;

    @Autowired
    private GenericService genericService;

    @Before
    public void setup() {
        controller = generateNewInitializedController(AuthorityManagementController.class);
    }

    @Test
    public void testIndex() {
        // not much to do here, other than check out the getters and make sure we are skipping validation
        assertTrue(CollectionUtils.isNotEmpty(controller.getDedupeableTypes()));
    }

    @Test
    public void testSelectAuthorityNoRequestVariables() {
        setIgnoreActionErrors(true);
        controller.validate();
        controller.selectAuthority();
        assertEquals("should be only one action error.  contents:" + controller.getActionErrors(), 2, controller.getActionErrors().size());
        assertTrue("Expecting no dupes error ",
                controller.getActionErrors().contains(MessageHelper.getMessage("authorityManagementController.error_no_duplicates")));
        assertTrue("Expecting 'select a type' error",
                controller.getActionErrors().contains(MessageHelper.getMessage("authorityManagementController.error_no_entity_type")));
    }

    @Test
    public void testSelectAuthorityNotEnoughDupes() {
        setIgnoreActionErrors(true);
        controller.setEntityType(DedupeableType.INSTITUTION);
        controller.getSelectedDupeIds().add(1L);
        controller.validate();
        controller.selectAuthority();
        assertTrue("expecting not enough dupes ",
                controller.getActionErrors().contains(MessageHelper.getMessage("authorityManagementController.error_not_enough_duplicates")));

    }

    @Test
    @Rollback
    public void testMergeDuplicatesNoAuthority() {
        setIgnoreActionErrors(true);
        controller.setEntityType(DedupeableType.INSTITUTION);
        controller.getSelectedDupeIds().add(1L);
        controller.validate();
        controller.mergeDuplicates();
        assertTrue("no authority", controller.getActionErrors().contains(MessageHelper.getMessage("authorityManagementController.error_no_authority_record")));
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

        // assert that the dupe is gone.
        dupe = genericService.find(Institution.class, dupeId);
        assertEquals(Status.DUPLICATE, dupe.getStatus());

        // this syncronize is necessary (apparently) because we need to ensure that any pending deletes that may throw key violations fire
        // before this test terminates.
        evictCache();
        sendEmailProcess.setEmailService(emailService);
        sendEmailProcess.execute();
        Email received = checkMailAndGetLatest("Records Merged");

        assertTrue(received.getSubject().contains(MessageHelper.getMessage("authorityManagementService.service_name")));
        assertTrue(received.getMessage().contains("Records Merged"));
        assertEquals(received.getFrom(), emailService.getFromEmail());
        assertEquals(received.getTo(), getTdarConfiguration().getSystemAdminEmail());
    }

    @Test
    @Rollback
    public void testProtectedPersonRecordsCannotBeDeduped() {
        setIgnoreActionErrors(true);
        Person person1 = createAndSaveNewPerson("person1@tdar.net", "person1");
        Person protectedRecord1 = createAndSaveNewPerson("protectedRecord1@tdar.net", "protectedRecord1");
        Person protectedRecord2 = createAndSaveNewPerson("protectedRrecord2@tdar.net", "protectedRecord2");
        genericService.saveOrUpdate(protectedRecord1);
        genericService.saveOrUpdate(protectedRecord2);
        controller.setEntityType(DedupeableType.PERSON);
        controller.getSelectedDupeIds().addAll(Arrays.asList(person1.getId(), protectedRecord1.getId(), protectedRecord2.getId()));
        controller.prepare();
        controller.validate();
        controller.selectAuthority();
        assertEquals(MessageHelper.getMessage("authorityManagementController.error_too_many_protected_records"), controller.getActionErrors().iterator().next());
    }

    @Test
    @Rollback
    public void testProtectedPersonRecordsCannotBeDeduped2() {
        setIgnoreActionErrors(true);
        Person person1 = createAndSaveNewPerson("person1@tdar.net", "person1");
        Person protectedRecord = createAndSaveNewPerson("protectedRecord1@tdar.net", "protectedRecord1");
        genericService.saveOrUpdate(protectedRecord);
        controller.setEntityType(DedupeableType.PERSON);
        controller.getSelectedDupeIds().addAll(Arrays.asList(person1.getId(), protectedRecord.getId()));
        controller.setAuthorityId(person1.getId());
        controller.prepare();
        controller.validate();
        controller.mergeDuplicates();
        assertTrue("expecting protected record error",
                controller.getActionErrors().contains(MessageHelper.getMessage("authorityManagementController.error_cannot_dedupe_protected_records")));
    }

    @Test
    @Rollback
    // when you dedupe something, it should become a synonym of the authority record that took its place
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

        // make sure the synonyms were persisted
        Person authority2 = genericService.find(Person.class, authority.getId());
        assertTrue("authority record should have duplicates", authority2.getSynonyms().size() > 0);

    }

    // make two institutions (auth and dupe), and then associate it w/ all manner of tdar stuff
    private void makeSomeInstitutionReferences(Institution authority, Institution dupe) throws Exception {
        genericService.save(authority);
        genericService.save(dupe);

        // reference via document.resourceProviderInstitution
        Document document = createAndSaveNewInformationResource(Document.class);
        document.setResourceProviderInstitution(dupe);
        genericService.saveOrUpdate(document);

        // reference via resourceCreator.creator
        ResourceCreator rc = new ResourceCreator(dupe, ResourceCreatorRole.COLLABORATOR);
        rc.setSequenceNumber(1);
        document.getResourceCreators().add(rc);
        genericService.saveOrUpdate(rc);

        // reference via person.institution
        Person person = new Person("john", "doe", "johndoe123@tdar.net");
        person.setInstitution(dupe);
        genericService.saveOrUpdate(person);
    }

    // TODO: test for max dupe size.

}
