package org.tdar.search.query.part;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;

public enum PhraseFormatter {
    WILDCARD,
    ESCAPED,
    QUOTED,
    ESCAPE_QUOTED;

    public String format(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        switch (this) {
            case ESCAPED:
                return QueryParser.escape(value.trim());
            case WILDCARD:
                return String.format("%s*", value);
            case QUOTED:
                return String.format("\"%s\"", value);
            case ESCAPE_QUOTED:
                return QUOTED.format(ESCAPED.format(value));
            default:
                return value;
        }
    }
}