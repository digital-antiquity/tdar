package org.tdar.search.query.part.resource;

import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.QueryPartGroup;

import com.opensymphony.xwork2.TextProvider;

/**
 * Search for resources in a project or the project itself (by id).
 * 
 * @author abrin
 *
 */
public class ProjectIdLookupQueryPart extends FieldQueryPart<Long> {

    public ProjectIdLookupQueryPart(Long projectId) {
        add(projectId);
    }

    @Override
    public String generateQueryString() {
        QueryPartGroup group = new QueryPartGroup();
        group.setOperator(Operator.OR);
        for (Long value : getFieldValues()) {
            group.append(new FieldQueryPart<Long>(QueryFieldNames.PROJECT_ID, value));
            group.append(new FieldQueryPart<Long>(QueryFieldNames.ID, value));
        }
        return group.generateQueryString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        return provider.getText("projectIdLookupQueryPart.description", Arrays.asList(StringUtils.join(getFieldValues(), ",")));
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }
}
