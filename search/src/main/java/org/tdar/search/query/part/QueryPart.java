package org.tdar.search.query.part;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;

import com.opensymphony.xwork2.TextProvider;

/**
 * 
 * $Id$
 * Interface for managing query parts
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public interface QueryPart<T> {

    static final String[] LUCENE_RESERVED_WORDS = { "AND", "OR", "NOT", "TO" };

    /**
     * Generate and return the Lucene/SOLR query String
     * 
     * @return
     */
    String generateQueryString();

    /**
     * Should the "text" of the description be shown to the user (for internal components e.g. rights, we might hide these)
     * 
     * @return
     */
    boolean isDescriptionVisible();

    void setDescriptionVisible(boolean visible);

    /**
     * Get the Text description
     * 
     * @param provider
     * @return
     */
    String getDescription(TextProvider provider);

    /**
     * Get the Text description escaped for HTML output
     * 
     * @param provider
     * @return
     */
    String getDescriptionHtml(TextProvider provider);

    /**
     * Is the query part or query part group empty() i.e. has no values
     * 
     * @return
     */
    boolean isEmpty();

    /**
     * AND or OR
     * 
     * @return
     */
    Operator getOperator();

}
