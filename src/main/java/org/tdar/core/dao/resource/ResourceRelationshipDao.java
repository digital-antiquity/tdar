package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRelationship;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;

@Component
public class ResourceRelationshipDao extends Dao.HibernateBase<ResourceRelationship> {

    public ResourceRelationshipDao() {
        super(ResourceRelationship.class);
    }

    @SuppressWarnings("unchecked")
    public List<ResourceRelationship> findRelatedResources(Resource resource) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_RELATED_RESOURCES);
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE));
        query.setParameter("resourceId", resource.getId());
        return query.list();
    }

}
