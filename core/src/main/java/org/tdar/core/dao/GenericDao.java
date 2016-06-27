package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Obfuscatable;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.HasStatic;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * 
 * Provides base DAO support with an explicit type (class) parameter.
 * 
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Component("genericDao")
public class GenericDao {

    private static final String FROM_HQL_ORDER_BY = "from %s order by %s";
    private static final String SELECT_RANGE_HQL = "select id from %s where id between '%s' and '%s' order by id asc";
    private static final String RANDOM = "1=1 order by random()";
    private static final String FROM_HQL = "from %s ";
    private static final String DESC = " desc";
    private static final String ASC = " asc";

    private static final String SELECT_ID_FROM_HQL_ORDER_BY_ID_ASC = "select id from %s order by id asc";

    public enum FindOptions {
        FIND_FIRST,
        FIND_ALL,
        FIND_FIRST_OR_CREATE;
    }

    private final transient Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private transient SessionFactory sessionFactory;

    public <T> T find(Class<T> cls, Long id) {
        // FIXME: push guard checks into Service layer?
        if (id == null) {
            return null;
        }
        Object objectInSession = getCurrentSession().get(cls, id);
        logger.trace("{}", objectInSession);
        T obj = cls.cast(objectInSession);
        logger.trace("object: {}", obj);
        return obj;
    }

    public void setCacheModeForCurrentSession(CacheMode mode) {
        getCurrentSession().setCacheMode(mode);
    }

