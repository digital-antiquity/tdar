package org.tdar.core.dao.base;

import java.util.Collection;
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
 * $Id$
 * 
 * TDAR interface used to mark Data Access Objects. Provides two base classes
 * for hibernate and jdbc support to remove much of the basic boilerplate code
 * necessary to create a hibernate DAO over some persistent bean or make JDBC
 * queries, respectively.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 * @param <T>
 *            the persistent Entity to which this DAO is providing access
 *            (FIXME: should type parameter extend Persistable or be more
 *            relaxed?)
 */
public interface Dao<T> {

    /**
     * Returns the persistent entity T with the given id or null if none exists.
     * 
     * @param id
     * @return persistent entity T with the given id, or null if none exists.
     */
    T find(Long id);

    <C> C find(Class<C> cls, Long id);
    
    /**
     * Returns all persistent T entities in the database.
     * 
     * @return a List<T> of all persistent T entities in the database.
     */
    List<T> findAll();

    List<T> findAll(int start, int numberOfRecords);

    /**
     * Returns a list of entities that match the set of ids passed in, i.e.,
     * FROM entity-class-name WHERE id in (:ids)
     * 
     * @param ids
     * @return
     */
    List<T> findAll(List<Long> ids);

    /**
     * Returns all persistent T entities in the database, sorted according to
     * some ordering criteria.
     * 
     * @return
     */
    List<T> findAllSorted();

    List<T> findAllSorted(String orderByClause);

    /**
     * Returns the total number of T entities in the database. Equivalent to
     * select count(*) from T.
     * 
     * @return
     */
    Number count();

    /**
     * Persists each entity in the collection to the database. Assumes that all
     * items in the collection are fresh entities.
     * 
     * @param persistentCollection
     */
    <E> void save(Collection<E> persistentCollection);

    /**
     * Saves a fresh entity to the database. Not recommended for updating
     * existing entities, use {@link #saveOrUpdate(Object)} or {@link #update(Object)} in that case.
     * 
     * @param o
     */
    <E> void save(E o);

    <E> void persist(E o);

    /**
     * Saves a fresh entity or updates an existing entity to the database.
     * 
     * @param o
     */
    <E> void saveOrUpdate(E o);

    /**
     * Merges a detached entity (see
     * http://docs.jboss.org/hibernate/stable/entitymanager
     * /reference/en/html/objectstate.html#d0e1318 for more details).
     */
    <E> E merge(E entity);

    /**
     * Updates an existing entity.
     * 
     * @param o
     */
    <E> void update(E o);

    /**
     * Deletes the given entity.
     * 
     * @param o
     */
    <E> void delete(E o);

    /**
     * Deletes each entity in persistentCollection.
     * 
     * @param persistentCollection
     */
    <E> void delete(Collection<E> persistentCollection);

    List<T> findByEqCriteria(Map<String, ?> criteria);

    List<T> findAllByProperty(String propertyName, Object propertyValue);

    List<T> findAllFromList(String propertyName, List<?> propertyValues);

    T findOrCreateById(T entity);

    /**
     * Subclasses receive boilerplate save/delete/find functionality for free.
     * You must pass the class being persisted into the constructor, which is
     * redundant with the generic type information also passed in at
     * compile-time, but this appears to be the best that Java generics can give
     * us at this point.
     */
    @Component
    public abstract static class HibernateBase<P extends Persistable> extends GenericDao implements Dao<P>, TdarNamedQueries {

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
}
