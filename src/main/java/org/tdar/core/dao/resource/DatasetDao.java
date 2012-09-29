package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.ReflectionService;

/**
 * $Id$ Provides DAO access to
 * persistent Datasets.
 * 
 * @author <a href='Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision$
 */
@Component
public class DatasetDao extends ResourceDao<Dataset> {

    public DatasetDao() {
        super(Dataset.class);
    }

    public boolean canLinkDataToOntology(Dataset dataset) {
        if (dataset == null)
            return false;
        Query query = getCurrentSession().getNamedQuery(QUERY_DATASET_CAN_LINK_TO_ONTOLOGY);
        query.setLong("datasetId", dataset.getId());
        return !query.list().isEmpty();
    }

    public long countResourcesForUserAccess(Person user) {
        if (user == null)
            return 0;
        Query query = getCurrentSession().getNamedQuery(QUERY_USER_GET_ALL_RESOURCES_COUNT);
        query.setLong("userId", user.getId());
        query.setParameterList("resourceTypes", Arrays.asList(ResourceType.values()));
        query.setParameterList("statuses", Status.values());
        query.setParameter("allStatuses", true);
        query.setParameter("effectivePermission", GeneralPermissions.MODIFY_RECORD.getEffectivePermissions() - 1);
        query.setParameter("allResourceTypes", true);
        query.setParameter("admin", false);
        return (Long) query.iterate().next();
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findResourceLinkedValues(Class<?> cls) {
        String name = ReflectionService.cleanupMethodName(cls.getSimpleName() + "s");
        if (Keyword.class.isAssignableFrom(cls)) {
            String prop = "label";
            Criteria createCriteria = getCriteria(Resource.class).setProjection(Projections.distinct(Projections.property("kwd." + prop)))
                    .add(Restrictions.eq("status", Status.ACTIVE)).createAlias(name, "kwd", Criteria.INNER_JOIN).addOrder(Order.asc("kwd." + prop));
            return createCriteria.list();
        }
        // if (Creator.class.isAssignableFrom(cls)) {
        // String prop = "properName";
        // Criteria createCriteria = getCriteria(ResourceCreator.class).setProjection(Projections.distinct(Projections.property("rc." + prop)))
        // .createAlias("creator", "rc")
        // .createCriteria("resource").add(Restrictions.eq("status", Status.ACTIVE)).addOrder(Order.asc("rc." + prop));
        // return createCriteria.list();
        //
        // }
        throw new TdarRecoverableRuntimeException("passed a class we didn't know what to do with");

    }

}
