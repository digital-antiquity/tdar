package org.tdar.core.service.resource;

import java.util.List;

import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRelationship;

public interface ResourceRelationshipService {

    /**
     * Find ResourceRelationship objects that are related to the specified @link Resource
     * 
     * @param resource
     * @return
     */
    List<ResourceRelationship> findRelatedResources(Resource resource);

}