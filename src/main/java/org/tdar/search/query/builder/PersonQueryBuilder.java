package org.tdar.search.query.builder;

import org.apache.lucene.queryParser.QueryParser.Operator;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;

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
        this.setClasses(new Class[] { Person.class, TdarUser.class });
    }

    public PersonQueryBuilder(Operator op) {
        this();
        setOperator(op);
    }
}
