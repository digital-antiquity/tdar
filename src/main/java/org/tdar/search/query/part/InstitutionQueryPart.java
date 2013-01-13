package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.entity.Institution;

public class InstitutionQueryPart extends FieldQueryPart<Institution> {

    public InstitutionQueryPart() {
    }

    @Override
    public String generateQueryString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ( ");
        StringBuilder sbauto = new StringBuilder();
        setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        for (int i = 0; i < getFieldValues().size(); i++) {
            appendPhrase(sbauto, i);
        }
        if (sbauto.length() > 0) {
            constructQueryPhrase(sbauto, "name_auto");
        }
        sb.append(sbauto).append(" OR ");

        List<String> names = new ArrayList<String>();
        boolean containsSpaces = false;
        if (CollectionUtils.isNotEmpty(getFieldValues())) {
            for (Institution inst : getFieldValues()) {
                names.add(inst.getName().trim());
                if (inst.getName().trim().contains(" ")) {
                    containsSpaces = true;
                }
            }
            FieldQueryPart<String> fqp = new FieldQueryPart<String>("name", Operator.OR, names.toArray(new String[0]));
            fqp.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            fqp.setBoost(4f);
            sb.append(" " + fqp.toString());
        }

        StringBuilder sbacro = new StringBuilder();
        //match ASU, but not "arizona state"
        if (!containsSpaces) {
            sb.append( " OR ");
            for (int i = 0; i < getFieldValues().size(); i++) {
                appendPhrase(sbacro, i);
            }
            if (sbauto.length() > 0) {
                constructQueryPhrase(sbacro, "acronym");
            }
        }
        sb.append(sbacro).append(" ) ");
        if (sbacro.length() == 0 && sbauto.length() == 0) {
            return "";
        }
        return sb.toString();
    }
}
