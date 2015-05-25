package org.tdar.search.query.part;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.entity.Person;

import com.opensymphony.xwork2.TextProvider;

public class BookmarkQueryPart extends FieldQueryPart<Person> {

    public BookmarkQueryPart() {
        setAllowInvalid(true);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup(Operator.OR);
        for (Person person : getFieldValues()) {
            group.append(new FieldQueryPart<Long>("bookmarkedResource.person.id", person.getId()));
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
