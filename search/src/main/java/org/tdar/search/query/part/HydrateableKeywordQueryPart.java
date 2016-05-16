package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

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

    private static final String LABEL_KEYWORD = "_label";
    private static final String LABEL = "_label_phrase";
    private boolean includeChildren = true;
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unchecked")
    public HydrateableKeywordQueryPart(KeywordType type, List<K> fieldValues_) {
        setOperator(Operator.OR);
        setActualClass((Class<K>) type.getKeywordClass());
        String name = null;
        switch (type) {
            case CULTURE_KEYWORD:
                name = QueryFieldNames.ACTIVE_CULTURE_KEYWORDS;
                break;
            case INVESTIGATION_TYPE:
                name = QueryFieldNames.ACTIVE_INVESTIGATION_TYPES;
                break;
            case GEOGRAPHIC_KEYWORD:
                name = QueryFieldNames.ACTIVE_GEOGRAPHIC_KEYWORDS;
                break;
            case MATERIAL_TYPE:
                name = QueryFieldNames.ACTIVE_MATERIAL_KEYWORDS;
                break;
            case OTHER_KEYWORD:
                name = QueryFieldNames.ACTIVE_OTHER_KEYWORDS;
                break;
            case SITE_NAME_KEYWORD:
                name = QueryFieldNames.ACTIVE_SITE_NAME_KEYWORDS;
                break;
            case SITE_TYPE_KEYWORD:
                name = QueryFieldNames.ACTIVE_SITE_TYPE_KEYWORDS;
                break;
            case TEMPORAL_KEYWORD:
                name = QueryFieldNames.ACTIVE_TEMPORAL_KEYWORDS;
                break;
        }
        setFieldName(name);
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
            if (PersistableUtils.isNotNullOrTransient(getFieldValues().get(i))) {
                ids.add(getFieldValues().get(i).getId());
            } else if (StringUtils.isNotBlank(getFieldValues().get(i).getLabel())) {
                labels.add(getFieldValues().get(i).getLabel());
            }
        }
        FieldQueryPart<String> labelPart = new FieldQueryPart<String>(getFieldName() + LABEL, getOperator(), labels);
        FieldQueryPart<String> labelKeyPart = new FieldQueryPart<String>(getFieldName() + LABEL_KEYWORD, getOperator(), labels);
        labelPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        FieldQueryPart<Long> idPart = new FieldQueryPart<Long>(getFieldName(), getOperator(), ids);
        labelKeyPart.setPhraseFormatters(PhraseFormatter.ESCAPE_QUOTED);
        QueryPartGroup field = new QueryPartGroup(getOperator(), idPart, labelPart, labelKeyPart);

        QueryPartGroup topLevel = new QueryPartGroup(Operator.AND, field);
        if (includeChildren) {
            topLevel.setOperator(Operator.OR);
            FieldQueryPart<Long> irIdPart = new FieldQueryPart<Long>(getFieldName(), getOperator(), ids);
            FieldQueryPart<String> irLabelPart = new FieldQueryPart<String>(getFieldName() + LABEL, getOperator(), labels);
            irLabelPart.setPhraseFormatters(PhraseFormatter.ESCAPED_EMBEDDED, PhraseFormatter.QUOTED);
            FieldQueryPart<String> irLabelKeyPart = new FieldQueryPart<String>(getFieldName() + LABEL_KEYWORD, getOperator(),
                    labels);
            irLabelKeyPart.setPhraseFormatters(PhraseFormatter.ESCAPED_EMBEDDED, PhraseFormatter.QUOTED);
            QueryPartGroup group = new QueryPartGroup(getOperator(), new FieldJoinQueryPart<>(QueryFieldNames.PROJECT_ID, QueryFieldNames.ID, irLabelPart), 
                    new FieldJoinQueryPart<>(QueryFieldNames.PROJECT_ID, QueryFieldNames.ID, irIdPart), 
                    new FieldJoinQueryPart<>(QueryFieldNames.PROJECT_ID, QueryFieldNames.ID, irLabelKeyPart));
            topLevel.append(group);
        }
        return topLevel.generateQueryString();
    }

    public String getDescriptionLabel(TextProvider provider) {
        return provider.getText("searchParameters." + getFieldName());
    }

    @Override
    public String getDescription(TextProvider provider) {
        String strValues = StringUtils.join(getFieldValues(), getDescriptionOperator(provider));
        if (StringUtils.isNotBlank(strValues)) {
            return String.format("%s: \"%s\"", getDescriptionLabel(provider), strValues);
        }
        return "";
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
