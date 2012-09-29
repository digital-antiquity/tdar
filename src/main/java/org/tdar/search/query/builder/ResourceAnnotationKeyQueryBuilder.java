package org.tdar.search.query.builder;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.tdar.core.bean.resource.ResourceAnnotationKey;

public class ResourceAnnotationKeyQueryBuilder extends QueryBuilder{

    public ResourceAnnotationKeyQueryBuilder() {
        this.setClasses(new Class<?>[]{ResourceAnnotationKey.class});
    }
    
    @Override
    protected Map<String, Class<? extends Analyzer>> createPartialLabelOverrides() {
        //ignore any partial label overrides defined by parent, we don't want any.
        return null;
    }
}
