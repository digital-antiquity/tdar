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
    abstract void append(QueryPart<?> q);

    abstract Operator getOperator();

    abstract void setOperator(Operator or);

    abstract boolean isEmpty();

    abstract List<QueryPart<?>> getParts();

}
