
package org.tdar.search.query.part;

import java.util.List;

import org.apache.lucene.queryParser.QueryParser.Operator;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public interface QueryGroup {
    public abstract void append(QueryPart q);
    
    public abstract Operator getOperator();

    public abstract void setOperator(Operator or);

    public abstract boolean isEmpty();
    
    public abstract List<QueryPart> getParts();

}
