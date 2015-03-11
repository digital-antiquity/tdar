package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.service.search.Operator;

public class InstitutionAutocompleteQueryPart extends FieldQueryPart<Institution> {

    public InstitutionAutocompleteQueryPart() {
    }

    @Override
    public Query generateQuery(QueryBuilder builder) {
        QueryPartGroup group = generateRawQuery();
        if (group == null) {
            return null;
        }
        return group.generateQuery(builder);
    }
    @Override
    public String generateQueryString() {
        QueryPartGroup group = generateRawQuery();
        if (group == null) {
            return "";
        }
        return group.generateQueryString();
    }

    private QueryPartGroup generateRawQuery() {
        QueryPartGroup group = new QueryPartGroup(Operator.OR);
        List<String> names = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(getFieldValues())) {
            for (Institution inst : getFieldValues()) {
                names.add(StringUtils.trim(inst.getName()));
            }
            FieldQueryPart<String> fqp = new FieldQueryPart<String>("name", Operator.OR, names);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            fqp.setBoost(3f);
            group.append(fqp);
        }
        FieldQueryPart<Institution> name_auto = new FieldQueryPart<Institution>("name_auto", getFieldValues());
        group.append(name_auto);
        name_auto.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);

        // match ASU, but not "arizona state"
        FieldQueryPart<String> acronym = new FieldQueryPart<String>("acronym", names);
        acronym.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        acronym.setOperator(Operator.OR);
        acronym.setBoost(7f);
        group.append(acronym);
        if (CollectionUtils.isEmpty(names)) {
            return null;
        }
        return group;
    }
}
