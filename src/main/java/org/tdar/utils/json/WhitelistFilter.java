package org.tdar.utils.json;

import java.util.HashSet;

import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

/**
 * A simple property filter that accepts a list of property names, irrespective of the class that
 * property belongs to.
 * 
 * @author jimdevos
 * 
 */
public class WhitelistFilter implements PropertyFilter {

    private final HashSet<String> whitelist = new HashSet<String>();

    public WhitelistFilter(String propertyName) {
        add(propertyName);
    }

    public WhitelistFilter(String... propertyNames) {
        add(propertyNames);
    }

    /**
     * Returns true if propertyName should be filtered out, false if it should be included.
     * 
     * @see {@link PropertyFilter#apply(Object, String, Object)}
     * @see JsonConfig
     */
    @Override
    public boolean apply(Object obj, String propertyName, Object ignoredPropertyValue) {
        return !whitelist.contains(propertyName);
    }

    /**
     * add a property to the whitelist
     * 
     * @param propertyName
     */
    public void add(String propertyName) {
        whitelist.add(propertyName);
    }

    /**
     * add properties to the whitelist
     * 
     * @param propertyNames
     */
    public void add(String... propertyNames) {
        for (String propertyName : propertyNames) {
            whitelist.add(propertyName);
        }
    }

    @Override
    public String toString() {
        return whitelist.toString();
    }

}
