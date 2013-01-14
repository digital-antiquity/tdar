package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Deprecated
public class KeywordQueryPart implements QueryPart<Keyword> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private List<String> terms;
    private boolean includeChildren = true;
    
    //for use when generating description
    private String descriptionLabel = "Keyword";
    
    private String keywordType;
    
    public KeywordQueryPart() {
        this("unknown");
    }

    public void setFieldName(String field) {
        this.keywordType = field;
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(terms);
    }
    public void setLimit(Keyword obj) {
        terms = new ArrayList<String>(Arrays.asList((String)obj.getLabel()));
    }

    public KeywordQueryPart(String keywordType) {
        terms = new ArrayList<String>();
        this.keywordType = keywordType;
    }

    public KeywordQueryPart(String keywordType, String term) {
        terms = new ArrayList<String>(Arrays.asList(term));
        this.keywordType = keywordType;
    }
    
    //FIXME: add signature that accepts collection of keywords,   use a static Map<Class, string> to lookup keywordType

    @SuppressWarnings("rawtypes")
    // There's no way in the controller signature to differentiate between types of collections
    // so we're going raw
    public KeywordQueryPart(String keywordType, Collection terms_) {
        terms = new ArrayList<String>();
        for (Object term : terms_) {
            if (term == null)
                continue;

            if (term instanceof Keyword) {
                Keyword kwd = (Keyword) term;
                terms.add(kwd.getLabel());
            } else {
                terms.add(term.toString());
            }
        }
        this.keywordType = keywordType;
    }

    public List<String> getTerm() {
        return terms;
    }

    public void addTerm(String Term) {
        terms.add(Term);
    }
    
    public void addTerms(Collection<? extends Keyword> keywords) {
        for(Keyword keyword : keywords) {
            terms.add(keyword.getLabel());
        }
    }

    public void addTermsById(Collection<Long> ids) {
        for (Long id : ids) {
            terms.add(Long.toString(id));
        }
    }

    @Override
    public String generateQueryString() {
        StringBuilder q = new StringBuilder();
        for (String term : terms) {
            if (StringUtils.isEmpty(term))
                continue;
            if (q.length() > 1)
                q.append(" OR ");
            
            String field = "label";
            if (StringUtils.isNumeric(term)) {
                field = "id";
            }
            //append group of two terms with explicit OR (term1 corresponds to direct list, term2 corresponds to keyword list in children informationResources)
            q.append(String.format("(%1$s.%2$s:(\"%3$s\")",keywordType, field, term));
            if (includeChildren) {
                q.append(String.format(" OR informationResources.%1$s.%2$s:(\"%3$s\"))",keywordType, field, term));
            }
        }
        return q.toString();
    }

    public void setKeywordType(String keywordType) {
        this.keywordType = keywordType;
    }

    public String getKeywordType() {
        return keywordType;
    }
    
    public String getDescriptionLabel() {
        return descriptionLabel;
    }
    
    public void setDescriptionLabel(String label) {
        descriptionLabel = label;
    }

    @Override
    public String getDescription() {
        String strValues = StringUtils.join(terms, ", ");
        return String.format("%s: %s", descriptionLabel, strValues);
    }

    @Override
    public String getDescriptionHtml() {
        return StringEscapeUtils.escapeHtml4(getDescription());
    }

    @Override
    public boolean isDescriptionVisible() {
        return false;
    }

    @Override
    public void setDescriptionVisible(boolean visible) {
        
    }

    public boolean isIncludeChildren() {
        return includeChildren;
    }

    public void setIncludeChildren(boolean includeChildren) {
        this.includeChildren = includeChildren;
    }

}
