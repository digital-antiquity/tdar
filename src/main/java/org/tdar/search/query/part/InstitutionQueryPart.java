package org.tdar.search.query.part;

import org.tdar.core.bean.entity.Institution;

public class InstitutionQueryPart extends FieldQueryPart<Institution> {

    public InstitutionQueryPart() {
    }

    @Override
    public String generateQueryString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ( ");
        StringBuilder sbauto = new StringBuilder();
        setPhraseFormatters(PhraseFormatter.ESCAPED);
        for (int i = 0; i < getFieldValues().size(); i++) {
            appendPhrase(sbauto, i);
        }
        if (sbauto.length() > 0) {
            constructQueryPhrase(sbauto, "name_auto");
        }
        sb.append(sbauto).append(" OR ");

        StringBuilder sbacro = new StringBuilder();
        for (int i = 0; i < getFieldValues().size(); i++) {
            appendPhrase(sbacro, i);
        }
        if (sbauto.length() > 0) {
            constructQueryPhrase(sbacro, "acronym");
        }
        sb.append(sbacro).append(" ) ");
        if (sbacro.length() == 0 && sbauto.length() == 0) {
            return "";
        }
        return sb.toString();
    }
}
