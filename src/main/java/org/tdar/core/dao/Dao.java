package org.tdar.core.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;

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
    public T find(Long id);

    /**
     * Returns all persistent T entities in the database.
     * 
     * @return a List<T> of all persistent T entities in the database.
     */
    public List<T> findAll();

    public List<T> findAll(int start, int numberOfRecords);

    /**
     * Returns a list of entities that match the set of ids passed in, i.e.,
     * FROM entity-class-name WHERE id in (:ids)
     * @param ids
     * @return
     */
    public List<T> findAll(List<Long> ids);

    /**
     * Returns all persistent T entities in the database, sorted according to
     * some ordering criteria.
     * 
     * @return
     */
    public List<T> findAllSorted();

    public List<T> findAllSorted(String orderByClause);

    /**
     * Returns the total number of T entities in the database. Equivalent to
     * select count(*) from T.
     * 
     * @return
     */
    public Number count();

    /**
     * Persists each entity in the collection to the database. Assumes that all
     * items in the collection are fresh entities.
     * 
     * @param persistentCollection
     */
    public void save(Collection<?> persistentCollection);

    /**
     * Saves a fresh entity to the database. Not recommended for updating
     * existing entities, use {@link #saveOrUpdate(Object)} or {@link #update(Object)} in that case.
     * 
     * @param o
     */
    public void save(Object o);

    public void persist(Object o);

    /**
     * Saves a fresh entity or updates an existing entity to the database.
     * 
     * @param o
     */
    public void saveOrUpdate(Object o);

    /**
     * Merges a detached entity (see
     * http://docs.jboss.org/hibernate/stable/entitymanager
     * /reference/en/html/objectstate.html#d0e1318 for more details).
     */
    public <E> E merge(E entity);

    /**
     * Updates an existing entity.
     * 
     * @param o
     */
    public void update(Object o);

    /**
     * Deletes the given entity.
     * 
     * @param o
     */
    public void delete(Object o);

    /**
     * Deletes each entity in persistentCollection.
     * 
     * @param persistentCollection
     */
    public void delete(Collection<?> persistentCollection);

    public List<T> findByEqCriteria(Map<String, ?> criteria);

    public List<T> findAllByProperty(String propertyName, Object propertyValue);

    public List<T> findAllFromList(String propertyName, List<?> propertyValues);

    public T findOrCreateById(T entity);

    /**
     * Subclasses receive boilerplate save/delete/find functionality for free.
     * You must pass the class being persisted into the constructor, which is
     * redundant with the generic type information also passed in at
     * compile-time, but this appears to be the best that Java generics can give
     * us at this point.
     */
    @Component
    public abstract static class HibernateBase<E extends Persistable> extends GenericDao implements Dao<E>, TdarNamedQueries {

        protected final Class<E> persistentClass;

        /**
         * Subclasses must super to this constructor with the persistent entity
         * class under the DAO.
         * 
         * @param persistentClass
         *            the class to which this DAO provides persistent access.
         */
        public HibernateBase(Class<E> persistentClass) {
            this.persistentClass = persistentClass;
        }

        public List<E> findByEqCriteria(Map<String, ?> map) {
            return findByCriteria(getDetachedCriteria().add(Restrictions.allEq(map)));
        }

        public List<E> findByCriteria(DetachedCriteria criteria) {
            return super.findByCriteria(persistentClass, criteria);
        }

        public List<E> findByCriteria(DetachedCriteria criteria, int start, int numberOfRecords) {
            return super.findByCriteria(persistentClass, criteria, start, numberOfRecords);
        }

        public Number count() {
            return super.count(persistentClass);
        }

        public E find(Long id) {
            return super.find(persistentClass, id);
        }

        public E find(String id) {
            throw new UnsupportedOperationException("FIXME: unimplemented, reliably convert String into arbitrary subtypes of Number.");
        }

        public List<E> findAll() {
            return super.findAll(persistentClass);
        }
        
        public List<Long> findAllIds() {
            return super.findAllIds(persistentClass);
        }
        
        public List<E> findAll(List<Long> ids) {
            return super.findAll(persistentClass, ids);
        }

        public List<E> findAll(int start, int numberOfRecords) {
            return super.findAll(persistentClass, start, numberOfRecords);
        }

        public List<E> findAllSorted() {
            return super.findAllSorted(persistentClass);
        }

        public List<E> findAllSorted(String orderByClause) {
            return super.findAllSorted(persistentClass, orderByClause);
        }

        public E findByProperty(String propertyName, Object propertyValue) {
            return super.findByProperty(persistentClass, propertyName, propertyValue);
        }

        public List<E> findAllByProperty(String propertyName, Object propertyValue) {
            return super.findAllByProperty(persistentClass, propertyName, propertyValue);
        }

        public List<E> findAllFromList(String propertyName, List<?> propertyValues) {
            return super.findAllFromList(persistentClass, propertyName, propertyValues);
        }

        public E findByName(String name) {
            return super.findByProperty(persistentClass, "name", name);
        }

        public E findOrCreateById(E entity) {
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

        public Class<E> getPersistentClass() {
            return persistentClass;
        }

    }
}
