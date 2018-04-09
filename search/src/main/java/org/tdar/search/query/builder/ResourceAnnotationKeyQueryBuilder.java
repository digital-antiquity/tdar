package org.tdar.search.query.builder;

import org.tdar.search.service.CoreNames;

public class ResourceAnnotationKeyQueryBuilder extends QueryBuilder {

    @Override
    public String getCoreName() {
        return CoreNames.ANNOTATION_KEY;
    }

}
