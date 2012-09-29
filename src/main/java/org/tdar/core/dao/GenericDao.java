package org.tdar.core.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;

/**
 * $Id$
 * 
 * Performs most DAO tasks with an explicit type / class parameter. Mainly used
 * by GenericService.
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

    public <E> E find(Class<E> cls, Number id) {
        // FIXME: push guard checks into Service layer.
        if (id == null)
            return null;
        return cls.cast(getCurrentSession().get(cls, id));
    }

    public Number count(Class<?> persistentClass) {
        Query query = getCurrentSession().createQuery(String.format(TdarNamedQueries.QUERY_SQL_COUNT, persistentClass.getSimpleName()));
        return (Number) query.uniqueResult();
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

    public <E> List<E> findAll(Class<E> cls) {
        return findAll(cls, -1, -1);

        // return findByCriteria(cls, getDetachedCriteria(cls));
    }

    @SuppressWarnings("unchecked")
    public <E> List<E> findAll(Class<E> cls, int start, int numberOfRecords) {
        Query query = getCurrentSession().createQuery("from " + cls.getName());
        if (numberOfRecords > 0) {
            query.setFirstResult(start);
            query.setMaxResults(numberOfRecords);
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
        logger.trace("find by example for {}: {}", persistentClass.getSimpleName(), entity);
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
        Session session = getCurrentSession();
        for (Object o : persistentEntities) {
            session.save(o);
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

    public <P extends Persistable> P merge(P incomingEntity, P existingEntity) {
        logger.trace("exchanging incoming {} with {}", incomingEntity, existingEntity);
        incomingEntity.setId(existingEntity.getId());
        detachFromSession(existingEntity);
        return merge(incomingEntity);
    }

    public void delete(Object entity) {
        Session session = getCurrentSession();
        session.delete(entity);
    }

    public void detachFromSession(Object entity) {
        Session session = getCurrentSession();
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

}
