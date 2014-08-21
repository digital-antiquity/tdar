package org.tdar.search.query.builder;

import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.entity.Person;

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
        this.setClasses(new Class[] { Person.class });
    }

    public PersonQueryBuilder(Operator op) {
        this();
        setOperator(op);
    }
}
