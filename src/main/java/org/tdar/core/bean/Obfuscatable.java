package org.tdar.core.bean;

import java.util.Set;

/**
 * allows for abstraction of all resources that can be obfuscated
 * 
 * @author abrin
 * 
 */
public interface Obfuscatable {

    /**
     * check whether the object is obfuscated or not
     */
    boolean isObfuscated();

    Long getId();

    /**
     * obfuscates the current object and returns a list of potential objects for further obfuscation
     */
    Set<Obfuscatable> obfuscate();

    /**
     * mark the object as obfuscated, ideally should be transient.
     * This does not actually obfuscate the object as implemented by instances of this interface: it is only called by
     * the implementations of the obfuscate method. Given obfuscate() is a one way street, should this method be removed?
     */
    @Deprecated
    void setObfuscated(boolean obfuscated);

    Boolean getObfuscatedObjectDifferent();

    void setObfuscatedObjectDifferent(Boolean value);

}
