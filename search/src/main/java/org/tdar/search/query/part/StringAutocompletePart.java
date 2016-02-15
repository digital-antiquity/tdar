package org.tdar.search.query.part;

import java.util.List;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.service.SearchUtils;

public class StringAutocompletePart extends FieldQueryPart<String> {

    public StringAutocompletePart(String name, List<String> list) {
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
            String term_ = SearchUtils.prepareAutoCompleteField(term);
            FieldQueryPart<String> fqp2 = new FieldQueryPart<String>(QueryFieldNames.NAME_AUTOCOMPLETE, term_);
            fqp2.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            FieldQueryPart<String> fqp3 = new FieldQueryPart<String>(QueryFieldNames.NAME_AUTOCOMPLETE, term_);
            fqp3.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
            primary.append(fqp2);
            primary.append(fqp3);
        }
        return primary.generateQueryString();
    }

}
