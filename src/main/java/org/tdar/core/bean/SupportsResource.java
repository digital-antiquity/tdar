package org.tdar.core.bean;

import org.tdar.core.bean.resource.CategoryVariable;

public interface SupportsResource {

    public abstract CategoryVariable getCategoryVariable();

    public abstract void setCategoryVariable(CategoryVariable category);

}
