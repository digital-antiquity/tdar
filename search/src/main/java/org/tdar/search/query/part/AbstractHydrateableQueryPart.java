package org.tdar.search.query.part;

import org.tdar.core.bean.Persistable;

public abstract class AbstractHydrateableQueryPart<C extends Persistable> extends FieldQueryPart<C> {

    private Class<C> actualClass;

    public Class<C> getActualClass() {
        return actualClass;
    }

    public void setActualClass(Class<C> actualClass) {
        this.actualClass = actualClass;
    }

}
