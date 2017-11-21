package org.tdar.struts.action.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.bean.entity.Person;
import org.tdar.struts.action.AbstractAdminControllerITCase;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.Action;

public class AddressControllerITCase extends AbstractAdminControllerITCase {

    AddressController controller;

    @Before
    public void before() {
        controller = generateNewInitializedController(AddressController.class);

    }


    @Test
    @Rollback
    public void addNullAddressToPerson() throws Exception {
        setIgnoreActionErrors(true);
        Person p = createAndSaveNewUser();
        Long presonId = p.getId();
        p = null;
        controller = generateNewInitializedController(AddressController.class);
        controller.setId(presonId);
        controller.prepare();
        String editAddress = controller.editAddress();
        assertEquals(Action.SUCCESS, editAddress);

        controller = generateNewInitializedController(AddressController.class);
        controller.setId(presonId);
        controller.setAddress(null);
        controller.prepare();
        controller.validate();

        assertEquals(MessageHelper.getMessage("address.street_required"), controller.getActionErrors().iterator().next());
    }

    @Test
    @Rollback
    public void addAddressToPerson() throws Exception {
        Long presonId = addAddressToNewPerson();

        Person person = genericService.find(Person.class, presonId);
        assertEquals(1, person.getAddresses().size());
        assertEquals("85287", person.getAddresses().iterator().next().getPostal());
    }

    @Test
    @Rollback
    public void editAddressInitialize() throws Exception {
        Long presonId = addAddressToNewPerson();
        Person person = genericService.find(Person.class, presonId);
        controller = generateNewInitializedController(AddressController.class);
        controller.setAddressId(person.getAddresses().iterator().next().getId());
        controller.setId(presonId);
        person = null;
        controller.prepare();
        controller.editAddress();
        assertEquals("85287", controller.getAddress().getPostal());
    }

    @Test
    @Rollback
    public void editAddressSave() throws Exception {
        Long presonId = addAddressToNewPerson();
        Person person = genericService.find(Person.class, presonId);
        controller = generateNewInitializedController(AddressController.class);
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
        assertEquals(AddressController.RETURN_URL, saveAddress);
        controller = null;

        person = genericService.find(Person.class, presonId);

        assertEquals("85287", person.getAddresses().iterator().next().getPostal());
        assertNotEquals("tempe", person.getAddresses().iterator().next().getCity());
    }

    @Test
    @Rollback
    public void editAddressDelete() throws Exception {
        final Long presonId = addAddressToNewPerson();
        Person person = genericService.find(Person.class, presonId);
        Long addressId = person.getAddresses().iterator().next().getId();
        // this seems hokey
        genericService.detachFromSession(person);
        person = null;
        controller = generateNewInitializedController(AddressController.class);
        controller.setAddressId(addressId);
        controller.setId(presonId);
        controller.prepare();
        controller.validate();
        controller.setServletRequest(getServletPostRequest());
        logger.info("hi");
        String saveAddress = controller.deleteAddress();
        assertEquals(Action.SUCCESS, saveAddress);
        controller = null;
        Person person_ = genericService.find(Person.class, presonId);
        assertEquals(0, person_.getAddresses().size());
        genericService.delete(person_);
    }

    

    private Long addAddressToNewPerson() throws Exception {
        Person p = createAndSaveNewUser();
        Long presonId = p.getId();
        p = null;
        controller = generateNewInitializedController(AddressController.class);
        controller.setId(presonId);
        controller.prepare();
        String editAddress = controller.editAddress();
        assertEquals(Action.SUCCESS, editAddress);

        controller = generateNewInitializedController(AddressController.class);
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
