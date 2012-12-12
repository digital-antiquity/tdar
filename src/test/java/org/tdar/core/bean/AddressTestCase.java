package org.tdar.core.bean;

import org.junit.Assert;
import org.junit.Test;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.bean.entity.AddressType;
import org.tdar.core.exception.TdarValidationException;

public class AddressTestCase {

    @Test
    public void test() {
        Address address = new Address();
        assertAddressInvalid(address, Address.STREET_ADDRESS_IS_REQUIRED);
        address.setStreet1("street1");
        assertAddressInvalid(address, Address.CITY_IS_REQUIRED);
        address.setCity("city");
        assertAddressInvalid(address, Address.STATE_IS_REQUIRED);
        address.setState("state");
        assertAddressInvalid(address, Address.COUNTRY_IS_REQUIRED);
        address.setCountry("country");
        assertAddressInvalid(address, Address.POSTAL_CODE_IS_REQUIRED);
        address.setPostal("postal");
        assertAddressInvalid(address, Address.ADDRESS_TYPE_IS_REQUIRED);
        address.setType(AddressType.BILLING);
        Assert.assertTrue(address.isValid());
    }

    private void assertAddressInvalid(Address address, String reason) {
        TdarValidationException tv = null;
        try {
            address.isValid();
        } catch (TdarValidationException ex) {
            tv = ex;
        }
        Assert.assertNotNull(tv);
        if (reason != null) {
            Assert.assertEquals(reason, tv.getMessage());
        }
    }

}
