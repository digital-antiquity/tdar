/**
 * 
 */
package org.tdar.search.query.builder;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.log4j.Logger;

/**
 * $$Id$$
 * 
 * This class serves two separate purposes:
 * (a) to pass an object that encapsulates all Field properties into
 * a single object.
 * 
 * (b) to create a series of static methods that introspect a class or
 * class hierarchy in order to determine all of the @Fields specified
 * by that class.
 * 
 * @author $$Author$$
 * @version $$Revision$$
 */

public class DynamicQueryComponent implements Comparable<DynamicQueryComponent> {
    private String label;
    private Class<?> analyzer;
    private String parent;
    private Logger logger = Logger.getLogger(DynamicQueryComponent.class);

    /**
     * Basic constructor
     * 
     * @param label_
     * @param analyzerClass
     * @param parent2
     */
    public DynamicQueryComponent(String label_, Class<?> analyzerClass, String parent2) {
        this.analyzer = analyzerClass;
        this.label = label_;
        this.parent = parent2;
    }

    public String getLabel() {
        return DynamicQueryComponentHelper.addParent(parent, label);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Class<?> getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Class<?> analyzer) {
        this.analyzer = analyzer;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (o == this)
            return true;
        if (o instanceof DynamicQueryComponent) {
            return toString().equals(o.toString());
        }
        return false;
    }

    @Override
    public int compareTo(DynamicQueryComponent o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(9, 37).append(this.toString()).toHashCode();
    }

    @Override
    public String toString() {
        if (this.getAnalyzer() == null)
            return this.getLabel() + " - null";
        return this.getParent() + this.getLabel() + " - " + this.getAnalyzer().getCanonicalName();
    }
}
