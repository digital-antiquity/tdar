package org.tdar.core.bean;

import java.util.List;

public interface Obfuscatable {

    /*
     * check whether the object is obfuscated or not
     */
    public boolean isObfuscated();

    /*
     * obfuscates the current object and returns a list of potential objects for further obfuscation
     */
    public List<Obfuscatable> obfuscate();

    /*
     * mark the object as obfuscated, ideally should be transient
     */
    public void setObfuscated(boolean obfuscated);
}
