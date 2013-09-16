package org.tdar.core.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * A simple annotation to mark methods and fields as Transient when being converted to JSON
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
// @Deprecated
// TODO:do away with this JsonModel + @JSONTransient in favor of XmlService#toJson + JAXBAnnotations and/or JacksonAnnotations
public @interface JSONTransient {

}
