package org.tdar.struts_base.interceptor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * $Id$
 * 
 * Tells the Obfuscation Results Listener not to Obfuscate the object (getter); action (method); or entireClass
 *
 * FIXME: can we change reason to 'value' to leverage shorthand annotation syntax? (e.g. @DoNotObfuscate("not needed / performance test")
 * 
 * @author <a href='mailto:adam.brin@asu.edu'>Adam Brin</a>
 * @version $Rev$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface DoNotObfuscate {
    String reason();
}
