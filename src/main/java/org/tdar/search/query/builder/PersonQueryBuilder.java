package org.tdar.search.query.builder;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.service.search.Operator;

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
