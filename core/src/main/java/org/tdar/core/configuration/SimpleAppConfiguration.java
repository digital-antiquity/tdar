package org.tdar.core.configuration;

import java.io.Serializable;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@SuppressWarnings("serial")
@ComponentScan(basePackages = { "org.tdar" },
        excludeFilters = {
                @Filter(type = FilterType.ASSIGNABLE_TYPE,
                        value = {
                                SimpleAppConfiguration.class
                        })
        })
@Configuration
public abstract class SimpleAppConfiguration extends AbstractAppConfiguration implements Serializable {

}
