package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.util.HomepageGeographicKeywordCache;
import org.tdar.core.bean.util.HomepageResourceCountCache;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.NamedNativeQueries;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;

/**
 * $Id$
 * 
 * Base class for resource DAOs providing basic query functionalities for
 * Resource metadata.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev: 494$
 * @param <E>
 */
public abstract class ResourceDao<E extends Resource> extends Dao.HibernateBase<E> {
    @Autowired
    private AuthenticationAndAuthorizationService authenticationService;

    public ResourceDao(Class<E> resourceClass) {
        super(resourceClass);
    }

    public List<E> findByTitle(final String title) {
        DetachedCriteria criteria = getOrderedDetachedCriteria();
        criteria.add(Restrictions.eq("title", title));
        return findByCriteria(criteria);
    }

    /**
     * FIXME: replace with HQL if possible
     * 
     * @param submitter
     * @return
     */
    public List<E> findBySubmitter(Person submitter) {
        DetachedCriteria criteria = getOrderedDetachedCriteria();
        if (submitter == null) {
            return Collections.emptyList();
        }
        SimpleExpression eq = Restrictions.eq("submitter.id", submitter.getId());

        if (!authenticationService.isAdministrator(submitter)) {
            criteria.add(Restrictions.and(eq, Restrictions.or(
                    Restrictions.eq("status", Status.ACTIVE), Restrictions.eq("status", Status.DRAFT))));
        } else {
            criteria.add(eq);
        }
        return findByCriteria(criteria);
    }

    /*
     * abrin 2010-08-20 FIXME: made changes to enable finding all resources that
     * are available to a user. This works for CodingSheets and for Ontologies,
     * but will not properly work for others because it does not take into
     * account the rights associated by the fullUser table. This is not an issue
     * for the above types because they cannot set permissions to 'confidential'
     * and therefore assigning rights does not matter.
     */
    @SuppressWarnings("unchecked")
    public List<E> findSparseResourceBySubmitterType(Person submitter, ResourceType resourceType) {
        Query query = getCurrentSession().getNamedQuery(QUERY_SPARSE_RESOURCES);
        if (submitter != null) {
            query = getCurrentSession().getNamedQuery(QUERY_SPARSE_RESOURCES_SUBMITTER);
            query.setLong("submitter", submitter.getId());
        }
        query.setString("resourceType", resourceType.name());
        query.setString("status", Status.ACTIVE.name());
        query.setReadOnly(true);
        return query.list();
    }

    public boolean hasBeenModifiedSince(Resource resource, Date date) {
        Query query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_MODIFIED_SINCE);
        query.setLong("id", resource.getId());
        query.setDate("date", date);
        int numFound = ((Integer) query.iterate().next()).intValue();
        return numFound > 0;
    }

    @Override
    protected String getDefaultOrderingProperty() {
        return "title";
    }

    public ResourceType findResourceType(Number id) {
        Query query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_RESOURCETYPE);
        query.setLong("id", id.longValue());
        return (ResourceType) query.uniqueResult();
    }

    public void incrementAccessCounter(Resource r) {
        Query query = getCurrentSession().createSQLQuery(NamedNativeQueries.incrementAccessCount(r));
        query.executeUpdate();
    }

    public Number countActiveResources(Class<?> resourceClass) {
        Query query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_SQL_COUNT_ACTIVE_RESOURCE, resourceClass.getSimpleName()));
        return (Number) query.uniqueResult();
    }

    public List<HomepageGeographicKeywordCache> getISOGeographicCounts() {
        logger.info("executing country count from database");

        List<HomepageGeographicKeywordCache> cache = new ArrayList<HomepageGeographicKeywordCache>();
        Query query = getCurrentSession().getNamedQuery(QUERY_MANAGED_ISO_COUNTRIES);
        for (Object o : query.list()) {
            try {
                Object[] objs = (Object[]) o;
                if (objs == null || objs[0] == null)
                    continue;
                cache.add(new HomepageGeographicKeywordCache((String) objs[0], (Level) objs[1], (Long) objs[2]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return cache;
    }

    /**
     * @return
     */
    public List<HomepageResourceCountCache> getResourceCounts() {
        logger.info("executing resource count from database");
        List<HomepageResourceCountCache> resourceTypeCounts = new ArrayList<HomepageResourceCountCache>();
        Query query = getCurrentSession().getNamedQuery(QUERY_ACTIVE_RESOURCE_TYPE_COUNT);

        List<ResourceType> types = new ArrayList<ResourceType>(Arrays.asList(ResourceType.values()));

        for (Object o : query.list()) {
            try {
                Object[] objs = (Object[]) o;
                if (objs == null || objs[0] == null)
                    continue;
                ResourceType resourceType = (ResourceType) objs[1];
                resourceTypeCounts.add(new HomepageResourceCountCache(resourceType, (Long) objs[0]));
                types.remove(resourceType);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        for (ResourceType remainingType : types) {
            resourceTypeCounts.add(new HomepageResourceCountCache(remainingType, 0l));
        }
        
        return resourceTypeCounts;
    }

    public Long getResourceCount(ResourceType resourceType, Status status) {
        Query query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS);
        query.setString("resourceType", resourceType.toString());
        query.setString("status", status.toString());
        Long count = (Long) query.uniqueResult();
        return count;
    }

    public Map<ResourceType, Map<Status, Long>> getResourceCountAndStatusForUser(Person p, List<ResourceType> types) {
        // Query sqlQuery = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_DASHBOARD);
        SQLQuery sqlQuery = NamedNativeQueries.generateDashboardGraphQuery(getCurrentSession());
        sqlQuery.setLong("submitterId", p.getId());
        sqlQuery.setInteger("effectivePermissions", GeneralPermissions.MODIFY_RECORD.getEffectivePermissions() - 1);
        // SQLQuery sqlQuery = getCurrentSession().createSQLQuery(NamedNativeQueries.generateDashboardGraphQuery(p, GeneralPermissions.MODIFY_RECORD));
        Set<Long> ids = new HashSet<Long>();
        Map<ResourceType, Map<Status, Long>> toReturn = new HashMap<ResourceType, Map<Status, Long>>();

        for (Object obj_ : sqlQuery.list()) {
            Object[] objs = (Object[]) obj_;
            Long id_ = (Long) objs[0];
            boolean newId = ids.add(id_);
            if (!newId) {
                continue;
            }
            Status status = Status.valueOf((String) objs[1]);
            ResourceType type = ResourceType.valueOf((String) objs[2]);
            Map<Status, Long> statMap = toReturn.get(type);
            if (statMap == null) {
                statMap = new HashMap<Status, Long>();
                toReturn.put(type, statMap);
            }
            Long count = statMap.get(status);
            if (count == null) {
                count = 0L;
            }
            statMap.put(status, count + 1L);
        }

        return toReturn;
    }

}
