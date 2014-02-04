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

    String generateQueryString();

    boolean isDescriptionVisible();

    void setDescriptionVisible(boolean visible);

    String getDescription();

    String getDescriptionHtml();

    boolean isEmpty();

    Operator getOperator();

}
