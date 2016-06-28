package org.tdar.search.query.builder;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.service.CoreNames;

public class InstitutionQueryBuilder extends QueryBuilder {

    public InstitutionQueryBuilder() {
    }

    public InstitutionQueryBuilder(Operator op) {
        this();
        setOperator(op);
    }

    @Override
    public String getCoreName() {
        return CoreNames.INSTITUTIONS;
    }
}
