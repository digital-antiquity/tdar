package org.tdar.core.service.resource;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRelationship;
import org.tdar.core.dao.resource.ResourceRelationshipDao;
import org.tdar.core.service.ServiceInterface;

/**
 * Supporting service for dealing with @link ResourceRelationship entities.
 * 
 * @author abrin
 * 
 */
@Service
public class ResourceRelationshipService extends ServiceInterface.TypedDaoBase<ResourceRelationship, ResourceRelationshipDao> {

    /**
     * Find ResourceRelationship objects that are related to the specified @link Resource
     * 
     * @param resource
     * @return
     */
    @Transactional
    public List<ResourceRelationship> findRelatedResources(Resource resource) {
        return getDao().findRelatedResources(resource);
    }
}
