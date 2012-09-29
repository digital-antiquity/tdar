package org.tdar.core.dao.entity;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.dao.Dao;

/**
 * $Id$
 * 
 * Provides DAO access for Person entities, including a variety of methods for
 * looking up a Person in tDAR.
 *
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class CreatorDao extends Dao.HibernateBase<Creator> {
    
    public CreatorDao() {
        super(Creator.class);
    }
    


}
