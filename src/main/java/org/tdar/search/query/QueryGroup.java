/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.search.query;

import org.apache.lucene.queryParser.QueryParser.Operator;

/**
 * @author Adam Brin
 *
 */
public interface QueryGroup {
    public abstract void append(QueryPart q);
    
    public abstract Operator getOperator();

    public abstract void setOperator(Operator or);

    public abstract boolean isEmpty();

}
