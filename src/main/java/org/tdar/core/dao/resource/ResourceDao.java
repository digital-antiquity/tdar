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

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.cache.HomepageGeographicKeywordCache;
import org.tdar.core.bean.cache.HomepageResourceCountCache;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.GeographicKeyword.Level;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.NamedNativeQueries;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.struts.data.AggregateDownloadStatistic;
import org.tdar.struts.data.AggregateViewStatistic;
import org.tdar.struts.data.DateGranularity;
import org.tdar.struts.data.ResourceSpaceUsageStatistic;

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

    @SuppressWarnings("unchecked")
    public List<ResourceRevisionLog> getLogEntriesForResource(Resource resource) {
        Query query = getCurrentSession().getNamedQuery(LOGS_FOR_RESOURCE);
        query.setParameter("resourceId", resource.getId());
        return (List<ResourceRevisionLog>) query.list();
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
                logger.debug("cannot get iso counts:", e);
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
        types.remove(ResourceType.CODING_SHEET);
        types.remove(ResourceType.ONTOLOGY);
        for (Object o : query.list()) {
            try {
                Object[] objs = (Object[]) o;
                if (objs == null || objs[0] == null)
                    continue;
                ResourceType resourceType = (ResourceType) objs[1];
                Long count = (Long) objs[0];
                if (count > 0) {
                    resourceTypeCounts.add(new HomepageResourceCountCache(resourceType, count));
                }
                types.remove(resourceType);
            } catch (Exception e) {
                logger.debug("cannot get homepage resource cache:", e);
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
        sqlQuery.setInteger("effectivePermissions", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
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

    /*
     * This method is the combined method for finding a random resource in a collection or a project or in all of tDAR. Due to the nature of the
     * database queries that are actually performed, it's split into two parts (a) find the random resource.id and (b) retrieve the resource
     */
    @SuppressWarnings({ "hiding", "unchecked" })
    protected <E> List<E> findRandomFeaturedResource(boolean restrictToFiles, List<ResourceCollection> collections, Project project, int maxResults) {
        logger.trace("find random resource start");

        // use projection to just get the ID of the resource back -- less crazy binding in database queries
        Criteria criteria = getCriteria(persistentClass);
        criteria.setProjection(Projections.projectionList().add(Projections.property("id")));
        criteria.add(Restrictions.eq("status", Status.ACTIVE));
        if (restrictToFiles) {
            criteria.createCriteria("informationResourceFiles");
        }

        if (Persistable.Base.isNotNullOrTransient(project)) {
            criteria.createCriteria("project").add(Restrictions.eq("id", project.getId()));
        }

        if (CollectionUtils.isNotEmpty(collections)) {
            List<Long> idList = new ArrayList<Long>();
            for (ResourceCollection collection : collections) {
                idList.add(collection.getId());
            }
            criteria.createCriteria("resourceCollections").add(Restrictions.in("id", idList));
        }

        criteria.add(Restrictions.sqlRestriction("1=1 order by random()"));
        criteria.setMaxResults(maxResults);

        // find the resource by ID using the projected version
        List<Long> ids = new ArrayList<Long>();
        for (Object result : criteria.list()) {
            ids.add((Long) result);
        }
        logger.trace("find random resource end");
        return (List<E>) findAll(ids);
    }

    public List<AggregateViewStatistic> getAggregateUsageStats(DateGranularity granularity, Date start, Date end, Long minCount) {
        List<AggregateViewStatistic> toReturn = new ArrayList<AggregateViewStatistic>();
        Query query = setupStatsQuery(granularity, start, end, minCount, false);
        for (Object obj_ : query.list()) {
            Object[] obj = (Object[]) obj_;
            AggregateViewStatistic view = new AggregateViewStatistic((Date) obj[3], (Number) obj[4], new Resource((Long) obj[0], (String) obj[1],
                    (ResourceType) obj[2]));
            markReadOnly(view.getResource());
            toReturn.add(view);
        }
        return toReturn;
    }

    public List<AggregateViewStatistic> getUsageStatsForResource(DateGranularity granularity, Date start, Date end, Long minCount, List<Long> resourceIds) {
        Query query = getCurrentSession().getNamedQuery(RESOURCE_ACCESS_HISTORY);
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setParameter("minCount", minCount);
        query.setParameterList("resourceIds", resourceIds);
        return query.list();
    }

    private Query setupStatsQuery(DateGranularity granularity, Date start, Date end, Long minCount, boolean download) {
        Query query = getCurrentSession().getNamedQuery(ACCESS_BY);
        if (download) {
            query = getCurrentSession().getNamedQuery(DOWNLOAD_BY);
        }
        // query.setParameter("part", granularity.name().toLowerCase());
        query.setParameter("start", start);
        query.setParameter("end", end);
        query.setParameter("minCount", minCount);
        return query;
    }

    public List<AggregateDownloadStatistic> getAggregateDownloadStats(DateGranularity granularity, Date start, Date end, Long minCount) {
        List<AggregateDownloadStatistic> toReturn = new ArrayList<AggregateDownloadStatistic>();
        Query query = setupStatsQuery(granularity, start, end, minCount, true);
        for (Object obj_ : query.list()) {
            Object[] obj = (Object[]) obj_;
            InformationResourceFile irf = find(InformationResourceFile.class, (Long) obj[2]);
            toReturn.add(new AggregateDownloadStatistic((Date) obj[0], (Number) obj[1], irf.getFileName(), irf.getId(), irf.getInformationResource().getId()));
        }
        return toReturn;
    }

    public ResourceSpaceUsageStatistic getResourceSpaceUsageStatistics(List<Long> personId, List<Long> resourceId, List<Long> collectionId,
            List<Long> projectId,
            List<Status> statuses, List<VersionType> types) {
        List<Status> statuses_ = new ArrayList<Status>(Arrays.asList(Status.values()));
        List<VersionType> types_ = new ArrayList<VersionType>(Arrays.asList(VersionType.values()));

        if (CollectionUtils.isNotEmpty(types)) {
            types_ = types;
        }

        if (CollectionUtils.isNotEmpty(statuses)) {
            statuses_ = statuses;
        }

        Object[] params = { resourceId, projectId, personId, collectionId, statuses_, types_ };
        logger.info("admin stats [resources: {} projects: {} people: {} collections: {} statuses: {} types: {} ]", params);
        Query query = null;
        if (CollectionUtils.isNotEmpty(resourceId)) {
            query = getCurrentSession().getNamedQuery(SPACE_BY_RESOURCE);
            query.setParameterList("resourceIds", resourceId);
        }
        if (CollectionUtils.isNotEmpty(projectId)) {
            query = getCurrentSession().getNamedQuery(SPACE_BY_PROJECT);
            query.setParameterList("projectIds", projectId);
        }
        if (CollectionUtils.isNotEmpty(personId)) {
            query = getCurrentSession().getNamedQuery(SPACE_BY_SUBMITTER);
            query.setParameterList("submitterIds", personId);
        }
        if (CollectionUtils.isNotEmpty(collectionId)) {
            query = getCurrentSession().getNamedQuery(SPACE_BY_COLLECTION);
            query.setParameterList("collectionIds", collectionId);
        }
        if (query == null) {
            return null;
        }
        query.setParameterList("statuses", statuses_);
        query.setParameterList("types", types_);
        List<?> list = query.list();
        for (Object obj_ : list) {
            Object[] obj = (Object[]) obj_;
            return new ResourceSpaceUsageStatistic((Number) obj[0], (Number) obj[1], (Number) obj[2]);
        }
        return null;
    }
}
