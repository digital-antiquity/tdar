/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for tracking bulk import fields and providing a central location for all of the long descriptions.
 * 
 * @author Adam Brin
 * 
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface BulkImportField {

    // a way to tell the parser about subclasses (Creator -> Person/Institution)
    Class<?>[] implementedSubclasses() default {};

    // Whether the field is required or not
    boolean required() default false;

    // The sort order for the excel columns, lower means closer to the left. Sorting is within the class
    int order() default 0;

    String key() default "";

}
