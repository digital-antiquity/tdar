package org.tdar.core.dao.entity;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.dao.Dao;

/**
 * $Id$
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class ResourceCreatorDao extends Dao.HibernateBase<ResourceCreator> {
    
    public ResourceCreatorDao() {
        super(ResourceCreator.class);
    }

}
