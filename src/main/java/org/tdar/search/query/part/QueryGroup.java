package org.tdar.search.query.part;

import java.util.List;

import org.tdar.core.service.search.Operator;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public interface QueryGroup {
    void append(QueryPart<?> q);

    Operator getOperator();

    void setOperator(Operator or);

    boolean isEmpty();

    List<QueryPart<?>> getParts();

}
