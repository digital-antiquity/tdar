package org.tdar.search.query.part.resource;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.QueryPartGroup;

import com.opensymphony.xwork2.TextProvider;

/**
 * Find resources that are bookmarked by a person
 * @author abrin
 *
 */
public class BookmarkQueryPart extends FieldQueryPart<TdarUser> {

    public BookmarkQueryPart() {
        setAllowInvalid(true);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(Operator.OR);
        for (TdarUser person : getFieldValues()) {
            group.append(new FieldQueryPart<Long>(QueryFieldNames.BOOKMARKED_RESOURCE_PERSON_ID, person.getId()));
        }
        return group.generateQueryString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        return provider.getText("bookmarkQueryPart.bookmarked");
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }
}
