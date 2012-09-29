package org.tdar.utils.db;


import org.hibernate.mapping.Map;

import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * $Id$
 * 
 * borrowed from http://constc.blogspot.com/2008/03/xstream-with-hibernate.html
 * 
 * @author Adam Brin
 * @version $Revision$
 * 
 */
public class HibernateMapConverter extends MapConverter {

	public HibernateMapConverter(Mapper mapper) {
        super(mapper);
    }

	@SuppressWarnings("rawtypes")
    public boolean canConvert(Class type) {
        return super.canConvert(type) || type == Map.class; 
    }
}
