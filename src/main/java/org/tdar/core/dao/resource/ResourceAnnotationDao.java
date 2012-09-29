package org.tdar.core.dao.resource;

import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.ResourceAnnotation;
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
public class ResourceAnnotationDao extends Dao.HibernateBase<ResourceAnnotation> {

    public ResourceAnnotationDao() {
        super(ResourceAnnotation.class);
    }

    public String getDefaultOrderingProperty() {
        return "timestamp";
    }

}
