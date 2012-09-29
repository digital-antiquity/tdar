package org.tdar.search.query.builder;

import org.tdar.core.bean.entity.Institution;

public class InstitutionQueryBuilder extends QueryBuilder {

    public InstitutionQueryBuilder() {
        this.setClasses(new Class<?>[] { Institution.class });
    }
}
