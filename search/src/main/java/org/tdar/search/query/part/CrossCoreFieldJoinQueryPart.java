package org.tdar.search.query.part;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.TextProvider;

/**
 * Allows the join of a lucene query across multiple indexes (cores)
 * 
 * @author abrin
 *
 * @param <T>
 */
public class CrossCoreFieldJoinQueryPart<T extends QueryPart<?>> implements QueryPart<T> {

    // http://comments.gmane.org/gmane.comp.jakarta.lucene.solr.user/95646

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());
    T part;
    private boolean descriptionVisible = false;
    private String outerFieldName;
    private String innerFieldName;
    private String coreName;

    public CrossCoreFieldJoinQueryPart(String innerFieldName, String outerFieldName, T part, String coreName) {
        this.innerFieldName = innerFieldName;
        this.outerFieldName = outerFieldName;
        this.part = part;
        this.coreName = coreName;
    }

    @Override
    public String generateQueryString() {
        if (part.isEmpty()) {
            return null;
        }
        // Localparams syntax can't be composed in the same way that we treat other queryparts, so we
        // utilize nested queries (i.e. the _query_ parameter) as a work around.
        // More info here: https://github.com/SolrNet/SolrNet/issues/143
        // Example of nested queries: https://lucidworks.com/post/nested-queries-in-solr/
        String qs = part.generateQueryString();
        // because our query is now a parameter value, we need to encode it
        qs = qs.replace("\\ ", "\\\\ ");
        String str = String.format("_query_:\"{!join fromIndex=%s from=%s to=%s}%s\"", coreName, outerFieldName, innerFieldName, qs);
        return str;
    }

    @Override
    public boolean isDescriptionVisible() {
        return descriptionVisible;
    }

    @Override
    public void setDescriptionVisible(boolean visible) {
        this.descriptionVisible = visible;
    }

    @Override
    public String getDescription(TextProvider provider) {
        return null;
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return part.isEmpty();
    }

    @Override
    public Operator getOperator() {
        return part.getOperator();
    }

    public String getCoreName() {
        return coreName;
    }

    public void setCoreName(String coreName) {
        this.coreName = coreName;
    }

}
