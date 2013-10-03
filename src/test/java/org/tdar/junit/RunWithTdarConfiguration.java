package org.tdar.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface RunWithTdarConfiguration {

    public String TDAR = "src/test/resources/tdar.properties";
    public String FAIMS = "src/test/resources/tdar.faims.properties";
    public String CREDIT_CARD = "src/test/resources/tdar.cc.properties";
    public String TOS_CHANGE = "src/test/resources/tdar.tos.properties";

    public String[] runWith() default {};

}
