package org.tdar.utils.db;

import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.mapping.List;
import org.hibernate.mapping.Set;

import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * $Id$
 * borrowed from http://constc.blogspot.com/2008/03/xstream-with-hibernate.html
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public class HibernateCollectionConverter extends CollectionConverter {
    public HibernateCollectionConverter(Mapper mapper) {
        super(mapper);
    }

    @SuppressWarnings("rawtypes")
    public boolean canConvert(Class type) {
        return super.canConvert(type) || type == List.class || type == Set.class ||
                type == PersistentSet.class || type == PersistentBag.class;
    }
}