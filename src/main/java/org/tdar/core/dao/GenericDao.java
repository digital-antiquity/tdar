package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

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

    public enum FindOptions {
        FIND_FIRST,
        FIND_ALL,
        FIND_FIRST_OR_CREATE;
    }

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient SessionFactory sessionFactory;

    public <E> E find(Class<E> cls, Long id) {
        // FIXME: push guard checks into Service layer.
        if (id == null)
            return null;
        Object objectInSession = getCurrentSession().get(cls, id);
        logger.trace("{}", objectInSession);
        E obj = cls.cast(objectInSession);
        logger.trace("object: {}", obj);
        return obj;
    }
    
    public <E> List<E> findAllWithProfile(Class<E> class1, List<Long> ids, String profileName) {
        getCurrentSession().enableFetchProfile(profileName);
        List<E> ret = findAll(class1, ids);
        getCurrentSession().disableFetchProfile(profileName);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findAll(Class<E> persistentClass, Collection<Long> ids) {
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
    public <E> List<Long> findAllIds(Class<E> persistentClass) {
        return getCurrentSession().createQuery("select id from " + persistentClass.getName()).list();
    }

    @SuppressWarnings("unchecked")
    public List<Long> findActiveIds(Class<? extends HasStatus> persistentClass) {
        return getCurrentSession().createQuery(String.format("select id from %s where status in ('ACTIVE')", persistentClass.getName())).list();
    }

    @SuppressWarnings("unchecked")
    public <E> List<Long> findAllIds(Class<E> persistentClass, long startId, long endId) {
        String hqlfmt = "select id from %s where id between %s and %s order by id asc";
        String hql = String.format(hqlfmt, persistentClass.getName(), startId, endId);
        return getCurrentSession().createQuery(hql).list();
    }

    public Number count(Class<?> persistentClass) {
        Query query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_SQL_COUNT, persistentClass.getName()));
        return (Number) query.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findIdRange(Class<E> persistentClass, long startId, long endId, int maxResults) {
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
    public <E> List<E> findRandom(Class<E> persistentClass, int maxResults) {
        Criteria criteria = getCriteria(persistentClass);
        if (Resource.class.isAssignableFrom(persistentClass)) {
            criteria.add(Restrictions.eq("status", Status.ACTIVE));
        }
        criteria.add(Restrictions.sqlRestriction("1=1 order by random()"));
        criteria.setMaxResults(maxResults);
        return criteria.list();
    }

    public <E> List<E> findByCriteria(Class<E> cls, DetachedCriteria criteria) {
        return findByCriteria(cls, criteria, -1, -1);
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findByCriteria(Class<E> cls, DetachedCriteria criteria, int start, int numberOfRecords) {
        Session session = getCurrentSession();
        Criteria executableCriteria = criteria.getExecutableCriteria(session);
        if (numberOfRecords != -1) {
            executableCriteria.setMaxResults(numberOfRecords);
            executableCriteria.setFirstResult(start);
        }
        return (List<E>) executableCriteria.list();
    }

    public <P extends Persistable> List<P> loadFromSparseEntities(Collection<P> incoming, Class<P> cls) {
        return findAll(cls, Persistable.Base.extractIds(incoming));
    }

    @SuppressWarnings("unchecked")
    public <P extends Persistable> List<P> populateSparseObjectsById(Collection<Long> ids, Class<?> cls) {
        if (CollectionUtils.isEmpty(ids))
            return Collections.emptyList();
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_SPARSE_RESOURCE_LOOKUP);
        if (cls.isAssignableFrom(ResourceCollection.class)) {
            query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_SPARSE_COLLECTION_LOOKUP);
        }
        query.setParameterList("ids", ids);
        query.setReadOnly(true);
        return query.list();
    }

    public <P extends Persistable> P loadFromSparseEntity(P item, Class<P> cls) {
        if (item == null) {
            return null;
        }
        return find(cls, item.getId());
    }

    public <E> List<E> findAll(Class<E> cls) {
        return findAll(cls, -1);
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findAll(Class<E> cls, int maxResults) {
        Query query = getCurrentSession().createQuery("from " + cls.getName());
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return (List<E>) query.list();
    }

    public <E> List<E> findAllSorted(Class<E> cls) {
        return findAllSorted(cls, getDefaultOrderingProperty() + " asc");
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findAllSorted(Class<E> cls, String orderingProperty) {
        if (StringUtils.isBlank(orderingProperty)) {
            getLogger().warn("Trying to find all sorted with no order by clause, using default ordering property.");
            orderingProperty = getDefaultOrderingProperty() + " asc";
        }
        StringBuilder hqlBuilder = new StringBuilder("from ").append(cls.getName());
        hqlBuilder.append(" order by ").append(orderingProperty);
        Query query = getCurrentSession().createQuery(hqlBuilder.toString());
        return query.list();
    }

    public <E> E findByProperty(Class<E> persistentClass, String propertyName, Object propertyValue) {
        return persistentClass.cast(getCriteria(persistentClass).add(Restrictions.eq(propertyName, propertyValue)).uniqueResult());
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findAllByProperty(Class<E> persistentClass, String propertyName, Object propertyValue) {
        return getCriteria(persistentClass).add(Restrictions.eq(propertyName, propertyValue)).list();
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findAllFromList(Class<E> persistentClass, String propertyName, List<?> propertyValues) {
        return getCriteria(persistentClass).add(Restrictions.in(propertyName, propertyValues.toArray())).list();
    }

    public <E> E findByName(Class<E> persistentClass, String name) {
        return findByProperty(persistentClass, "name", name);
    }

    protected String addWildCards(String value) {
        if (value.charAt(0) == '%' || value.charAt(value.length() - 1) == '%') {
            // no-op if any wildcards are already present.
            return value;
        }
        return String.format("%%%s%%", value);
    }

    public <E> List<E> findByExample(Class<E> persistentClass, E entity, FindOptions option) {
        return findByExample(persistentClass, entity, new ArrayList<String>(), option);
    }

    public <E> List<E> findByExample(Class<E> persistentClass, E entity, List<String> ignoredProperties, FindOptions option) {
        List<E> toReturn = new ArrayList<E>();
        logger.trace("find by example for {}: {}", persistentClass.getName(), entity);
        logger.trace("  ignoring {} [{}]", ignoredProperties, option);
        List<E> found = findByExampleLike(persistentClass, entity, MatchMode.EXACT, ignoredProperties);
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

    public <E extends Persistable> E findOrCreateById(Class<E> persistentClass, E entity) {
        E hibernateProxy = find(persistentClass, entity.getId());
        if (hibernateProxy == null) {
            save(entity);
            return entity;
        }
        return hibernateProxy;
    }

    public <E> ScrollableResults findAllScrollable(Class<E> persistentClass) {
        return getCriteria(persistentClass).setCacheMode(CacheMode.IGNORE).setFetchSize(TdarConfiguration.getInstance().getScrollableFetchSize())
                .scroll(ScrollMode.FORWARD_ONLY);
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findByExampleLike(Class<E> persistentClass, E entity, MatchMode matchMode, List<String> ignoreProperties) {
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

    protected <E> DetachedCriteria getOrderedDetachedCriteria(Class<E> cls) {
        return getOrderedDetachedCriteria(cls, getDefaultOrderingProperty());
    }

    protected <E> DetachedCriteria getOrderedDetachedCriteria(Class<E> cls, String orderBy) {
        return getDetachedCriteria(cls).addOrder(Order.asc(orderBy));
    }

    protected <E> DetachedCriteria getDetachedCriteria(Class<E> persistentClass) {
        return DetachedCriteria.forClass(persistentClass).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

    protected <E> Criteria getCriteria(Class<E> persistentClass) {
        return getCurrentSession().createCriteria(persistentClass).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

    public void save(Collection<?> persistentEntities) {
        for (Object o : persistentEntities) {
            save(o);
        }
    }

    public void saveOrUpdate(Collection<?> persistentEntities) {
        for (Object o : persistentEntities) {
            saveOrUpdate(o);
        }
    }

    public void persist(Object entity) {
        getCurrentSession().persist(entity);
    }

    public void save(Object entity) {
        Session session = getCurrentSession();
        session.save(entity);
    }

    public void saveOrUpdate(Object entity) {
        Session session = getCurrentSession();
        session.saveOrUpdate(entity);
    }

    public void update(Object entity) {
        Session session = getCurrentSession();
        session.update(entity);
    }

    @SuppressWarnings("unchecked")
    public <E> E merge(E entity) {
        Session session = getCurrentSession();
        return (E) session.merge(entity);
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

    public void delete(Object entity) {
        if (entity instanceof HasStatus) {
            ((HasStatus) entity).setStatus(Status.DELETED);
            saveOrUpdate(entity);
        } else if (entity  instanceof InformationResourceFileVersion) {
            if (((InformationResourceFileVersion) entity).isUploadedOrArchival()) {
                throw new TdarRecoverableRuntimeException("Should not delete Uploaded or Archival Version");
            } else {
                forceDelete(entity);
            }
        } else {
            forceDelete(entity);
        }
    }

    public void forceDelete(Object entity) {
        Session session = getCurrentSession();
        session.delete(entity);
    }

    public void detachFromSession(Object entity) {
        Session session = getCurrentSession();
        session.evict(entity);
    }

    public void detachFromSessionAndWarn(Object entity) {
        Session session = getCurrentSession();
        if (session.contains(entity)) {
            logger.error("This entity should not be on the session: {}",entity);
        }
        session.evict(entity);
    }

    /**
     * Deletes all persistent entities and removes them from the hibernate managed collection.
     * 
     * @param persistentEntities
     */
    public void delete(Collection<?> persistentEntities) {
        if (CollectionUtils.isEmpty(persistentEntities)) {
            return;
        }
        Session session = getCurrentSession();
        ArrayList<Object> copy = new ArrayList<Object>(persistentEntities);
        // remove associations first
        persistentEntities.clear();
        // then delete all entities.
        for (Object entity : copy) {
            session.delete(entity);
        }
    }

    public <E extends Persistable> int deleteAll(Class<E> persistentClass) {
        return getCurrentSession().createQuery("DELETE FROM " + persistentClass.getName()).executeUpdate();
    }

    protected String getDefaultOrderingProperty() {
        logger.warn("Did not override getDefaultOrderingProperty, using id as default ordering property.");
        return "id";
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

    public void refresh(Object object) {
        getCurrentSession().refresh(object);
    }

    public void refreshAll(Collection<?> objects) {
        for (Object object : objects) {
            getCurrentSession().refresh(object);
        }
    }

    public void markReadOnly(Object obj) {
        if (getCurrentSession().contains(obj)) {
            // mark as read only
            getCurrentSession().setCacheMode(CacheMode.GET);
            // dump it off the cache so that future searches don't find the updated version
            getCurrentSession().setReadOnly(obj, true);
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
        if (getCurrentSession().contains(obj)) {
            getCurrentSession().setReadOnly(obj, false);
        }
        return obj;
    }
    
    public <O> O markWritable(O obj) {
        if (getCurrentSession().contains(obj)) {
            // theory -- if we're persistable and have not been 'saved' perhaps we don't need to worry about merging yet
            if (obj instanceof Persistable && Persistable.Base.isNotTransient((Persistable) obj)) {
                getCurrentSession().setCacheMode(CacheMode.NORMAL);
                getCurrentSession().setReadOnly(obj, false);
                getCurrentSession().evict(obj);
                return merge(obj);
            }
        }
        return obj;
    }

    public Statistics getSessionStatistics() {
        return getCurrentSession().getSessionFactory().getStatistics();
    }

}
