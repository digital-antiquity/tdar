package org.tdar.struts.action;

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
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.exception.StatusCode;
import org.tdar.struts.action.entity.AbstractCreatorController;
import org.tdar.struts.action.entity.PersonController;
import org.tdar.struts.action.entity.TdarUserController;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.Action;

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

    @Test
    @Rollback
    public void addNullAddressToPerson() throws TdarActionException {
        setIgnoreActionErrors(true);
        Person p = createAndSaveNewPerson();
        Long presonId = p.getId();
        p = null;
        controller = generateNewInitializedController(PersonController.class);
        controller.setId(presonId);
        controller.prepare();
        String editAddress = controller.editAddress();
        assertEquals(Action.SUCCESS, editAddress);

        controller = generateNewInitializedController(PersonController.class);
        controller.setId(presonId);
        controller.prepare();
        String msg = null;
        controller.setAddress(null);
        controller.setServletRequest(getServletPostRequest());

        assertEquals(Action.INPUT, controller.saveAddress());
        assertEquals(MessageHelper.getMessage("address.street_required"), controller.getActionErrors().iterator().next());

    }

    @Test
    @Rollback
    public void addAddressToPerson() throws TdarActionException {
        Long presonId = addAddressToNewPerson();

        Person person = genericService.find(Person.class, presonId);
        assertEquals(1, person.getAddresses().size());
        assertEquals("85287", person.getAddresses().iterator().next().getPostal());
    }

    @Test
    @Rollback
    public void editAddressInitialize() throws TdarActionException {
        Long presonId = addAddressToNewPerson();
        Person person = genericService.find(Person.class, presonId);
        controller = generateNewInitializedController(PersonController.class);
        controller.setAddressId(person.getAddresses().iterator().next().getId());
        controller.setId(presonId);
        person = null;
        controller.prepare();
        controller.editAddress();
        assertEquals("85287", controller.getAddress().getPostal());
    }

    @Test
    @Rollback
    public void editAddressSave() throws TdarActionException {
        Long presonId = addAddressToNewPerson();
        Person person = genericService.find(Person.class, presonId);
        controller = generateNewInitializedController(PersonController.class);
        controller.setAddressId(person.getAddresses().iterator().next().getId());
        controller.setId(presonId);
        person = null;
        controller.prepare();
        assertEquals(controller.getAddressId(), controller.getAddress().getId());
        assertEquals("tempe", controller.getAddress().getCity());
        controller.getAddress().setCity("definitely not tempe");
        controller.setServletRequest(getServletPostRequest());
        controller.setReturnUrl("/test");
        String saveAddress = controller.saveAddress();
        assertEquals(AbstractCreatorController.RETURN_URL, saveAddress);
        controller = null;

        person = genericService.find(Person.class, presonId);

        assertEquals("85287", person.getAddresses().iterator().next().getPostal());
        assertNotEquals("tempe", person.getAddresses().iterator().next().getCity());
    }

    @Test
    @Rollback
    public void editAddressDelete() throws TdarActionException {
        final Long presonId = addAddressToNewPerson();
        Person person = genericService.find(Person.class, presonId);
        Long addressId = person.getAddresses().iterator().next().getId();
        // this seems hokey
        genericService.detachFromSession(person);
        person = null;
        controller = generateNewInitializedController(PersonController.class);
        controller.setAddressId(addressId);
        controller.setId(presonId);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        logger.info("hi");
        String saveAddress = controller.deleteAddress();
        assertEquals(Action.SUCCESS, saveAddress);
        controller = null;
        Person person_ = genericService.find(Person.class, presonId);
        assertEquals(0, person_.getAddresses().size());
        genericService.delete(person_);
    }

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

    private Long addAddressToNewPerson() throws TdarActionException {
        Person p = createAndSaveNewPerson();
        Long presonId = p.getId();
        p = null;
        controller = generateNewInitializedController(PersonController.class);
        controller.setId(presonId);
        controller.prepare();
        String editAddress = controller.editAddress();
        assertEquals(Action.SUCCESS, editAddress);

        controller = generateNewInitializedController(PersonController.class);
        controller.setId(presonId);
        controller.prepare();
        controller.setServletRequest(getServletPostRequest());
        Address address = controller.getAddress();
        address.setCity("tempe");
        address.setState("Arizona");
        address.setStreet1("street");
        address.setCountry("USA");
        address.setPostal("85287");
        address.setType(AddressType.BILLING);
        String saveAddress = controller.saveAddress();
        assertEquals(Action.SUCCESS, saveAddress);
        evictCache();
        return presonId;
    }

}
