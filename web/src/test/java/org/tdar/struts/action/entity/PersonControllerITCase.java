package org.tdar.struts.action.entity;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.exception.StatusCode;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.struts_base.action.TdarActionException;

public class PersonControllerITCase extends AbstractAdminControllerITCase {

    PersonController controller;

    @Before
    public void before() {
        controller = generateNewInitializedController(PersonController.class);

    }

    @Test
    @Rollback
    public void testSavingPerson() throws Exception {
        // simulate the edit
        TdarUserController uc = generateNewInitializedController(TdarUserController.class, getAdminUser());
        uc.setId(1L);
        uc.prepare();
        uc.edit();
        Assert.assertEquals(uc.getPersistable().getFirstName().toLowerCase(), "allen");

        // simulate the save()
        uc = generateNewInitializedController(TdarUserController.class);
        uc.setId(1L);
        uc.prepare();
        Person p = uc.getPerson();
        p.setFirstName("bill");
        uc.setServletRequest(getServletPostRequest());
        uc.save();

        // ensure stuff was changed.
        p = null;
        p = genericService.find(Person.class, 1L);
        Assert.assertEquals("bill", p.getFirstName().toLowerCase());
    }

    @Test
    @Rollback
    public void testContributorChange() throws Exception {
        // simulate the edit
        TdarUser user = createAndSaveNewPerson("dfsd@sdasdf.com", "non");
        user.setContributorAgreementVersion(-1);
        user.setContributor(false);
        genericService.saveOrUpdate(user);
        TdarUserController uc = generateNewInitializedController(TdarUserController.class, user);
        Long userId = user.getId();
        user = null;

        uc.setId(userId);
        uc.prepare();
        uc.edit();
        logger.debug("{}", uc.getPersistable());
        // Assert.assertEquals(uc.getPersistable().getFirstName().toLowerCase(), "allen");
        assertEquals(false, uc.getContributor());
        uc.setContributor(true);
        uc.setServletRequest(getServletPostRequest());
        uc.save();

        user = null;
        genericService.synchronize();
        user = genericService.find(TdarUser.class, userId);
        logger.debug("version: {}; contributor: {}", user.getContributorAgreementVersion(), user.isContributor());
        Assert.assertEquals(Boolean.TRUE, user.isContributor());
        Assert.assertNotEquals(-1, user.getContributorAgreementVersion().intValue());
    }

    @Test
    @Rollback
    public void testEditingPersonByNonAdmin() throws Exception {
        setIgnoreActionErrors(true);
        // simulate a basicuser trying to edit the adminuser record
        controller = generateNewController(PersonController.class);
        init(controller, getBasicUser());
        controller.setId(getAdminUserId());

        // first off, ensure they can't even get to the edit page
        StatusCode code = null;
        try {
            controller.prepare();
            controller.edit();
        } catch (TdarActionException e) {
            code = e.getResponseStatusCode();
        }
        Assert.assertEquals(StatusCode.FORBIDDEN, code);

        // so far so good - now ensure they can't spoof a save request
        controller = generateNewController(PersonController.class);
        init(controller, getBasicUser());
        controller.setId(getAdminUserId());
        String oldLastName = getAdminUser().getLastName();
        final String newLastName = oldLastName.concat(" updated");
        try {
            controller.prepare();

            controller.getPerson().setLastName(newLastName);
            controller.setServletRequest(getServletPostRequest());
            code = null;
            controller.save();
        } catch (TdarActionException e) {
            code = e.getResponseStatusCode();
        }
        Assert.assertEquals(StatusCode.FORBIDDEN, code);

        // did hibernate save the person record anyway?
        evictCache();
        runInNewTransaction(new TransactionCallback<Person>() {
            @Override
            public Person doInTransaction(TransactionStatus status) {
                Person admin = entityService.find(getAdminUserId());
                Assert.assertFalse("name shouldn't have been changed", admin.getLastName().equals(newLastName));
                return admin;
            }

        });
    }

    @SuppressWarnings("unused")
    @Test
    @Rollback
    public void editEmailAlreadyInUse() throws TdarActionException {
        setIgnoreActionErrors(true);
        String email1 = "email1@tdar.org";
        Person existingUser = createAndSaveNewPerson(email1, "user1");
        controller = generateNewInitializedController(PersonController.class, getUser());
        controller.setId(getUserId());
        controller.prepare();
        controller.setEmail(email1);
        controller.setServletRequest(getServletPostRequest());
        controller.validate();
        assertThat(controller.getFieldErrors(), hasKey("email"));
    }

    @Test
    @Rollback
    // make sure none of the validators fail if we aren't making any changes
    public void testSaveWithNoChanges() throws TdarActionException {
        // change the first name but leave the email alone
        controller.setId(getUserId());
        controller.prepare();
        controller.setEmail(controller.getPerson().getEmail());
        controller.setServletRequest(getServletPostRequest());
        controller.validate();
        assertThat(controller.getFieldErrors().keySet(), empty());
    }

    @Test
    @Rollback
    public void editNewEmail() throws TdarActionException {
        String email1 = "email1@tdar.org";
        controller = generateNewInitializedController(PersonController.class, getUser());
        controller.setId(getUserId());
        controller.prepare();
        controller.setEmail(email1);
        controller.setServletRequest(getServletPostRequest());
        controller.validate();
        assertThat(controller.getFieldErrors().keySet(), empty());
    }

    @Test
    @Rollback
    public void testBlankEmailForActiveUser() throws TdarActionException {
        setIgnoreActionErrors(true);
        TdarUserController uc = generateNewInitializedController(TdarUserController.class, getAdminUser());
        uc.setId(getUserId());
        uc.prepare();
        uc.setEmail("");
        uc.setServletRequest(getServletPostRequest());
        uc.validate();
        assertThat(uc.getFieldErrors(), hasKey("email"));
    }

}
