package org.tdar.search.query.builder;

import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.tdar.search.service.CoreNames;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class PersonQueryBuilder extends QueryBuilder {

    public PersonQueryBuilder() {
    }

    public PersonQueryBuilder(Operator op) {
        this();
        setOperator(op);
    }

    @Override
    public String getCoreName() {
        return CoreNames.PEOPLE;
    }

}
