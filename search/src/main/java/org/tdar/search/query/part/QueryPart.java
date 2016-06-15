package org.tdar.search.query.part;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;

import com.opensymphony.xwork2.TextProvider;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public interface QueryPart<T> {

    String[] LUCENE_RESERVED_WORDS = { "AND", "OR", "NOT", "TO" };

    String generateQueryString();

    boolean isDescriptionVisible();

    void setDescriptionVisible(boolean visible);

    String getDescription(TextProvider provider);

    String getDescriptionHtml(TextProvider provider);

    boolean isEmpty();

    Operator getOperator();

}
