package org.tdar.core.service.integration;

/**
 * Simple wrapper for an int for incrementing without object generation
 * @author abrin
 *
 */
public class IntContainer {

    private int val = 0;
    
    public int getVal() {
        return val;
    }
    
    public void increment() {
        val = val +1;
    }
    
    public void add(int num) {
        val = val + num;
    }
    
    @Override
    public String toString() {
        return Integer.toString(val);
    }
}
