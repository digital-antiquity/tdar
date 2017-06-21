package org.tdar.search.query.builder;

import org.tdar.search.index.LookupSource;
import org.tdar.search.service.CoreNames;

public class ResourceCollectionQueryBuilder extends QueryBuilder {

    public ResourceCollectionQueryBuilder() {
        setTypeLimit(LookupSource.COLLECTION.name());
    }
    
    @Override
    public String getCoreName() {
        return CoreNames.RESOURCES;
    }
}
