package org.tdar.search.query.part;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.search.query.QueryFieldNames;

public class AutocompleteTitleQueryPart implements QueryPart<String> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final float TITLE_BOOST = 6f;

    private String title;

    public AutocompleteTitleQueryPart() {
    }

    public AutocompleteTitleQueryPart(String value) {
        this.title = value;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(title);
    }

    protected QueryPart<?> getQueryPart(String value) {
        if (StringUtils.isBlank(value))
            return null;
        QueryPartGroup titleGroup = new QueryPartGroup(Operator.OR);
        FieldQueryPart<String> autoPart = new FieldQueryPart<String>(QueryFieldNames.TITLE_AUTO, title).setBoost(TITLE_BOOST).setPhraseFormatters(
                PhraseFormatter.ESCAPE_QUOTED);
        autoPart.setOperator(Operator.AND);
        // FIXME: while allowed, I'm not sure it's helpful to include non-analyzed fields in a search, especially considering the fact that it will use a
        // default analyzer at search-time. arguments otherwise?
        FieldQueryPart<String> part = new FieldQueryPart<String>(QueryFieldNames.TITLE_SORT, value).setPhraseFormatters(PhraseFormatter.ESCAPED,
                PhraseFormatter.WILDCARD);
        FieldQueryPart<String> part2 = new FieldQueryPart<String>(QueryFieldNames.TITLE, value);
        if (value.length() > 2) {
            part2.setPhraseFormatters(PhraseFormatter.ESCAPED, PhraseFormatter.WILDCARD);
        }

        if (value.contains(" ")) {
            autoPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            part.setPhraseFormatters(PhraseFormatter.WILDCARD, PhraseFormatter.QUOTED);
            if (value.length() > 2) {
                part2.setPhraseFormatters(PhraseFormatter.WILDCARD, PhraseFormatter.QUOTED);
            }
        }
        titleGroup.append(autoPart);
        titleGroup.append(part.setBoost(4f));
        titleGroup.append(part2);
        logger.info(titleGroup.generateQueryString());
        return titleGroup;

    }

    @Override
    public String generateQueryString() {
        return this.getQueryPart(title).generateQueryString();
    }

    // public void setLimit(String obj) {
    // title = obj;
    // }

    @Override
    public String getDescription() {
        return "Title: " + title;
    }

    @Override
    public String getDescriptionHtml() {
        return StringEscapeUtils.escapeHtml(getDescription());
    }

    @Override
    public boolean isDescriptionVisible() {
        return false;
    }

    @Override
    public void setDescriptionVisible(boolean visible) {
        // TODO Auto-generated method stub
    }

    public Operator getOperator() {
        return Operator.AND;
    }
}
