package org.tdar.junit;

import java.lang.annotation.*;


/**
 * Signal to AbstractControllerITCase to ignore action errors generated during a test
 * Created by jimdevos on 2/25/16.
 */
@Retention( value = RetentionPolicy.RUNTIME)
@Target( value = { ElementType.METHOD, ElementType.TYPE})
public  @interface IgnoreActionErrors {
}
