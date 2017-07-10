package org.tdar.core.dao.base;

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.utils.MessageHelper;

/**
 * Subclasses receive boilerplate save/delete/find functionality for free.
 * You must pass the class being persisted into the constructor, which is
 * redundant with the generic type information also passed in at
 * compile-time, but this appears to be the best that Java generics can give
 * us at this point.
 */
@Component
public abstract class HibernateBase<P extends Persistable> extends GenericDao implements Dao<P>, TdarNamedQueries {

    protected final Class<P> persistentClass;

    /**
     * Subclasses must super to this constructor with the persistent entity
     * class under the DAO.
     * 
     * @param persistentClass
     *            the class to which this DAO provides persistent access.
     */
    public HibernateBase(Class<P> persistentClass) {
        this.persistentClass = persistentClass;
    }

    @Override
    public List<P> findByEqCriteria(Map<String, ?> map) {
        return findByCriteria(getDetachedCriteria().add(Restrictions.allEq(map)));
    }

    public List<P> findByCriteria(DetachedCriteria criteria) {
        return super.findByCriteria(persistentClass, criteria);
    }

    public List<P> findByCriteria(DetachedCriteria criteria, int start, int numberOfRecords) {
        return super.findByCriteria(persistentClass, criteria, start, numberOfRecords);
    }

    @Override
    public Number count() {
        return super.count(persistentClass);
    }

    @Override
    public P find(Long id) {
        return super.find(persistentClass, id);
    }

    public P find(String id) {
        throw new UnsupportedOperationException(MessageHelper.getMessage("error.not_implemented"));
    }

    @Override
    public List<P> findAll() {
        return super.findAll(persistentClass);
    }

    public List<Long> findAllIds() {
        return super.findAllIds(persistentClass);
    }

    @Override
    public List<P> findAll(List<Long> ids) {
        return super.findAll(persistentClass, ids);
    }

    @Override
    public List<P> findAll(int start, int maxResults) {
        return super.findAll(persistentClass, maxResults);
    }

    @Override
    public List<P> findAllSorted() {
        return super.findAllSorted(persistentClass);
    }

    @Override
    public List<P> findAllSorted(String orderByClause) {
        return super.findAllSorted(persistentClass, orderByClause);
    }

    public P findByProperty(String propertyName, Object propertyValue) {
        return super.findByProperty(persistentClass, propertyName, propertyValue);
    }

    @Override
    public List<P> findAllByProperty(String propertyName, Object propertyValue) {
        return super.findAllByProperty(persistentClass, propertyName, propertyValue);
    }

    @Override
    public List<P> findAllFromList(String propertyName, List<?> propertyValues) {
        return super.findAllFromList(persistentClass, propertyName, propertyValues);
    }

    public P findByName(String name) {
        return super.findByProperty(persistentClass, "name", name);
    }

    @Override
    public P findOrCreateById(P entity) {
        return super.findOrCreateById(persistentClass, entity);
    }

    protected DetachedCriteria getOrderedDetachedCriteria() {
        return super.getOrderedDetachedCriteria(persistentClass);
    }

    protected DetachedCriteria getDetachedCriteria() {
        return super.getDetachedCriteria(persistentClass);
    }

    protected Criteria getCriteria() {
        return super.getCriteria(persistentClass);
    }

    public Class<P> getPersistentClass() {
        return persistentClass;
    }
}