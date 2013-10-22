package org.tdar.struts.action;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.CombinableMatcher.either;
import static org.junit.Assert.*;

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
        // simulate the edit
        controller.setId(1L);
        controller.prepare();
        controller.edit();
        Assert.assertEquals(controller.getPersistable().getFirstName().toLowerCase(), "allen");

        // simulate the save()
        controller = generateNewInitializedController(PersonController.class);
        controller.setId(1L);
        controller.prepare();
        Person p = controller.getPerson();
        p.setFirstName("bill");
        controller.setServletRequest(getServletPostRequest());
        controller.save();

        // ensure stuff was changed.
        p = null;
        p = genericService.find(Person.class, 1L);
        Assert.assertEquals("bill", p.getFirstName().toLowerCase());
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testEditingPersonByNonAdmin() throws Exception {
        setIgnoreActionErrors(true);
        // simulate a basicuser trying to edit the adminuser record
        controller = generateNewController(PersonController.class);
        init(controller, getBasicUser());
        controller.setId(getAdminUserId());
        controller.prepare();

        // first off, ensure they can't even get to the edit page
        StatusCode code = null;
        try {
            controller.edit();
        } catch (TdarActionException e) {
            code = e.getResponseStatusCode();
        }
        Assert.assertEquals(StatusCode.FORBIDDEN, code);

        // so far so good - now ensure they can't spoof a save request
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
        Assert.assertEquals(StatusCode.FORBIDDEN, code);

        // did hibernate save the person record anyway?
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

    @Test
    @Rollback
    public void addNullAddressToPerson() throws TdarActionException {
        Person p = createAndSaveNewPerson();
        Long presonId = p.getId();
        p = null;
        controller = generateNewInitializedController(PersonController.class);
        controller.setId(presonId);
        controller.prepare();
        String editAddress = controller.editAddress();
        assertEquals(PersonController.SUCCESS, editAddress);

        controller = generateNewInitializedController(PersonController.class);
        controller.setId(presonId);
        controller.prepare();
        String msg = null;
        controller.setAddress(null);
        controller.setServletRequest(getServletPostRequest());
        
        assertEquals(PersonController.INPUT, controller.saveAddress());
        assertEquals(Address.STREET_ADDRESS_IS_REQUIRED, controller.getActionErrors().iterator().next());
        setIgnoreActionErrors(true);

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
        assertEquals(controller.getAddressId(),controller.getAddress().getId());
        assertEquals("tempe", controller.getAddress().getCity());
        controller.getAddress().setCity("definitely not tempe");
        controller.setServletRequest(getServletPostRequest());
        controller.setReturnUrl("/test");
        String saveAddress = controller.saveAddress();
        assertEquals(PersonController.RETURN_URL, saveAddress);
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
        Long addressId =  person.getAddresses().iterator().next().getId();
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
        assertEquals(PersonController.SUCCESS, saveAddress);
        controller = null;
        Person person_ = genericService.find(Person.class, presonId);
        assertEquals(0, person_.getAddresses().size());
        genericService.delete(person_);
    }

    @Test
    @Rollback
    public void editEmailAlreadyInUse() throws TdarActionException {
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
    //make sure none of the validators fail if we aren't making any changes
    public void testSaveWithNoChanges() throws TdarActionException {
        //change the first name but leave the email alone
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
        controller.setId(getUserId());
        controller.prepare();
        controller.setEmail("");
        controller.setServletRequest(getServletPostRequest());
        controller.validate();
        assertThat(controller.getFieldErrors(), hasKey("email"));
    }

    private Long addAddressToNewPerson() throws TdarActionException {
        Person p = createAndSaveNewPerson();
        Long presonId = p.getId();
        p = null;
        controller = generateNewInitializedController(PersonController.class);
        controller.setId(presonId);
        controller.prepare();
        String editAddress = controller.editAddress();
        assertEquals(PersonController.SUCCESS, editAddress);

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
        assertEquals(PersonController.SUCCESS, saveAddress);
        genericService.synchronize();
        return presonId;
    }

}
