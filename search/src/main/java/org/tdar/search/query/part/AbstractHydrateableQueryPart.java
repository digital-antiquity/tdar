package org.tdar.search.query.part;

import org.tdar.core.bean.Persistable;

/**
 * For Persistables, we may send in a "skeleton" that just has an ID. For those, we will need to hydrate them to be able to show a useful
 * field description
 * 
 * @author abrin
 *
 * @param <C>
 */
public abstract class AbstractHydrateableQueryPart<C extends Persistable> extends FieldQueryPart<C> {

    private Class<C> actualClass;

    public Class<C> getActualClass() {
        return actualClass;
    }

    public void setActualClass(Class<C> actualClass) {
        this.actualClass = actualClass;
    }

}
