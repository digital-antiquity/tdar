package org.tdar.core.bean.entity;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

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

        //FIXME: technically hashcodes don't *need* to be different if they not equal.
//        Assert.assertNotSame("these keys should have different hashcodes", k1.hashCode(), k2.hashCode());
        // set the id to be the same, they should *still* have different hashcodes
        Long id = -1L;
        k1.setId(id);
        k2.setId(id);
//        Assert.assertNotSame("these keys should have different hashcodes", k1.hashCode(), k2.hashCode());
        Assert.assertNotEquals("equality based on field,  values should be different", k1, k2);
        Assert.assertEquals("equality based on id, values should be equal", a1, a2);
        
//        Assert.assertNotSame("these annotations should have different hashcodes", a1.hashCode(), a2.hashCode());

        a1.setId(id);
        a2.setId(id);
//        Assert.assertNotSame("these annotations should have different hashcodes", a1.hashCode(), a2.hashCode());
        Assert.assertEquals("equality based on id,  should be equal", a1, a2);

        // okay, put these in a set and make sure the set has one item
        Set<ResourceAnnotation> set = new HashSet<ResourceAnnotation>();
        set.add(a1);
        set.add(a2);
        Assert.assertEquals("set should have one item in it", 1, set.size());
    }

}
