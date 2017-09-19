package org.tdar.core.bean.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.apache.commons.lang.ClassUtils;
import org.junit.Test;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Resource;

public class ResourceEqualityTest {

    @Test
    public void testIsAssignable() {
        assertFalse(ClassUtils.isAssignable(Resource.class, Document.class));
        assertTrue(ClassUtils.isAssignable(Image.class, Resource.class));
    }
    
    @Test
    // pick a class that doesn't overRide the base Persistable.Base implementation of equalityFields. We are trying to assert that it's behavior
    // w.r.t equality is the same as Object.equals() and Object.hashCode()
    public void testBaseEqualityShouldBeBasedOnIdentity() {

        Resource r1 = new Resource();
        Resource r2 = new Resource();

        assertNotSame("objects are not identical", r1, r2);
        r1.equals(r2);
        assertNotSame("base implementation states that two objects are only equal if they are identical", r1, r2);
        assertEquals("per equals() 'contract' an object must be equal to itself", r1, r1);
        assertEquals("per equals() 'contract' an object must be equal to itself", r2, r2);

        // since equality and hashCode are strictly based on identity expect that they will be considered unique w.r.t a Set
        HashSet<Resource> set = new HashSet<Resource>();
        set.add(r1);
        set.add(r2);
        assertEquals(2, set.size());

        set.remove(r1);
        assertEquals(1, set.size());

        // it shouldn't matter if we change field value for r2; the hashCode hasn't changed so we can still pull r2 out of a set
        r2.setTitle("this is a test");
        boolean wasFound = set.remove(r2);
        assertTrue("r2 should have been found in set", wasFound);

    }

}
