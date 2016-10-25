package org.tdar.search.query.builder;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.search.service.CoreNames;

public class ResourceCollectionQueryBuilder extends QueryBuilder {

    public ResourceCollectionQueryBuilder() {
        this.setClasses(new Class<?>[] { ResourceCollection.class });
    }

    @Override
    public String getCoreName() {
        return CoreNames.COLLECTIONS;
    }
}
