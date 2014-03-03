package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.TextProvider;

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

    private static final String ID = ".id";
    private static final String LABEL_KEYWORD = ".labelKeyword";
    private static final String LABEL = ".label";
    private static final String INFORMATION_RESOURCES = "informationResources.";
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
        FieldQueryPart<String> labelPart = new FieldQueryPart<String>(getFieldName() + LABEL, getOperator(), labels);
        FieldQueryPart<String> labelKeyPart = new FieldQueryPart<String>(getFieldName() + LABEL_KEYWORD, getOperator(), labels);
        labelPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        FieldQueryPart<Long> idPart = new FieldQueryPart<Long>(getFieldName() + ID, getOperator(), ids);
        labelKeyPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        QueryPartGroup field = new QueryPartGroup(getOperator(), idPart, labelPart, labelKeyPart);

        QueryPartGroup topLevel = new QueryPartGroup(Operator.AND, field);
        if (includeChildren) {
            topLevel.setOperator(Operator.OR);
            FieldQueryPart<Long> irIdPart = new FieldQueryPart<Long>(INFORMATION_RESOURCES + getFieldName() + ID, getOperator(), ids);
            FieldQueryPart<String> irLabelPart = new FieldQueryPart<String>(INFORMATION_RESOURCES + getFieldName() + LABEL, getOperator(), labels);
            irLabelPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            FieldQueryPart<String> irLabelKeyPart = new FieldQueryPart<String>(INFORMATION_RESOURCES + getFieldName() + LABEL_KEYWORD, getOperator(),
                    labels);
            irLabelKeyPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
            QueryPartGroup group = new QueryPartGroup(getOperator(), irLabelPart, irIdPart, irLabelKeyPart);
            topLevel.append(group);
        }
        return topLevel.generateQueryString();
    }

    public String getDescriptionLabel(TextProvider provider) {
        return provider.getText("keywordQueryPart.label");
    }

    @Override
    public String getDescription(TextProvider provider) {
        String strValues = StringUtils.join(getFieldValues(), getDescriptionOperator(provider));
        return String.format("%s: \"%s\"", getDescriptionLabel(provider), strValues);
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }

    public void setIncludeChildren(boolean includeChildren) {
        this.includeChildren = includeChildren;
    }

}
