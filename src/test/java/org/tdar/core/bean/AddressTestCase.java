package org.tdar.core.bean;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;

public class AddressTestCase {

    @Test
    public void test() {
        Address address = new Address();
        AbstractIntegrationTestCase.assertInvalid(address, Address.STREET_ADDRESS_IS_REQUIRED);
        address.setStreet1("street1");
        AbstractIntegrationTestCase.assertInvalid(address, Address.CITY_IS_REQUIRED);
        address.setCity("city");
        AbstractIntegrationTestCase.assertInvalid(address, Address.STATE_IS_REQUIRED);
        address.setState("state");
        AbstractIntegrationTestCase.assertInvalid(address, Address.COUNTRY_IS_REQUIRED);
        address.setCountry("country");
        AbstractIntegrationTestCase.assertInvalid(address, Address.POSTAL_CODE_IS_REQUIRED);
        address.setPostal("postal");
        AbstractIntegrationTestCase.assertInvalid(address, Address.ADDRESS_TYPE_IS_REQUIRED);
        address.setType(AddressType.BILLING);
        Assert.assertTrue(address.isValid());
    }

}
