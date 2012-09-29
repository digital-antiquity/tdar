package org.tdar.search.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.tdar.core.bean.keyword.Keyword;

/**
 * 
 * $Id: QueryPart.java 1728 2011-03-10 03:51:29Z abrin $
 * 
 * {@link QueryPart} which builds a query string looking for
 * keywords associated with a project and it's resources.
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev: 1728 $
 * 
 */
public class KeywordQueryPart implements QueryPart {

    private List<String> terms;
    private String keywordType;

    public KeywordQueryPart(String keywordType) {
        terms = new ArrayList<String>();
        this.keywordType = keywordType;
    }

    public KeywordQueryPart(String keywordType, String term) {
        terms = new ArrayList<String>(Arrays.asList(term));
        this.keywordType = keywordType;
    }

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

    @Override
    public String generateQueryString() {
        StringBuilder q = new StringBuilder();
        for (String term : terms) {
            if (StringUtils.isEmpty(term))
                continue;
            if (q.length() > 1)
                q.append(" OR ");
            q.append("(").append(keywordType).append(".label:(");
            q.append("\"").append(term).append("\"");
            q.append(") OR informationResources.").append(keywordType).append(".label:(");
            q.append("\"").append(term).append("\"");
            q.append("))");
        }
        return q.toString();
    }

    public void setKeywordType(String keywordType) {
        this.keywordType = keywordType;
    }

    public String getKeywordType() {
        return keywordType;
    }

}
