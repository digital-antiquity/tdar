package org.tdar.core.bean;

import java.util.List;

public interface Obfuscatable {

    /**
     * check whether the object is obfuscated or not
     */
    public boolean isObfuscated();

    /**
     * obfuscates the current object and returns a list of potential objects for further obfuscation
     */
    public List<Obfuscatable> obfuscate();

    /**
     * mark the object as obfuscated, ideally should be transient.
     * This does not actually obfuscate the object as implemented by implementations of this interface: it is generally called by 
     * the implementations of the obfuscate method. Given obfuscate() is a one way street, should this method be removed?
     */
    @Deprecated
    public void setObfuscated(boolean obfuscated);
}
