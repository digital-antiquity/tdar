package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.List;

import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRelationship;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.base.Dao;

@Component
public class ResourceRelationshipDao extends Dao.HibernateBase<ResourceRelationship> {

    public ResourceRelationshipDao() {
        super(ResourceRelationship.class);
    }

    public List<ResourceRelationship> findRelatedResources(Resource resource) {
        Query<ResourceRelationship> query = getNamedQuery(TdarNamedQueries.QUERY_RELATED_RESOURCES, ResourceRelationship.class);
        query.setParameter("statuses", Arrays.asList(Status.ACTIVE));
        query.setParameter("resourceId", resource.getId());
        return query.getResultList();
    }

}
