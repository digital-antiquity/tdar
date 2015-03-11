package org.tdar.search.query.builder;

import org.tdar.core.bean.entity.Institution;
import org.tdar.core.service.search.Operator;

public class InstitutionQueryBuilder extends QueryBuilder {

    public InstitutionQueryBuilder() {
        this.setClasses(new Class<?>[] { Institution.class });
    }

    public InstitutionQueryBuilder(Operator op) {
        this();
        setOperator(op);
    }
}
