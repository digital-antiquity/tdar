package org.tdar.search.query;

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

}
