package org.tdar.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface RunWithTdarConfiguration {

    String TDAR = "src/test/resources/tdar.properties";
    String JAI_DISABLED = "src/test/resources/tdar.nojai.properties";
    String FAIMS = "src/test/resources/tdar.faims.properties";
    String CREDIT_CARD = "src/test/resources/tdar.cc.properties";
    String TOS_CHANGE = "src/test/resources/tdar.tos.properties";
    String TDAR_DISABLED_OBFUSCATION = "src/test/resources/tdar.no_obfs.properties";
    String BROKEN_KETTLE = "src/test/resources/tdar.broken.kettle.properties";
    String TDARDATA_SMALL_BATCH = "src/test/resources/tdar.small.batch.properties";
    String SMALL_EXCEL = "src/test/resources/tdar.small.excel.properties";

    public String[] runWith() default {};

}
