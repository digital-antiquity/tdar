package org.tdar.search.query.builder;

import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.search.service.CoreNames;

public class ResourceAnnotationKeyQueryBuilder extends QueryBuilder {

    public ResourceAnnotationKeyQueryBuilder() {
        this.setClasses(new Class<?>[] { ResourceAnnotationKey.class });
    }

    @Override
    public String getCoreName() {
        return CoreNames.ANNOTATION_KEY;
    }

}
