package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.query.QueryFieldNames;

import com.opensymphony.xwork2.TextProvider;

public class TitleQueryPart extends FieldQueryPart<String> {

    private static final float TITLE_BOOST = 6f;
    private String prefix = "";

    public TitleQueryPart() {
    }

    public TitleQueryPart(String... values) {
        add(values);
    }

    public TitleQueryPart(Collection<String> values, Operator operator) {
        add(values.toArray(new String[0]));
        setOperator(operator);
    }

    @Override
    public boolean isEmpty() {
        // we define this as empty if we have no values or if ALL values are blank.
        if (getFieldValues().isEmpty()) {
            return true;
        }
        for (String value : getFieldValues()) {
            if (StringUtils.isNotBlank(value)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean validate(String fieldValue) {
        return StringUtils.isNotBlank(fieldValue);
    }

    protected QueryPart<?> getQueryPart(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        QueryPartGroup group = new QueryPartGroup();
        group.setOperator(Operator.OR);
        FieldQueryPart<String> wordsInTitle = new FieldQueryPart<String>(getPrefix() + QueryFieldNames.NAME, value);
        if (value.contains(" ")) {
            wordsInTitle.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD, PhraseFormatter.QUOTED);
        } else {
            wordsInTitle.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
        }
        FieldQueryPart<String> wholeTitle = new FieldQueryPart<String>(getPrefix() + QueryFieldNames.NAME_AUTOCOMPLETE, value);
        wholeTitle.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED).setBoost(TITLE_BOOST);
        group.append(wholeTitle);
        group.append(wordsInTitle);
        return group;
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(getOperator());
        for (String title : getFieldValues()) {
            group.append(getQueryPart(title));
        }
        return group.generateQueryString();
    }

    @Override
    public void setLimit(String obj) {
        add(obj);
    }

    @Override
    public String getDescription(TextProvider provider) {
        List<String> vals = new ArrayList<>();
        vals.add(StringUtils.join(getFieldValues(), ";"));
        return provider.getText("titleQueryPart.description", vals);
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
