package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.Keyword;

/**
 * 
 * $Id$
 * 
 * {@link QueryPart} which builds a query string looking for
 * keywords associated with a project and it's resources.
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev: 1728 $
 * 
 */
public class HydrateableKeywordQueryPart<K extends Keyword> extends AbstractHydrateableQueryPart<K> {

    private String descriptionLabel = "Keyword";
    private boolean includeChildren = true;

    public HydrateableKeywordQueryPart(String fieldName, Class<K> originalClass, List<K> fieldValues_) {
        setOperator(Operator.OR);
        setActualClass(originalClass);
        setFieldName(fieldName);
        setFieldValues(fieldValues_);
    }

    @Override
    public String generateQueryString() {
        List<String> labels = new ArrayList<String>();
        List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < getFieldValues().size(); i++) {
            if (getFieldValues().get(i) == null) {
                continue;
            }
            if (Persistable.Base.isNotNullOrTransient(getFieldValues().get(i))) {
                ids.add(getFieldValues().get(i).getId());
            } else if (StringUtils.isNotBlank(getFieldValues().get(i).getLabel())) {
                labels.add(getFieldValues().get(i).getLabel());
            }
        }
        FieldQueryPart<String> labelPart = new FieldQueryPart<String>(getFieldName() + ".label", getOperator(), labels);
        FieldQueryPart<String> labelKeyPart = new FieldQueryPart<String>(getFieldName() + ".labelKeyword", getOperator(), labels);
        labelPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        FieldQueryPart<Long> idPart = new FieldQueryPart<Long>(getFieldName() + ".id", getOperator(), ids);
        labelKeyPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        QueryPartGroup field = new QueryPartGroup(getOperator(), idPart, labelPart, labelKeyPart);

        QueryPartGroup topLevel = new QueryPartGroup(Operator.AND, field);
        if (includeChildren) {
            topLevel.setOperator(Operator.OR);
            FieldQueryPart<Long> irIdPart = new FieldQueryPart<Long>("informationResources." + getFieldName() + ".id", getOperator(), ids);
            FieldQueryPart<String> irLabelPart = new FieldQueryPart<String>("informationResources." + getFieldName() + ".label", getOperator(), labels);
            irLabelPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            FieldQueryPart<String> irLabelKeyPart = new FieldQueryPart<String>("informationResources." + getFieldName() + ".labelKeyword", getOperator(),
                    labels);
            irLabelKeyPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            QueryPartGroup group = new QueryPartGroup(getOperator(), irLabelPart, irIdPart, irLabelKeyPart);
            topLevel.append(group);
        }
        return topLevel.generateQueryString();
    }

    public String getDescriptionLabel() {
        return descriptionLabel;
    }

    public void setDescriptionLabel(String label) {
        descriptionLabel = label;
    }

    @Override
    public String getDescription() {
        String strValues = StringUtils.join(getFieldValues(), getDescriptionOperator());
        return String.format("%s: \"%s\"", descriptionLabel, strValues);
    }

    @Override
    public String getDescriptionHtml() {
        return StringEscapeUtils.escapeHtml4(getDescription());
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }

    public void setIncludeChildren(boolean includeChildren) {
        this.includeChildren = includeChildren;
    }

}
