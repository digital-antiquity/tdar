package org.tdar.search.query.part.entity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.entity.Institution;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.PhraseFormatter;
import org.tdar.search.query.part.QueryPart;
import org.tdar.search.query.part.QueryPartGroup;

/**
 * Search for an Institution, but don't left-match everything as you would with an autocomplete
 * @author abrin
 *
 */
public class InstitutionQueryPart extends FieldQueryPart<Institution> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private boolean useProximity = true;
    protected static final float NAME_BOOST = 6f;
    protected static final float ANY_FIELD_BOOST = 2f;

    public InstitutionQueryPart(Institution institution) {
        add(institution);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(getOperator());
        for (Institution value : getFieldValues()) {
            group.append(this.getQueryPart(value));
        }
        return group.generateQueryString();
    }

    protected QueryPartGroup getQueryPart(Institution value) {
        String cleanedQueryString = getCleanedQueryString(value.getName());

        QueryPartGroup primary = new QueryPartGroup(Operator.OR);

        logger.trace(cleanedQueryString);

        FieldQueryPart<String> titlePart = new FieldQueryPart<String>(QueryFieldNames.NAME_TOKEN, cleanedQueryString);

        List<String> fields = new ArrayList<String>();
        for (String txt : StringUtils.split(cleanedQueryString)) {
            if (!ArrayUtils.contains(QueryPart.LUCENE_RESERVED_WORDS, txt)) {
                fields.add(txt);
            }
        }

        FieldQueryPart<String> allFieldsAsPart = new FieldQueryPart<String>(QueryFieldNames.NAME_TOKEN, fields).setBoost(ANY_FIELD_BOOST);
        allFieldsAsPart.setOperator(Operator.AND);
        allFieldsAsPart.setPhraseFormatters(PhraseFormatter.ESCAPED);

        if (cleanedQueryString.contains(" ")) {
//            titlePart = new FieldQueryPart<String>(QueryFieldNames.NAME_PHRASE, cleanedQueryString);
            // FIXME: magic words
            if (useProximity) {
                titlePart.setProximity(3);
            }
        }

        primary.append(titlePart.setBoost(NAME_BOOST));
        primary.append(allFieldsAsPart);

        return primary;
    }
}
