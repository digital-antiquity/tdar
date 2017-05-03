package org.tdar.search.query.part;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.TextProvider;

public class FieldJoinQueryPart<T extends QueryPart<?>> implements QueryPart<T> {

    //http://comments.gmane.org/gmane.comp.jakarta.lucene.solr.user/95646
    
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(getClass());
    T part;
    private boolean descriptionVisible = false;
    private String outerFieldName;
    private String innerFieldName;
    
    public FieldJoinQueryPart(String innerFieldName, String outerFieldName, T subq) {
        this.innerFieldName = innerFieldName;
        this.outerFieldName = outerFieldName;
        this.part = subq;
    }
    
    @Override
    public String generateQueryString() {
        if (part.isEmpty()) {
            return null;
        }
        String str = String.format("{!join from=%s to=%s v='%s'}", innerFieldName , outerFieldName , part.generateQueryString());
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

}
