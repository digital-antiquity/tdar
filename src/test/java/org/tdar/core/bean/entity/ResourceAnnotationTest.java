package org.tdar.core.bean.entity;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.tdar.core.bean.resource.ResourceAnnotation;
import org.tdar.core.bean.resource.ResourceAnnotationKey;

public class ResourceAnnotationTest {
    Logger logger = Logger.getLogger(getClass());
    
    @Test
    public void twoUniqueAnnotations() {

        ResourceAnnotation a1 = new ResourceAnnotation();
        ResourceAnnotation a2 = new ResourceAnnotation();
        ResourceAnnotationKey k1 = new ResourceAnnotationKey();
        ResourceAnnotationKey k2 = new ResourceAnnotationKey();
        String sameValue = "a value";

        k1.setKey("key1");
        k2.setKey("key2");
        a1.setResourceAnnotationKey(k1);
        a1.setValue(sameValue);
        a2.setResourceAnnotationKey(k2);
        a2.setValue(sameValue);

        Assert.assertNotSame("these keys should have different hashcodes", k1.hashCode(), k2.hashCode());
        // set the id to be the same, they should *still* have different hashcodes
        Long id = -1L;
        k1.setId(id);
        k2.setId(id);
        Assert.assertNotSame("these keys should have different hashcodes", k1.hashCode(), k2.hashCode());
        Assert.assertFalse(k1.equals(k2));
        Assert.assertFalse(a1.equals(a2));
        Assert.assertNotSame("these annotations should have different hashcodes", a1.hashCode(), a2.hashCode());

        a1.setId(id);
        a2.setId(id);
        Assert.assertNotSame("these annotations should have different hashcodes", a1.hashCode(), a2.hashCode());
        Assert.assertFalse(a1.equals(a2));

        // okay, put these in a set and make sure the set has two items
        Set<ResourceAnnotation> set = new HashSet<ResourceAnnotation>();
        set.add(a1);
        set.add(a2);
        Assert.assertEquals("set should have two items in it", 2, set.size());
    }

}
