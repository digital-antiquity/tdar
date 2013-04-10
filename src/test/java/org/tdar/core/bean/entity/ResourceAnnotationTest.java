package org.tdar.core.bean.entity;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.openqa.selenium.internal.selenesedriver.GetElementAttribute;
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

        Long id = -1L;
        k1.setId(id);
        k2.setId(id);
        Assert.assertNotEquals("equality based on 'business key',  values should be different", k1, k2);
        Assert.assertNotEquals("equality based on 'business key',  values should be different", a1, a2);

        // making ID non-transient  (shouldn't make a difference on hashcode or equality for this class)
        id = 1L;
        a1.setId(id);
        a2.setId(id);
        Assert.assertNotEquals("equality based on 'business key',  values should still be different", a1, a2);
        logger.info(String.format("%s==%s %s==%s", a1.hashCode(), a2.hashCode(), a1.getEqualityFields(), a2.getEqualityFields()));
        
        
        // ensure the set has two items since these two annotations are unique
        Set<ResourceAnnotation> set = new HashSet<ResourceAnnotation>();
        set.add(a1);
        set.add(a2);
        Assert.assertEquals("set should have one item in it", 2, set.size());
        
        
        //create a new resourceAnnotation that is the same as (but not identical to) a1
        ResourceAnnotation a1c = new ResourceAnnotation();
        ResourceAnnotationKey k1c = new ResourceAnnotationKey();
        //feed original string through buffer to force equal string that is not identical 
        a1c.setValue( (new StringBuffer(a1.getValue())).toString());
        k1c.setKey( ( new StringBuffer( k1.getKey() ) ).toString() );  
        a1c.setResourceAnnotationKey(k1c);
        
        Assert.assertEquals("expecting equal but not identical", a1, a1c);
        Assert.assertNotSame("expecting equal but not identical", a1, a1c);
        
        Assert.assertEquals("expecting equal but not identical", k1.getKey(), k1c.getKey());
        Assert.assertNotSame("expecting equal but not identical", k1.getKey(), k1c.getKey());
        
        
    }

}