    public <T> List<T> findAllWithProfile(Class<T> class1, List<Long> ids, String profileName) {
        getCurrentSession().enableFetchProfile(profileName);
        List<T> ret = findAll(class1, ids);
        getCurrentSession().disableFetchProfile(profileName);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAllWithL2Cache(Class<T> persistentClass, Collection<Long> ids) {
        Query query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_FIND_ALL, persistentClass.getName()));
        if (CollectionUtils.isNotEmpty(ids)) {
            query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_FIND_ALL_WITH_IDS, persistentClass.getName()));
            query.setParameterList("ids", ids);
        }
        query.setCacheable(true);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Class<T> persistentClass, Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_FIND_ALL_WITH_IDS, persistentClass.getName()));
        return query.setParameterList("ids", ids).list();
    }

    @SuppressWarnings("unchecked")
    public <F extends HasStatus> List<F> findAllWithStatus(Class<F> persistentClass, Status... statuses) {
        Query query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_FIND_ALL_WITH_STATUS, persistentClass.getName()));
        return query.setParameterList("statuses", statuses).list();
    }

    @SuppressWarnings("unchecked")
    public <T> List<Long> findAllIds(Class<T> persistentClass) {
        return getCurrentSession().createQuery(String.format(SELECT_ID_FROM_HQL_ORDER_BY_ID_ASC, persistentClass.getName())).list();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findActiveIds(Class<? extends HasStatus> persistentClass) {
        if (persistentClass.isAssignableFrom(Institution.class)) {
            return getCurrentSession().createQuery(String.format(TdarNamedQueries.FIND_ACTIVE_INSTITUTION_BY_ID, persistentClass.getName())).list();
        } else if (persistentClass.isAssignableFrom(Person.class)) {
            return getCurrentSession().createQuery(String.format(TdarNamedQueries.FIND_ACTIVE_PERSON_BY_ID, persistentClass.getName())).list();
        } else {
            return getCurrentSession().createQuery(String.format(TdarNamedQueries.FIND_ACTIVE_PERSISTABLE_BY_ID, persistentClass.getName())).list();
        }
    }

    public Number countActive(Class<? extends HasStatus> persistentClass) {
        return (Number) getCurrentSession().createQuery(String.format(TdarNamedQueries.COUNT_ACTIVE_PERSISTABLE_BY_ID, persistentClass.getName()))
                .uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public <T> List<Long> findAllIds(Class<T> persistentClass, long startId, long endId) {
        String hqlfmt = SELECT_RANGE_HQL;
        String hql = String.format(hqlfmt, persistentClass.getName(), startId, endId);
        return getCurrentSession().createQuery(hql).list();
    }

    public Query createQuery(String queryString) {
        return getCurrentSession().createQuery(queryString);
    }

    public Query getNamedQuery(String queryName) {
        return getCurrentSession().getNamedQuery(queryName);
    }

    public Number count(Class<?> persistentClass) {
        Query query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_SQL_COUNT, persistentClass.getName()));
        return (Number) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findIdRange(Class<T> persistentClass, long startId, long endId, int maxResults) {
        Criteria criteria = getCriteria(persistentClass);
        // FIXME: replace with HasStatus isAssignable?
        if (Resource.class.isAssignableFrom(persistentClass)) {
            criteria.add(Restrictions.eq("status", Status.ACTIVE));
        }
        criteria.add(Restrictions.ge("id", startId));
        criteria.add(Restrictions.le("id", endId));
        criteria.addOrder(Order.asc("id"));
        criteria.setMaxResults(maxResults);
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findRandom(Class<T> persistentClass, int maxResults) {
        Criteria criteria = getCriteria(persistentClass);
        if (Resource.class.isAssignableFrom(persistentClass)) {
            criteria.add(Restrictions.eq("status", Status.ACTIVE));
        }
        criteria.add(Restrictions.sqlRestriction(RANDOM));
        criteria.setMaxResults(maxResults);
        return criteria.list();
    }

    public <T> List<T> findByCriteria(Class<T> cls, DetachedCriteria criteria) {
        return findByCriteria(cls, criteria, -1, -1);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findByCriteria(Class<T> cls, DetachedCriteria criteria, int start, int numberOfRecords) {
        Session session = getCurrentSession();
        Criteria executableCriteria = criteria.getExecutableCriteria(session);
        if (numberOfRecords != -1) {
            executableCriteria.setMaxResults(numberOfRecords);
            executableCriteria.setFirstResult(start);
        }
        return executableCriteria.list();
    }

    public <P extends Persistable> List<P> loadFromSparseEntities(Collection<P> incoming, Class<P> cls) {
        return findAll(cls, PersistableUtils.extractIds(incoming));
    }

    @SuppressWarnings("unchecked")
    public <P extends Persistable> List<P> populateSparseObjectsById(Collection<Long> ids, Class<?> cls) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_SPARSE_RESOURCE_LOOKUP);
        if (cls.isAssignableFrom(ResourceCollection.class)) {
            query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_SPARSE_COLLECTION_LOOKUP);
        }
        query.setParameterList("ids", ids);
        query.setReadOnly(true);
        return query.list();
    }
    
    public boolean isSessionReadOnly() {
    	return getCurrentSession().isDefaultReadOnly();
    }

    public <P extends Persistable> P loadFromSparseEntity(P item, Class<P> cls) {
        if (item == null) {
            return null;
        }
        return find(cls, item.getId());
    }

    public <T> List<T> findAll(Class<T> cls) {
        return findAll(cls, -1);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAll(Class<T> cls, int maxResults) {
        Query query = getCurrentSession().createQuery(String.format(FROM_HQL, cls.getName()));
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return query.list();
    }

    public <T> List<T> findAllSorted(Class<T> cls) {
        return findAllSorted(cls, true);
    }

    public <T> List<T> findAllSorted(Class<T> cls, boolean ascending) {
        String ordering = ascending ? ASC : DESC;
        return findAllSorted(cls, getDefaultOrderingProperty() + ordering);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAllSorted(Class<T> cls, String orderingProperty_) {
        String orderingProperty = orderingProperty_;
        if (StringUtils.isBlank(orderingProperty)) {
            getLogger().warn("Trying to find all sorted with no order by clause, using default ordering property.");
            orderingProperty = getDefaultOrderingProperty() + ASC;
        }
        String sql = String.format(FROM_HQL_ORDER_BY, cls.getName(), orderingProperty);
        Query query = getCurrentSession().createQuery(sql);
        return query.list();
    }

    public <T> T findByProperty(Class<T> persistentClass, String propertyName, Object propertyValue) {
        return persistentClass.cast(getCriteria(persistentClass).add(Restrictions.eq(propertyName, propertyValue)).uniqueResult());
    }

    public <T> T findByPropertyIgnoreCase(Class<T> persistentClass, String propertyName, Object propertyValue) {
        return persistentClass.cast(getCriteria(persistentClass).add(Restrictions.eq(propertyName, propertyValue).ignoreCase()).uniqueResult());
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAllByProperty(Class<T> persistentClass, String propertyName, Object propertyValue) {
        return getCriteria(persistentClass).add(Restrictions.eq(propertyName, propertyValue)).list();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findAllFromList(Class<T> persistentClass, String propertyName, List<?> propertyValues) {
        return getCriteria(persistentClass).add(Restrictions.in(propertyName, propertyValues.toArray())).list();
    }

    public <T> T findByName(Class<T> persistentClass, String name) {
        return findByProperty(persistentClass, "name", name);
    }

    protected String addWildCards(String value) {
        if ((value.charAt(0) == '%') || (value.charAt(value.length() - 1) == '%')) {
            // no-op if any wildcards are already present.
            return value;
        }
        return String.format("%%%s%%", value);
    }

    public <T> List<T> findByExample(Class<T> persistentClass, T entity, FindOptions option) {
        return findByExample(persistentClass, entity, new ArrayList<String>(), option);
    }

    public <T> List<T> findByExample(Class<T> persistentClass, T entity, List<String> ignoredProperties, FindOptions option) {
        List<T> toReturn = new ArrayList<T>();
        logger.trace("find by example for {}: {}", persistentClass.getName(), entity);
        logger.trace("  ignoring {} [{}]", ignoredProperties, option);
        List<T> found = findByExampleLike(persistentClass, entity, MatchMode.EXACT, ignoredProperties);
        logger.trace("found: {} ", found);
        switch (option) {
            case FIND_FIRST_OR_CREATE:
                if (CollectionUtils.isEmpty(found)) {
                    save(entity);
                    toReturn.add(entity);
                    break;
                } // don't break if found has things in, just getFirst
            case FIND_FIRST:
                if (!CollectionUtils.isEmpty(found)) {
                    toReturn.add(found.get(0));
                }
                break;
            case FIND_ALL:
                toReturn.addAll(found);
                break;
        }
        logger.trace("returning: {} ", toReturn);
        return toReturn;
    }

    /**
     * NOTE: This method, while convenient, can become extremely inefficient. Use with care.
     * 
     * @param classToCreate
     * @param incomingCollection
     * @param ignoreProperties
     * @param options
     * @return
     */
    public <T> Set<T> findByExamples(Class<T> classToCreate, Collection<T> incomingCollection, List<String> ignoreProperties, FindOptions options) {
        Set<T> toReturn = new HashSet<T>();
        if (CollectionUtils.isEmpty(incomingCollection)) {
            return toReturn;
        }
        for (T incomingElement : incomingCollection) {
            if (incomingElement == null) {
                logger.debug("Skipping null example");
                continue;
            }
            toReturn.addAll(findByExample(classToCreate, incomingElement, ignoreProperties, options));
        }
        return toReturn;
    }

    public <T extends Persistable> T findOrCreateById(Class<T> persistentClass, T entity) {
        T hibernateProxy = find(persistentClass, entity.getId());
        if (hibernateProxy == null) {
            save(entity);
            return entity;
        }
        return hibernateProxy;
    }

    public <T> ScrollableResults findAllScrollable(Class<T> persistentClass) {
        return getCriteria(persistentClass).addOrder(Order.asc("id")).setCacheMode(CacheMode.IGNORE)
                .setFetchSize(TdarConfiguration.getInstance().getScrollableFetchSize())
                .scroll(ScrollMode.FORWARD_ONLY);
    }

    public <T> ScrollableResults findAllActiveScrollable(Class<T> persistentClass) {
        return getCriteria(persistentClass).add(Restrictions.eq("status", Status.ACTIVE)).
                setCacheMode(CacheMode.IGNORE).setFetchSize(TdarConfiguration.getInstance().getScrollableFetchSize())
                .scroll(ScrollMode.FORWARD_ONLY);
    }

    public <T> ScrollableResults findAllScrollable(Class<T> persistentClass, int batchSize) {
        return getCriteria(persistentClass).addOrder(Order.asc("id")).setCacheMode(CacheMode.IGNORE).setFetchSize(batchSize).scroll(ScrollMode.FORWARD_ONLY);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findByExampleLike(Class<T> persistentClass, T entity, MatchMode matchMode, List<String> ignoreProperties) {
        Example example = Example.create(entity);
        example.enableLike(matchMode);
        if (!CollectionUtils.isEmpty(ignoreProperties)) {
            for (String ignoreTerm : ignoreProperties) {
                example.excludeProperty(ignoreTerm);
            }
        }
        example.ignoreCase();
        Criteria criteria = getCriteria(persistentClass).add(example);
        return criteria.list();
    }

    protected <T> DetachedCriteria getOrderedDetachedCriteria(Class<T> cls) {
        return getOrderedDetachedCriteria(cls, getDefaultOrderingProperty());
    }

    protected <T> DetachedCriteria getOrderedDetachedCriteria(Class<T> cls, String orderBy) {
        return getDetachedCriteria(cls).addOrder(Order.asc(orderBy));
    }

    protected <T> DetachedCriteria getDetachedCriteria(Class<T> persistentClass) {
        return DetachedCriteria.forClass(persistentClass).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    protected <T> Criteria getCriteria(Class<T> persistentClass) {
        return getCurrentSession().createCriteria(persistentClass).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    public <T> void save(Collection<T> persistentEntities) {
        for (T o : persistentEntities) {
            save(o);
        }
    }

    public <T> void saveOrUpdate(Collection<T> persistentEntities) {
        for (T o : persistentEntities) {
            saveOrUpdate(o);
        }
    }

    public <T> void persist(T entity) {
        if (entity instanceof Obfuscatable && ((Obfuscatable) entity).isObfuscated()) {
            getCurrentSession().persist(entity);
        }
    }

    public <T> void save(T entity) {
        Session session = getCurrentSession();
        if (entity instanceof Obfuscatable && ((Obfuscatable) entity).isObfuscated()) {
            throw new TdarRecoverableRuntimeException(String.format("trying to save an obfuscated object %s ", entity));
        }
        session.save(entity);
    	publisher.publishEvent(new TdarEvent(entity, EventType.CREATE_OR_UPDATE));
    }

    public <T> void saveOrUpdate(T entity) {
        Session session = getCurrentSession();
        if (entity instanceof Obfuscatable && ((Obfuscatable) entity).isObfuscated()) {
            throw new TdarRecoverableRuntimeException(String.format("trying to save an obfuscated object %s ", entity));
        }
        session.saveOrUpdate(entity);
        fireEvent(entity);
    }

    public <T> void update(T entity) {
        Session session = getCurrentSession();
        if (entity instanceof Obfuscatable && ((Obfuscatable) entity).isObfuscated()) {
            throw new TdarRecoverableRuntimeException(String.format("trying to update an obfuscated object %s ", entity));
        }
        session.update(entity);
        fireEvent(entity);
    }

    @SuppressWarnings("unchecked")
    public <T> T merge(T entity) {
        Session session = getCurrentSession();
        return (T) session.merge(entity);
    }

    public <P extends Persistable> P merge(P incomingUnmanagedEntity, P existingManagedEntity) {
        logger.trace("exchanging incoming {} with {}", incomingUnmanagedEntity, existingManagedEntity);
        detachFromSession(existingManagedEntity);
        detachFromSession(incomingUnmanagedEntity);
        incomingUnmanagedEntity.setId(existingManagedEntity.getId());
        P entity = merge(incomingUnmanagedEntity);
        // FIXME: this shouldn't be necessary to bring the entity onto the session, need to get the tests to simulate
        // transaction boundary semantics similar to web requests.
        saveOrUpdate(entity);
        return entity;
    }

    public <T> void delete(T entity) {
        if (entity instanceof HasStatus) {
            ((HasStatus) entity).setStatus(Status.DELETED);
            saveOrUpdate(entity);
            fireEvent(entity);

            return;
        }
        publisher.publishEvent(new TdarEvent(entity, EventType.DELETE));

        if (entity instanceof InformationResourceFileVersion) {
            if (((InformationResourceFileVersion) entity).isUploadedOrArchival()) {
                throw new TdarRecoverableRuntimeException("error.cannot_delete_archival");
            }
        }

        forceDelete(entity);
    }

    private <T> void fireEvent(T entity) {
        	publisher.publishEvent(new TdarEvent(entity, EventType.CREATE_OR_UPDATE));
    }

    public <T> void forceDelete(T entity) {
        Session session = getCurrentSession();
        session.delete(entity);
    }

    public <T> void detachFromSession(T entity) {
        Session session = getCurrentSession();
        session.evict(entity);
    }

    public <T> void detachFromSession(Collection<T> entities) {
        Session session = getCurrentSession();
        for (T entity : entities) {
            session.evict(entity);
        }
    }

    public <T> boolean sessionContains(T entity) {
        if (entity instanceof HasStatic && ((HasStatic) entity).isStatic()) {
            return false;
        }

        return getCurrentSession().contains(entity);
    }

    public <T> void detachFromSessionAndWarn(T entity) {
        Session session = getCurrentSession();
        if (sessionContains(entity)) {
            logger.error("This entity should not be on the session: {}", entity);
        }
        session.evict(entity);
    }

    /**
     * Deletes all persistent entities and removes them from the hibernate managed collection.
     * 
     * @param persistentEntities
     */
    public <T> void delete(Collection<T> persistentEntities) {
        if (CollectionUtils.isEmpty(persistentEntities)) {
            return;
        }
        Session session = getCurrentSession();
        ArrayList<T> copy = new ArrayList<>(persistentEntities);
        // remove associations first
        persistentEntities.clear();
        // then delete all entities.
        for (T entity : copy) {
            session.delete(entity);
        }
    }

    public <T extends Persistable> int deleteAll(Class<T> persistentClass) {
        return getCurrentSession().createQuery("DELETE FROM " + persistentClass.getName()).executeUpdate();
    }

    protected String getDefaultOrderingProperty() {
        logger.warn("Did not override getDefaultOrderingProperty, using id as default ordering property.");
        return "id";
    }

    public Integer getCurrentSessionHashCode() {
        return getCurrentSession().hashCode();
    }

    public SQLQuery getNativeQuery(String sql) {
        return getCurrentSession().createSQLQuery(sql);
    }
    
    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    protected Logger getLogger() {
        return logger;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Note: this should not be used without thought: 1. SearchIndexService
     * calls this to make sure indexing reflects the DB underneath 2.
     * Retrainslated calls this when dealing with a unique constraint that
     * Hibernate doesn't get:
     * http://community.jboss.org/wiki/HibernateFAQ-AdvancedProblems#Hibernate_is_violating_a_unique_constraint
     */
    @Deprecated
    public void synchronize() {
        Session session = getCurrentSession();
        session.flush();
    }

    /**
     * Clears out the hibernate session, evicting all entities and discarding any unflushed changes. {@see org.hibernate.Session#clear()}
     */
    public void clearCurrentSession() {
        getCurrentSession().clear();
    }

    public <T> void refresh(T object) {
        getCurrentSession().refresh(object);
    }

    public <T> void refreshAll(Collection<T> objects) {
        for (T object : objects) {
            getCurrentSession().refresh(object);
        }
    }

    public <T> void markReadOnly(T obj) {
        if (sessionContains(obj)) {
            // mark as read only
            // dump it off the cache so that future searches don't find the updated version
            boolean readOnly = getCurrentSession().isReadOnly(obj);
            logger.trace("object read only?: {}", readOnly);
            if (!readOnly) {
                getCurrentSession().setReadOnly(obj, true);
            }
        }
    }

    public void markReadOnly() {
        logger.trace("read only object session ID: {}", getCurrentSession().hashCode());
        getCurrentSession().setDefaultReadOnly(true);
    }

    public void markWritable() {
        logger.trace("writable object session ID: {}", getCurrentSession().hashCode());
        getCurrentSession().setDefaultReadOnly(false);
    }

    public <O> O markWritableOnExistingSession(O obj) {
        if (sessionContains(obj)) {
            getCurrentSession().setReadOnly(obj, false);
        }
        return obj;
    }

    /**
     * Evict a read-only entity from the current session return an equivalent, writeable enity.
     * 
     * @param obj
     *            read-only entity.
     * @param <T>
     * @return writeable entity instance.
     */
    public <T> T markWritable(T obj) {

        if (sessionContains(obj)) {
            // theory -- if we're persistable and have not been 'saved' perhaps we don't need to worry about merging yet
            if ((obj instanceof Persistable) && PersistableUtils.isNotTransient((Persistable) obj)) {
                getCurrentSession().setReadOnly(obj, false);
                getCurrentSession().evict(obj);
                return merge(obj);
            }
        }
        return obj;
    }

    /**
     * Set a read-only entity to be writeable. This method applies any pending (i.e. non-flushed) changes to the object. Note that this operation does
     * not cascade. Any pending, non-flushed modifications to entity children will be lost, <i>even if they are marked CASCADE_UPDATE</i>
     * Similar to {@link #markWritable(Object)}, however, this method does not evict the supplied entity.
     * 
     * @param entity
     *            read-only, persistent entity
     * @param <T>
     *
     * @see <a href="https://docs.jboss.org/hibernate/orm/4.3/manual/en-US/html_single/#readonly-api-entity">Making a persistent entity read-only</a>
     *      Which covers how to make a read-only entity writeable again
     */
    public <T> void markUpdatable(T entity) {
        Session session = getCurrentSession();
        if (logger.isTraceEnabled()) {
            if (!session.isReadOnly(entity)) {
                logger.warn("Unnecessary call to markUpdatable - object was not read-only:{}", entity);
            }
        }
        // mark entity writable
        session.setReadOnly(entity, false);

        // evict the read-only entity so it is detached
        session.evict(entity);

        // make the detached entity (with the non-flushed changes) persistent
        session.update(entity);

        // now entity is no longer read-only and its changes can be flushed
    }

    public <T> void markUpdatable(Collection<T> entities) {
        for (T t : entities) {
            markUpdatable(t);
        }
    }

    public Statistics getSessionStatistics() {
        return getCurrentSession().getSessionFactory().getStatistics();
    }

    public boolean isSessionOpen() {
        return getCurrentSession().isOpen();
    }

    public boolean isSessionWritable() {
        return !getCurrentSession().isDefaultReadOnly();
    }

    public boolean cacheContains(Class<?> cls, Long id) {
        return sessionFactory.getCache().containsEntity(cls, id);
    }

    public void evictFromCache(Persistable id) {
        sessionFactory.getCache().evictEntity(id.getClass(), id);
    }

	public CacheMode getCacheModeForCurrentSession() {
		return getCurrentSession().getCacheMode();
	}

}
