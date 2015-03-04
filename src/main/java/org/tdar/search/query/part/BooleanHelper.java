package org.tdar.search.query.part;

import java.util.Collection;

import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.tdar.core.service.search.Operator;

public class BooleanHelper {

    public static Query generateQuery(QueryBuilder builder, Collection<QueryPart<?>> parts, Operator operator) {
        BooleanJunction<?> bq = builder.bool();
        boolean valid = false;
        for (QueryPart<?> part : parts) {
            Query q = part.generateQuery(builder);
            if (q == null) {
                continue;
            }
            valid = true;
            switch (operator) {
                case AND:
                    bq = bq.must(q);
                case OR:
                    bq = bq.should(q);
            }
        }
        if (!valid) {
            return null;
        }
        return bq.createQuery();
    }

}
