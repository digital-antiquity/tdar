package org.tdar.search.index;

import org.hibernate.search.indexes.interceptor.EntityIndexingInterceptor;
import org.hibernate.search.indexes.interceptor.IndexingOverride;
import org.tdar.core.bean.resource.CodingSheet;

/**
 * Don't index adhoc coding sheets, and remove them if they were previously indexed (perhaps they were not generated and then they became generated??)
 * @author jimdevos
 *
 */
public class DontIndexWhenGeneratedInterceptor implements EntityIndexingInterceptor<CodingSheet>{

    @Override
    public IndexingOverride onAdd(CodingSheet entity) {
        if(entity.isGenerated()) {
            return IndexingOverride.SKIP;
        }
        return IndexingOverride.APPLY_DEFAULT;
    }

    @Override
    public IndexingOverride onUpdate(CodingSheet entity) {
        if(entity.isGenerated()) {
            return IndexingOverride.REMOVE;
        }
        return IndexingOverride.APPLY_DEFAULT;
    }

    @Override
    public IndexingOverride onDelete(CodingSheet entity) {
        return IndexingOverride.APPLY_DEFAULT;
    }

    @Override
    public IndexingOverride onCollectionUpdate(CodingSheet entity) {
        return onUpdate(entity);
    }

}
