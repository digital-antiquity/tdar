/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.dao;

import org.tdar.core.bean.resource.Resource;

/**
 * @author Adam Brin
 *  This class should not exist once Hibernate properly supports the @NamedNativeQueries
 *  annotations per JPA2 
 */
public class NamedNativeQueries {

	public static String incrementAccessCount(Resource r) {
		return "update resource set access_counter=access_counter+1 where id="+r.getId();
	}
	
}
