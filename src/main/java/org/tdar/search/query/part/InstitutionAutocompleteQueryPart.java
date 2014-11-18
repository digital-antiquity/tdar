package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.entity.Institution;

public class InstitutionAutocompleteQueryPart extends FieldQueryPart<Institution> {

    public InstitutionAutocompleteQueryPart() {
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(Operator.OR);
        group.append(new FieldQueryPart<Institution>("name_auto", getFieldValues()));
        List<String> names = new ArrayList<String>();
        boolean containsSpaces = false;
        if (CollectionUtils.isNotEmpty(getFieldValues())) {
            for (Institution inst : getFieldValues()) {
                names.add(StringUtils.trim(inst.getName()));
                if (inst.getName().trim().contains(" ")) {
                    containsSpaces = true;
                }
            }
            FieldQueryPart<String> fqp = new FieldQueryPart<String>("name", Operator.OR, names.toArray(new String[0]));
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            fqp.setBoost(3f);
            group.append(fqp);
        }

        // match ASU, but not "arizona state"
        FieldQueryPart<String> acronym = new FieldQueryPart<String>("acronym", names);
        acronym.setOperator(Operator.OR);
        acronym.setBoost(7f);
        group.append(acronym);
        if (CollectionUtils.isEmpty(names)) {
            return "";
        }
        return group.generateQueryString();
    }
}
