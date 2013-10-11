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

    abstract String generateQueryString();

    abstract boolean isDescriptionVisible();

    abstract void setDescriptionVisible(boolean visible);

    abstract String getDescription();

    abstract String getDescriptionHtml();

    abstract boolean isEmpty();

    abstract Operator getOperator();

}
