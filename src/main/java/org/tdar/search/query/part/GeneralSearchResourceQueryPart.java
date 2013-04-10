package org.tdar.search.query.part;

import java.util.Collection;

import org.tdar.search.query.QueryFieldNames;

public class GeneralSearchResourceQueryPart extends GeneralSearchQueryPart {

    protected static final float TITLE_BOOST = 6f;
    protected static final float CREATOR_BOOST = 5f;
    protected static final float DESCRIPTION_BOOST = 4f;
    protected static final float PHRASE_BOOST = 3.2f;
    protected static final float ANY_FIELD_BOOST = 2f;

    public GeneralSearchResourceQueryPart() {
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
        QueryPartGroup queryPart = super.getQueryPart(value);
        String cleanedQueryString = getCleanedQueryString(value);

        FieldQueryPart<String> creatorPart = new FieldQueryPart<String>(QueryFieldNames.RESOURCE_CREATORS_PROPER_NAME, cleanedQueryString);
        FieldQueryPart<String> content = new FieldQueryPart<String>(QueryFieldNames.CONTENT, cleanedQueryString);
        FieldQueryPart<String> linkedContent = new FieldQueryPart<String>(QueryFieldNames.DATA_VALUE_PAIR, cleanedQueryString);
        
        if (cleanedQueryString.contains(" ")) {
            creatorPart.setProximity(2);
        }
        queryPart.append(creatorPart.setBoost(CREATOR_BOOST));

        queryPart.append(content);
        queryPart.append(linkedContent);
        return queryPart;
    }

}
