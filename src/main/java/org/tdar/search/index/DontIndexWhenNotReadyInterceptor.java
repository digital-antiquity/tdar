package org.tdar.search.index;

import org.hibernate.search.indexes.interceptor.EntityIndexingInterceptor;
import org.hibernate.search.indexes.interceptor.IndexingOverride;
import org.tdar.core.bean.Indexable;

/**
 * Don't index adhoc coding sheets, and remove them if they were previously indexed (perhaps they were not generated and then they became generated??)
 * 
 * @author jimdevos
 * 
 */
public class DontIndexWhenNotReadyInterceptor implements EntityIndexingInterceptor<Indexable> {

    @Override
    public IndexingOverride onAdd(Indexable entity) {
        if (!entity.isReadyToIndex()) {
            return IndexingOverride.SKIP;
        }
        return IndexingOverride.APPLY_DEFAULT;
    }

    @Override
    public IndexingOverride onUpdate(Indexable entity) {
        if (!entity.isReadyToIndex()) {
            return IndexingOverride.REMOVE;
        }
        return IndexingOverride.APPLY_DEFAULT;
    }

    @Override
    public IndexingOverride onDelete(Indexable entity) {
        return IndexingOverride.APPLY_DEFAULT;
    }

    @Override
    public IndexingOverride onCollectionUpdate(Indexable entity) {
        return onUpdate(entity);
    }

}
