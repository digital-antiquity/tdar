package org.tdar.core.bean;

import org.tdar.core.bean.resource.CategoryVariable;

/**
 * This interface denotes a sub-type of resource that's really a "supporting resource." i.e. Coding Sheets and Ontologies
 */
public interface SupportsResource {

    abstract CategoryVariable getCategoryVariable();

    abstract void setCategoryVariable(CategoryVariable category);

}
