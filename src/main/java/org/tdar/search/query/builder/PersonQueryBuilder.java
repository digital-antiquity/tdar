package org.tdar.search.query.builder;

import java.util.Collections;

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
//		setOmitContainedLabels(Collections.EMPTY_LIST);
	}

}
