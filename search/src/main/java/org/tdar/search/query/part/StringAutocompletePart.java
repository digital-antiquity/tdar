package org.tdar.search.query.part;

import java.util.Collection;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.query.QueryFieldNames;

public class StringAutocompletePart extends FieldQueryPart<String> {

    public StringAutocompletePart(String name, Collection<String> list) {
        this.setFieldName(name);
        this.getFieldValues().addAll(list);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup primary = new QueryPartGroup(Operator.OR);
        for (String term : getFieldValues()) {
            FieldQueryPart<String> fqp = new FieldQueryPart<String>(QueryFieldNames.NAME, Operator.OR, term);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            fqp.setBoost(3f);
            primary.append(fqp);
            String term_ = term;//SearchUtils.prepareAutoCompleteField(term);
            FieldQueryPart<String> fqp2 = new FieldQueryPart<String>(getFieldName(), term_);
            fqp2.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            FieldQueryPart<String> fqp3 = new FieldQueryPart<String>(getFieldName(), term_);
            fqp3.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            primary.append(fqp2);
            primary.append(fqp3);
        }
        return primary.generateQueryString();
    }

}
