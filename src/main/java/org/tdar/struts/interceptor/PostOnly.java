package org.tdar.struts.interceptor;



import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an Action method to only execute if the associated requests http method is POST
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface PostOnly {
}

