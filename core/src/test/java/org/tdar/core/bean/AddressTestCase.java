package org.tdar.core.bean;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.utils.MessageHelper;

public class AddressTestCase {

    @Test
    public void test() {
        Address address = new Address();
        AbstractIntegrationTestCase.assertInvalid(address, MessageHelper.getMessage("address.street_required"));
        address.setStreet1("street1");
        AbstractIntegrationTestCase.assertInvalid(address, MessageHelper.getMessage("address.city_required"));
        address.setCity("city");
        AbstractIntegrationTestCase.assertInvalid(address, MessageHelper.getMessage("address.state_required"));
        address.setState("state");
        AbstractIntegrationTestCase.assertInvalid(address, MessageHelper.getMessage("address.country_required"));
        address.setCountry("country");
        AbstractIntegrationTestCase.assertInvalid(address, MessageHelper.getMessage("address.postal_required"));
        address.setPostal("postal");
        AbstractIntegrationTestCase.assertInvalid(address, MessageHelper.getMessage("address.type_required"));
        address.setType(AddressType.BILLING);
        Assert.assertTrue(address.isValid());
    }

}
