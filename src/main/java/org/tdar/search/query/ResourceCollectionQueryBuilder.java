package org.tdar.search.query;

import org.tdar.core.bean.collection.ResourceCollection;

public class ResourceCollectionQueryBuilder extends QueryBuilder {

    public ResourceCollectionQueryBuilder() {
        this.setClasses(new Class<?>[] { ResourceCollection.class });
    }
}
