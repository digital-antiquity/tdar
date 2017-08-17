package org.tdar.search.query.part.resource;

import java.util.Arrays;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.QueryPartGroup;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * Query for a coding sheet or ontology that's associated with a category term or id 
 * @author abrin
 *
 */
public class CategoryTermQueryPart extends FieldQueryPart<String> {

    private Long sortCategoryId;
    private String term;

    public CategoryTermQueryPart(String term, Long sortCategoryId) {
        add(term);
        this.setTerm(term);
        this.setSortCategoryId(sortCategoryId);
    }

    @Override
    public String generateQueryString() {
        // assumption: if sortCategoryId is set, we assume we are serving a coding-sheet/ontology autocomplete
        // FIXME: instead of guessing this way it may be better to break codingsheet/ontology autocomplete lookups to another action.
        QueryPartGroup valueGroup = new QueryPartGroup();
        if (StringUtils.isNotBlank(getTerm())) {
            valueGroup.append(new AutocompleteTitleQueryPart(getTerm()));
        }

        if (StringUtils.isNumeric(getTerm()) && StringUtils.isNotBlank(getTerm())) {
            valueGroup.append(new FieldQueryPart<String>(QueryFieldNames.ID, getTerm()));
            valueGroup.setOperator(Operator.OR);
        }

        if (PersistableUtils.isNotNullOrTransient(getSortCategoryId())) {
            // SHOULD PREFER THINGS THAT HAVE THAT CATEGORY ID
            FieldQueryPart<String> q2 = new FieldQueryPart<String>(QueryFieldNames.CATEGORY_ID, getSortCategoryId().toString().trim());
            q2.setBoost(2f);
            valueGroup.append(q2);
            valueGroup.setOperator(Operator.OR);

        }

        return valueGroup.generateQueryString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        return provider.getText("categoryTermQueryPart.description", Arrays.asList(getTerm()));
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Long getSortCategoryId() {
        return sortCategoryId;
    }

    public void setSortCategoryId(Long sortCategoryId) {
        this.sortCategoryId = sortCategoryId;
    }

}
