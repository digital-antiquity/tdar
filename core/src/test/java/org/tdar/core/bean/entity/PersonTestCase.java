package org.tdar.core.bean.entity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PersonTestCase {

    @Test
    public void testInitials() {
        Person p = new Person("adam", "test user", "");
        assertEquals("ATU", p.getInitials());
    }

}
