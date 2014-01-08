package org.tdar.search.query.part;

import org.apache.lucene.queryParser.QueryParser.Operator;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public interface QueryPart<T> {

    String[] LUCENE_RESERVED_WORDS = {"AND", "OR", "NOT","TO"};

    public abstract String generateQueryString();

    public abstract boolean isDescriptionVisible();

    public abstract void setDescriptionVisible(boolean visible);

    public abstract String getDescription();

    public abstract String getDescriptionHtml();

    public abstract boolean isEmpty();
    
    public abstract Operator getOperator();
    
}
