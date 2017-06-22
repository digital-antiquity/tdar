package org.tdar.search.query.part.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.entity.Institution;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.search.query.part.StringAutocompletePart;

/**
 * Search for an Institution using an autocomplete (e.g. left match everything)
 * @author abrin
 *
 */
public class InstitutionAutocompleteQueryPart extends FieldQueryPart<Institution> {

    public InstitutionAutocompleteQueryPart() {
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(Operator.OR);
        List<String> names = new ArrayList<String>();
        List<String> noSpaces = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(getFieldValues())) {
            for (Institution inst : getFieldValues()) {
                String trim = StringUtils.trim(inst.getName());
                if (!trim.contains(" ")) {
                    noSpaces.add(trim);
                }
                names.add(trim);
            }
            FieldQueryPart<String> fqp = new FieldQueryPart<String>(QueryFieldNames.NAME, Operator.OR, names);
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            fqp.setBoost(3f);
            group.append(fqp);
        }
        group.append(new StringAutocompletePart(QueryFieldNames.NAME_AUTOCOMPLETE, names));

        // match ASU, but not "arizona state"
        if (!CollectionUtils.isEmpty(noSpaces)) {
            FieldQueryPart<String> acronym = new FieldQueryPart<String>(QueryFieldNames.ACRONYM, noSpaces);
            acronym.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            acronym.setOperator(Operator.OR);
            acronym.setBoost(7f);
            group.append(acronym);
        }
        if (CollectionUtils.isEmpty(names)) {
            return "";
        }
        return group.generateQueryString();
    }
}
