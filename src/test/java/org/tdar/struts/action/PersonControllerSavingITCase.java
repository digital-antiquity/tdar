package org.tdar.struts.action;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.entity.Person;
import org.tdar.struts.action.entity.PersonController;

import com.opensymphony.xwork2.Action;

/**
 * This IT attempts to simulate a non-user spoofing a person save request for a person record other than their own. Because struts modifies the fields
 * of a hibernate-managed object, there is the concern that hibernate will persist the change implicitly even if our controller action method returns an
 * error and doesn't explicitly save.
 * 
 * @author jimdevos
 * 
 */
@Ignore
public class PersonControllerSavingITCase extends AbstractAdminControllerITCase {
    // ADDING SO THAT WE DON'T HAVE KEY ISSUES WHEN TESTING
    private static final String PERSON_EMAIL = System.currentTimeMillis() + "personcontrollersavingitcaseemail@tdar.net";
    private static final String PERSON_FIRST_NAME_EXPECTED = "William";
    private static final String PERSON_FIRST_NAME_UPDATED = "Bill";

    Long personId;
    PersonController controller;

    @Test
    @Rollback(false)
    public void testEditingPersonByNonAdmin() throws Exception {
        setIgnoreActionErrors(true);

        Person person = new Person();
        person.setEmail(PERSON_EMAIL);
        person.setFirstName(PERSON_FIRST_NAME_EXPECTED);
        person.setLastName("Jones");
        entityService.save(person);
        personId = person.getId();

        // skipping the edit request - just trying to spoof a save
        controller = generateNewController(PersonController.class);
        init(controller, getBasicUser());
        controller.setId(personId);
        controller.prepare();

        Assert.assertEquals(PERSON_FIRST_NAME_EXPECTED, controller.getPerson().getFirstName());
        controller.getPerson().setFirstName(PERSON_FIRST_NAME_UPDATED);
        String result = controller.save();
        Assert.assertFalse("basic user shouldn't be able to save changes to another user's person record", Action.SUCCESS.equals(result));
        setVerifyTransactionCallback(new TransactionCallback<Person>() {
            @Override
            public Person doInTransaction(TransactionStatus status) {
                logger.debug("running 'testEditingPersonByNonAdmin' in new transaction");
                Person person = entityService.findByEmail(PERSON_EMAIL);
                Assert.assertEquals(PERSON_FIRST_NAME_EXPECTED, person.getFirstName());
                genericService.delete(person);
                return null;
            }
        });
    }

    @Test
    @Rollback(false)
    public void testEditingPersonByAdmin() throws Exception {
        setIgnoreActionErrors(true);
        Person person = new Person();
        person.setEmail(PERSON_EMAIL);
        person.setFirstName(PERSON_FIRST_NAME_EXPECTED);
        person.setLastName("Jones");
        entityService.save(person);
        personId = person.getId();

        // skipping the edit request - just trying to spoof a save
        controller = generateNewController(PersonController.class);
        init(controller, getAdminUser());
        controller.setId(personId);
        controller.prepare();
        Assert.assertEquals(PERSON_FIRST_NAME_EXPECTED, controller.getPerson().getFirstName());
        controller.getPerson().setFirstName(PERSON_FIRST_NAME_UPDATED);
        String result = controller.save();
        Assert.assertEquals("admin user should be able to save changes to another user's person record", Action.SUCCESS, result);
        Assert.assertTrue(controller.getActionErrors().isEmpty());
        evictCache();
        setVerifyTransactionCallback(new TransactionCallback<Person>() {
            @Override
            public Person doInTransaction(TransactionStatus status) {
                logger.debug("running 'testEditingPersonByAdmin' in new transaction");
                Person person = entityService.findByEmail(PERSON_EMAIL);
                Assert.assertEquals(PERSON_FIRST_NAME_UPDATED, person.getFirstName());
                genericService.delete(person);
                return null;
            }
        });

    }

}
