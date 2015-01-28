package org.tdar.struts.interceptor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.tdar.core.bean.TdarGroup;

/**
 * $Id$
 * 
 * Marks controllers as requiring the specified group membership in order to access any of that controller's actions / methods.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface RequiresTdarUserGroup {

    public TdarGroup value() default TdarGroup.TDAR_USERS;

}
