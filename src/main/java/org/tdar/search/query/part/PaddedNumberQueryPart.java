package org.tdar.search.query.part;

import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.search.index.TdarIndexNumberFormatter;

public class PaddedNumberQueryPart<C> extends FieldQueryPart<C> {

    public PaddedNumberQueryPart(String fieldName, C... fieldValues_) {
        this(fieldName, Arrays.asList(fieldValues_));
    }

    public PaddedNumberQueryPart(String fieldName, Collection<C> fieldValues_) {
        setFieldName(fieldName);
        setFieldValues(fieldValues_);
    }

    public PaddedNumberQueryPart(String fieldName, Operator oper, Collection<C> fieldValues_) {
        this(fieldName, fieldValues_);
        setOperator(oper);
    }

    public PaddedNumberQueryPart(String fieldName, Operator oper, C... fieldValues_) {
        this(fieldName, fieldValues_);
        setOperator(oper);
    }

    @Override
    protected String formatValueAsStringForQuery(int index) {
        C item = getFieldValues().get(index);
        if (item == null) {
            return "";
        }
        if (item instanceof Number) {
            return TdarIndexNumberFormatter.format((Number) item);
        } else {
            return item.toString();
        }
    }

}
