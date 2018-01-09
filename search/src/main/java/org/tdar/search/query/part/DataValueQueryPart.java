package org.tdar.search.query.part;

import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;

/*
 * Looks in the linked-data value index for vals (e..g key-value-pairs froma Mimbres record
 */
public class DataValueQueryPart extends FieldQueryPart<String> {

    public DataValueQueryPart() {
    }

    public DataValueQueryPart(String term) {
        getFieldValues().add(term);
    }

    public DataValueQueryPart(String text, Operator operator, List<String> contents) {
        super(QueryFieldNames.VALUE, text, operator, contents);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup subq = new QueryPartGroup(Operator.OR);
        FieldQueryPart<String> content = new FieldQueryPart<String>(QueryFieldNames.VALUE, getFieldValues());
        content.setPhraseFormatters(PhraseFormatter.ESCAPED_EMBEDDED);
        subq.append(content);
        FieldQueryPart<String> content2 = new FieldQueryPart<String>(QueryFieldNames.VALUE_PHRASE, getFieldValues());
        content2.setPhraseFormatters(PhraseFormatter.ESCAPED_EMBEDDED);
        subq.append(content2);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        CrossCoreFieldJoinQueryPart join = new CrossCoreFieldJoinQueryPart(QueryFieldNames.ID, QueryFieldNames.ID, subq, LookupSource.DATA.getCoreName());
        return join.generateQueryString();
    }
}
