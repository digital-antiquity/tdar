package org.tdar.search.query.part;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.search.index.analyzer.SiteCodeExtractor;
import org.tdar.search.query.QueryFieldNames;

public class GeneralSearchResourceQueryPart extends GeneralSearchQueryPart {

    protected static final float TITLE_BOOST = 6f;
    protected static final float CREATOR_BOOST = 5f;
    protected static final float SITE_CODE_BOOST = 5f;
    protected static final float DESCRIPTION_BOOST = 4f;
    protected static final float PHRASE_BOOST = 3.2f;
    protected static final float ANY_FIELD_BOOST = 2f;
    protected static final float ID_BOOST = 10f;

    public GeneralSearchResourceQueryPart(List<String> list, Operator operator) {
        this(list);
        setOperator(operator);
    }

    public GeneralSearchResourceQueryPart(String... values) {
        add(values);
    }

    public GeneralSearchResourceQueryPart(Collection<String> values) {
        add(values.toArray(new String[0]));
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(getOperator());
        for (String value : getFieldValues()) {
            group.append(this.getQueryPart(value));
        }
        return group.generateQueryString();
    }

    @Override
    protected QueryPartGroup getQueryPart(String value) {
        boolean siteCodeSearch = false;
        if (SiteCodeExtractor.matches(value)) {
            siteCodeSearch = true;
            setUseProximity(false);
        }

        QueryPartGroup queryPart = super.getQueryPart(value);
        String cleanedQueryString = getCleanedQueryString(value);
        if (StringUtils.isBlank(cleanedQueryString)) {
            return queryPart;
        }

        FieldQueryPart<String> creatorPart = new FieldQueryPart<String>(QueryFieldNames.RESOURCE_CREATORS_PROPER_NAME, cleanedQueryString);
        FieldQueryPart<String> content = new FieldQueryPart<String>(QueryFieldNames.CONTENT, cleanedQueryString);

        FieldQueryPart<String> linkedContent = new FieldQueryPart<String>(QueryFieldNames.DATA_VALUE_PAIR, cleanedQueryString);

        if (cleanedQueryString.contains(" ") && isUseProximity()) {
            creatorPart.setProximity(2);
        }

        // if we're searching for something numeric, put the tDAR ID into the query string and weight it appropriately.
        if (StringUtils.isNumeric(value)) {
            FieldQueryPart<String> idPart = new FieldQueryPart<String>(QueryFieldNames.ID, cleanedQueryString);
            idPart.setBoost(ID_BOOST);
            queryPart.append(idPart);
        }

        queryPart.append(creatorPart.setBoost(CREATOR_BOOST));
        // we use the original value because we'd be esacping things we don't want to otherwise
        if (siteCodeSearch) {
            FieldQueryPart<String> siteCodePart = new FieldQueryPart<String>(QueryFieldNames.SITE_CODE, cleanedQueryString);
            queryPart.append(siteCodePart.setBoost(SITE_CODE_BOOST));
        }
        if (TdarConfiguration.getInstance().useSeparateContentsIndexForSearching()) {
            queryPart.append(new ContentQueryPart(cleanedQueryString));
        } else {
            queryPart.append(content);
        }

        if (TdarConfiguration.getInstance().useSeparateLinkedDataIndexForSearching()) {
            queryPart.append(new DataValueQueryPart(cleanedQueryString));
        } else {
            queryPart.append(linkedContent);
        }
        return queryPart;
    }

}
