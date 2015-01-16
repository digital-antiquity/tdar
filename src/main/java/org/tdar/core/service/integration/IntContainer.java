package org.tdar.core.service.integration;

import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Simple wrapper for an int for incrementing without object generation
 * 
 * @author abrin
 *
 */
@JsonAutoDetect
public class IntContainer {

    private int val = 0;

    @JsonView(JsonIntegrationFilter.class)
    public int getVal() {
        return val;
    }

    public void increment() {
        val++;
    }

    public void add(int num) {
        val += num;
    }

    @Override
    public String toString() {
        return Integer.toString(val);
    }
}
