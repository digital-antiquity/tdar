package org.tdar.struts.interceptor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an Action method to only execute if the associated requests http method is GET (or HEAD)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
//FIXME: I don't like the name @GetOnly, since 'get' is ambiguous term in programming context, but I wanted to be consistent with @PostOnly. Maybe HttpGet, HttpPost, etc.?
//FIXME pt. 2:  CSharp has a
public @interface GetOnly {
}
