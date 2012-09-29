package org.tdar.search.query;

import org.tdar.core.bean.resource.ResourceAnnotationKey;

public class ResourceAnnotationKeyQueryBuilder extends QueryBuilder{

    public ResourceAnnotationKeyQueryBuilder() {
        this.setClasses(new Class<?>[]{ResourceAnnotationKey.class});
    }
}
