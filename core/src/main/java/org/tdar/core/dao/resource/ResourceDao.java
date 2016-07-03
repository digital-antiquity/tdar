package org.tdar.core.dao.resource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.query.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.statistics.AggregateDownloadStatistic;
import org.tdar.core.bean.statistics.AggregateViewStatistic;
import org.tdar.core.cache.HomepageGeographicCache;
import org.tdar.core.cache.HomepageResourceCountCache;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.NamedNativeQueries;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.resource.stats.DateGranularity;
import org.tdar.core.dao.resource.stats.ResourceSpaceUsageStatistic;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

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
	private AuthorizationService authenticationService;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public ResourceDao(Class<E> resourceClass) {
		super(resourceClass);
	}

	@SuppressWarnings("unchecked")
	public Set<Long> findResourcesSubmittedByUser(Person person) {
		Query<Long> query = getCurrentSession().getNamedQuery(QUERY_RESOURCES_SUBMITTER);
		query.setParameter("submitterId", person.getId());
		return new HashSet<Long>(query.getResultList());
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
	public List<E> findBySubmitter(TdarUser submitter) {
		DetachedCriteria criteria = getOrderedDetachedCriteria();
		if (submitter == null) {
			return Collections.emptyList();
		}
		SimpleExpression eq = Restrictions.eq("submitter.id", submitter.getId());

		if (!authenticationService.isAdministrator(submitter)) {
			criteria.add(Restrictions.and(eq, Restrictions.or(Restrictions.eq("status", Status.ACTIVE),
					Restrictions.eq("status", Status.DRAFT))));
		} else {
			criteria.add(eq);
		}
		return findByCriteria(criteria);
	}

	@SuppressWarnings("unchecked")
	public List<ResourceRevisionLog> getLogEntriesForResource(Resource resource) {
		Query<ResourceRevisionLog> query = getCurrentSession().getNamedQuery(LOGS_FOR_RESOURCE);
		query.setParameter("resourceId", resource.getId());
		return query.getResultList();
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
		Query<E> query = getCurrentSession().getNamedQuery(QUERY_SPARSE_RESOURCES);
		if (submitter != null) {
			query = getCurrentSession().getNamedQuery(QUERY_SPARSE_RESOURCES_SUBMITTER);
			query.setParameter("submitter", submitter.getId());
		}
		query.setParameter("resourceType", resourceType);
		query.setParameter("status", Status.ACTIVE);
		query.setReadOnly(true);
		return query.getResultList();
	}

	public boolean hasBeenModifiedSince(Resource resource, Date date) {
		Query<Number> query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_MODIFIED_SINCE);
		query.setParameter("id", resource.getId());
		query.setParameter("date", date);
		int numFound = query.getSingleResult().intValue();
		return numFound > 0;
	}

	@Override
	protected String getDefaultOrderingProperty() {
		return "title";
	}

	public ResourceType findResourceType(Number id) {
		Query<ResourceType> query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_RESOURCETYPE);
		query.setParameter("id", id.longValue());
		return  query.getSingleResult();
	}

	public void incrementAccessCounter(Resource r) {
		Query query = getCurrentSession().createNativeQuery(NamedNativeQueries.incrementAccessCount(r));
		query.executeUpdate();
	}

	public Number countActiveResources(ResourceType type) {
		Query<Number> query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_SQL_COUNT_ACTIVE_RESOURCE,
				type.getResourceClass().getSimpleName(), type.name()));
		return query.getSingleResult();
	}

	public Number countActiveResourcesWithFiles(ResourceType type) {
		if (type == ResourceType.PROJECT) {
			return 0;
		}
		Query<Number> query = getCurrentSession().createNativeQuery(
				String.format(TdarNamedQueries.QUERY_SQL_COUNT_ACTIVE_RESOURCE_WITH_FILES, type.name()));
		return query.getSingleResult();
	}

	public List<HomepageGeographicCache> getISOGeographicCounts() {
		logger.info("executing country count from database");

		List<HomepageGeographicCache> cache = new ArrayList<HomepageGeographicCache>();
		Query query = getCurrentSession().createNativeQuery(TdarNamedQueries.HOMEPAGE_GEOGRAPHIC);
		Map<String, Integer> totals = new HashMap<String, Integer>();
		for (Object o : query.getResultList()) {
			try {
				Object[] objs = (Object[]) o;
				if ((objs == null) || (objs[0] == null)) {
					continue;
				}
				String code = (String) objs[0];
				Integer count = ((Number) objs[2]).intValue();
				BigInteger bigint = (BigInteger) objs[3];
				ResourceType resourceType = ResourceType.valueOf((String) objs[1]);
				String label = MessageHelper.getInstance().getText(resourceType.getPluralLocaleKey());
                cache.add(new HomepageGeographicCache(code, resourceType, label ,count, bigint.longValue()));
				if (!totals.containsKey(code)) {
					totals.put(code, 0);
				}
				totals.put(code, totals.get(code) + count);
			} catch (Exception e) {
				logger.debug("cannot get iso counts:", e);
			}
		}
		for (String code : totals.keySet()) {
			cache.add(new HomepageGeographicCache(code, null, null, totals.get(code), null));
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
		for (Object o : query.getResultList()) {
			try {
				Object[] objs = (Object[]) o;
				if ((objs == null) || (objs[0] == null)) {
					continue;
				}
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

		// for (ResourceType remainingType : types) {
		// resourceTypeCounts.add(new HomepageResourceCountCache(remainingType,
		// 0l));
		// }

		return resourceTypeCounts;
	}

	public Long getResourceCount(ResourceType resourceType, Status status) {
		Query<Long> query = getCurrentSession().getNamedQuery(QUERY_RESOURCE_COUNT_BY_TYPE_AND_STATUS);
		query.setParameter("resourceType", resourceType);
		query.setParameter("status", status);
		Long count = query.getSingleResult();
		return count;
	}

	public ResourceTypeStatusInfo getResourceCountAndStatusForUser(Person p, List<ResourceType> types) {
		// Query sqlQuery =
		// getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_DASHBOARD);
		NativeQuery sqlQuery = NamedNativeQueries.generateDashboardGraphQuery(getCurrentSession());
		sqlQuery.setParameter("submitterId", p.getId());
		sqlQuery.setParameter("effectivePermissions", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
		Set<Long> ids = new HashSet<Long>();
		ResourceTypeStatusInfo info = new ResourceTypeStatusInfo();
		for (Object obj_ : sqlQuery.getResultList()) {
			Object[] objs = (Object[]) obj_;
			Long id_ = ((Number) objs[0]).longValue();
			boolean newId = ids.add(id_);
			if (!newId) {
				continue;
			}
			Status status = Status.valueOf((String) objs[1]);
			ResourceType type = ResourceType.valueOf((String) objs[2]);
			info.increment(status, type);
		}

		return info;
	}

	/*
	 * This method is the combined method for finding a random resource in a
	 * collection or a project or in all of tDAR. Due to the nature of the
	 * database queries that are actually performed, it's split into two parts
	 * (a) find the random resource.id and (b) retrieve the resource
	 */
	@SuppressWarnings({ "hiding", "unchecked" })
	protected <E> List<E> findRandomFeaturedResource(boolean restrictToFiles, List<ResourceCollection> collections,
			Project project, int maxResults) {
		logger.trace("find random resource start");

		// use projection to just get the ID of the resource back -- less crazy
		// binding in database queries
		Criteria criteria = getCriteria(persistentClass);
		criteria.setProjection(Projections.projectionList().add(Projections.property("id")));
		criteria.add(Restrictions.eq("status", Status.ACTIVE));
		if (restrictToFiles) {
			criteria.createCriteria("informationResourceFiles");
		}

		if (PersistableUtils.isNotNullOrTransient(project)) {
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

	@SuppressWarnings("unchecked")
	public List<AggregateViewStatistic> getAggregateUsageStats(DateGranularity granularity, Date start, Date end,
			Long minCount) {
		Query<AggregateViewStatistic> query = setupStatsQuery(start, end, minCount, StatisticsQueryMode.ACCESS_DAY);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AggregateViewStatistic> getOverallUsageStats(Date start, Date end, Long max) {
		Query<AggregateViewStatistic> query = setupStatsQuery(start, end, 1L, StatisticsQueryMode.ACCESS_OVERALL);
		query.setMaxResults(max.intValue());
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<AggregateViewStatistic> getUsageStatsForResource(DateGranularity granularity, Date start, Date end,
			Long minCount, List<Long> resourceIds) {
		Query<AggregateViewStatistic> query = getCurrentSession().getNamedQuery(RESOURCE_ACCESS_HISTORY);
		query.setParameter("start", start);
		query.setParameter("end", end);
		query.setParameter("minCount", minCount);
		query.setParameter("resourceIds", resourceIds);
		return query.getResultList();
	}

	public enum StatisticsQueryMode {
		ACCESS_DAY, ACCESS_OVERALL, DOWNLOAD_DAY;
	}

	@SuppressWarnings("unchecked")
	public List<AggregateDownloadStatistic> getDownloadStatsForFile(DateGranularity granularity, Date start, Date end,
			Long minCount, Long... irFileIds) {
		Query<AggregateDownloadStatistic> query = getCurrentSession().getNamedQuery(FILE_DOWNLOAD_HISTORY);
		query.setParameter("start", start);
		query.setParameter("end", end);
		query.setParameter("minCount", minCount);
		query.setParameter("fileIds", Arrays.asList(irFileIds));
		return query.getResultList();
	}

	private <D> Query<D> setupStatsQuery(Date start, Date end, Long minCount, StatisticsQueryMode mode) {
		Query<D> query = getCurrentSession().getNamedQuery(ACCESS_BY);
		switch (mode) {
		case ACCESS_DAY:
			break;
		case ACCESS_OVERALL:
			query = getCurrentSession().getNamedQuery(ACCESS_BY_OVERALL);
			break;
		case DOWNLOAD_DAY:
			query = getCurrentSession().getNamedQuery(DOWNLOAD_BY);
			break;
		}
		// query.setParameter("part", granularity.name().toLowerCase());
		query.setParameter("start", start);
		query.setParameter("end", end);
		query.setParameter("minCount", minCount);
		return query;
	}

	public <D> List<D> getAggregateDownloadStats(DateGranularity granularity, Date start, Date end,
			Long minCount) {
		Query<D> query = setupStatsQuery(start, end, minCount, StatisticsQueryMode.DOWNLOAD_DAY);
		logger.trace("s:{} e: {} min:{}", start, end, minCount);
		return query.getResultList();
	}

	public ResourceSpaceUsageStatistic getSpaceUsageForCollections(List<Long> collectionId, List<Status> statuses) {
		List<Status> statuses_ = new ArrayList<Status>(Arrays.asList(Status.values()));

		if (CollectionUtils.isNotEmpty(statuses)) {
			statuses_ = statuses;
		}

		Query query = getCurrentSession().getNamedQuery(SPACE_BY_COLLECTION);
		query.setParameter("collectionIds", collectionId);
		query.setParameter("statuses", statuses_);
		List<?> list = query.getResultList();
		for (Object obj_ : list) {
			Object[] obj = (Object[]) obj_;
			return new ResourceSpaceUsageStatistic((Number) obj[0], (Number) obj[1], (Number) obj[2]);
		}
		return null;

	}

	public ResourceSpaceUsageStatistic getResourceSpaceUsageStatisticsForUser(List<Long> personId,
			List<Status> status) {
		List<Status> statuses_ = new ArrayList<Status>(Arrays.asList(Status.values()));

		if (CollectionUtils.isNotEmpty(status)) {
			statuses_ = status;
		}

		Query query = getCurrentSession().getNamedQuery(SPACE_BY_SUBMITTER);
		query.setParameter("submitterIds", personId);

		query.setParameter("statuses", statuses_);
		// query.setParameter("types", types_);
		List<?> list = query.getResultList();
		for (Object obj_ : list) {
			Object[] obj = (Object[]) obj_;
			return new ResourceSpaceUsageStatistic((Number) obj[0], (Number) obj[1], (Number) obj[2]);
		}
		return null;
	}

	public ResourceSpaceUsageStatistic getResourceSpaceUsageStatistics(List<Long> resourceId, List<Long> projectId,
			List<Status> statuses) {
		List<Status> statuses_ = new ArrayList<Status>(Arrays.asList(Status.values()));

		if (CollectionUtils.isNotEmpty(statuses)) {
			statuses_ = statuses;
		}

		Object[] params = { resourceId, projectId, statuses_ };
		logger.trace("admin stats [resources: {} projects: {} people: {} collections: {} statuses: {}  ]", params);
		Query query = null;
		if (CollectionUtils.isNotEmpty(resourceId)) {
			query = getCurrentSession().getNamedQuery(SPACE_BY_RESOURCE);
			query.setParameter("resourceIds", resourceId);
		}
		if (CollectionUtils.isNotEmpty(projectId)) {
			query = getCurrentSession().getNamedQuery(SPACE_BY_PROJECT);
			query.setParameter("projectIds", projectId);
		}
		if (query == null) {
			return null;
		}
		query.setParameter("statuses", statuses_);
		// query.setParameter("types", types_);
		List<?> list = query.getResultList();
		for (Object obj_ : list) {
			Object[] obj = (Object[]) obj_;
			return new ResourceSpaceUsageStatistic((Number) obj[0], (Number) obj[1], (Number) obj[2]);
		}
		return null;
	}

	public ResourceSpaceUsageStatistic getResourceSpaceUsageStatistics(List<Long> resourceId, List<Status> statuses) {
		List<Status> statuses_ = new ArrayList<Status>(Arrays.asList(Status.values()));

		if (CollectionUtils.isNotEmpty(statuses)) {
			statuses_ = statuses;
		}

		Query query = getCurrentSession().getNamedQuery(SPACE_BY_RESOURCE);
		query.setParameter("resourceIds", resourceId);

		query.setParameter("statuses", statuses_);
		// query.setParameter("types", types_);
		List<?> list = query.getResultList();
		for (Object obj_ : list) {
			Object[] obj = (Object[]) obj_;
			return new ResourceSpaceUsageStatistic((Number) obj[0], (Number) obj[1], (Number) obj[2]);
		}
		return null;
	}

	public ResourceSpaceUsageStatistic getResourceSpaceUsageStatisticsForProject(Long resourceId,
			List<Status> statuses) {
		List<Status> statuses_ = new ArrayList<Status>(Arrays.asList(Status.values()));

		if (CollectionUtils.isNotEmpty(statuses)) {
			statuses_ = statuses;
		}

		Query query = getCurrentSession().getNamedQuery(SPACE_BY_PROJECT);
		query.setParameter("projectIds", Arrays.asList(resourceId));

		query.setParameter("statuses", statuses_);
		// query.setParameter("types", types_);
		List<?> list = query.getResultList();
		for (Object obj_ : list) {
			Object[] obj = (Object[]) obj_;
			return new ResourceSpaceUsageStatistic((Number) obj[0], (Number) obj[1], (Number) obj[2]);
		}
		return null;
	}

	public ScrollableResults findAllActiveScrollableForSitemap() {
		Query<Resource> query = getCurrentSession().getNamedQuery(SCROLLABLE_SITEMAP);
		return query.setCacheMode(CacheMode.IGNORE).scroll(ScrollMode.FORWARD_ONLY);

	}
}
