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
        List<String> names = new ArrayList<String>();
        boolean containsSpaces = false;
        if (CollectionUtils.isNotEmpty(getFieldValues())) {
            for (Institution inst : getFieldValues()) {
                names.add(StringUtils.trim(inst.getName()));
                if (inst.getName().trim().contains(" ")) {
                    containsSpaces = true;
                }
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
            return "";
        }
        return group.generateQueryString();
    }
}
