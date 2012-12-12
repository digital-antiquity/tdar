package org.tdar.core.bean;

import org.junit.Test;
import org.tdar.core.bean.entity.Address;
import org.tdar.core.exception.TdarValidationException;

public class AddressTestCase {

    @Test
    public void test() {
        Address address = new Address();
        assertAddressInvalid(address);
    }

    private void assertAddressInvalid(Address address) {
        try {
            
        } catch (TdarValidationException ex) {
            
        }
    }

}
