package org.tdar.core.dao.resource;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.dao.Dao;

/**
 * $Id$ 
 * <p>
 * Provides hibernate DAO access for ResourceNotes.
 *
 * @author Adam Brin
 * @version $Revision$
 */
@Component
public class ResourceAnnotationKeyDao extends Dao.HibernateBase<ResourceAnnotationKey> {

    public ResourceAnnotationKeyDao() {
        super(ResourceAnnotationKey.class);
    }

    public String getDefaultOrderingProperty() {
        return "timestamp";
    }

}
