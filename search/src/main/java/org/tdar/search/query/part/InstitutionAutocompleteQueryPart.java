package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.search.query.QueryFieldNames;

public class InstitutionAutocompleteQueryPart extends FieldQueryPart<Institution> {

    public InstitutionAutocompleteQueryPart() {
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(Operator.OR);
        List<String> names = new ArrayList<String>();
        boolean containsSpaces = false;
        if (CollectionUtils.isNotEmpty(getFieldValues())) {
            for (Institution inst : getFieldValues()) {
                String trim = StringUtils.trim(inst.getName());
                if (trim.contains(" ")) {
                    containsSpaces =true;
                }
                names.add(trim);
            }
            FieldQueryPart<String> fqp = new FieldQueryPart<String>(QueryFieldNames.NAME, Operator.OR, names);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            fqp.setBoost(3f);
            group.append(fqp);
        }
        FieldQueryPart<Institution> name_auto = new FieldQueryPart<Institution>(QueryFieldNames.NAME_AUTOCOMPLETE, getFieldValues());
        if (containsSpaces) {
            name_auto.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD, PhraseFormatter.QUOTED);
        } else {
            name_auto.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
        }
        group.append(name_auto);

        // match ASU, but not "arizona state"
        FieldQueryPart<String> acronym = new FieldQueryPart<String>(QueryFieldNames.ACRONYM, names);
        acronym.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        acronym.setOperator(Operator.OR);
        acronym.setBoost(7f);
        group.append(acronym);
        if (CollectionUtils.isEmpty(names)) {
            return "";
        }
        return group.generateQueryString();
    }
}
