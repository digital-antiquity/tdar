package org.tdar.search.query.part;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum PhraseFormatter {

    WILDCARD,
    ESCAPED,
    QUOTED,
    ESCAPE_QUOTED;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public String format(String value) {
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        switch (this) {
            case ESCAPED:
                return QueryParser.escape(value.trim());
            case WILDCARD:
                if (StringUtils.endsWith(value, "\"") && !StringUtils.endsWith(value, "\\\"")) {
                    logger.error("trying to wildcard a quoted element {}", value);
                }
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