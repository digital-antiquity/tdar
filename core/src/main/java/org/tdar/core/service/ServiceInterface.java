package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.dao.base.Dao;

/**
 * $Id$
 * 
 * This interface provides utility base classes, Service.Base, and
 * Service.TypedDaoBase that eases most of the boilerplate implementation for
 * delegating various Service methods to a DAO. Use the TypedDaoBase when you
 * want to use specific DAO methods on a custom DAO type besides the ones
 * specified on the Dao interface (e.g., if SomeCustomTypeDao has a method
 * findAllCustomTypes() you should extend and parameterize Service.TypedDaoBase
 * with SomeCustomTypeDao so that getDao() will return the SomeCustomTypeDao
 * subtype.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 * 
 * @param <T>
 */

public interface ServiceInterface<T, S extends Dao<T>> {

    /**
     * Find a @link Persistable by Id
     * 
     * @param id
     * @return
     */
    T find(Long id);

    /**
     * Merge all items in the collection back onto the hibernate session
     * 
     * @param collection
     * @return
     */
    Collection<T> mergeAll(Collection<T> collection);

    /**
     * Save or Update all Persistables in the collection
     * 
     * @see #saveOrUpdate(T)
     * 
     * @param c
     */
    void saveOrUpdateAll(Collection<T> c);

    /**
     * Merge the specific @link Persistable onto the session
     * 
     * @param entity
     * @return
     */
    T merge(T entity);

    /**
     * Find all @link Persistable entries that match the map of equality criteria (Exact matches)
     * 
     * @param propertyMap
     * @return
     */
    List<T> findByEqCriteria(Map<String, ?> propertyMap);

    /**
     * Find all items of the generic class by ID
     * 
     * @param id
     * @return
     */
    List<T> findAll();

    /**
     * Find all, but with paging
     * 
     * @param start
     * @param numberOfRecords
     * @return
     */
    List<T> findAll(int start, int numberOfRecords);

    /**
     * Find all of the resources in the list of IDs
     * 
     * @param ids
     * @return
     */
    List<T> findAll(List<Long> ids);

    /**
     * Find all sorted by #getDefaultOrderingProperty() default is ID
     * 
     * @return
     */
    List<T> findAllSorted();

    /**
     * Find all sorted by ordering clause
     * 
     * @param orderingClause
     * @return
     */
    List<T> findAllSorted(String orderingClause);

    /**
     * Save all in list (Save is hibernate's concept of save, new objects only)
     * 
     * @param persistentCollection
     */
    void save(List<T> persistentCollection);

    /**
     * Save just the entity (Save is hibernate's concept of save, new objects only)
     * 
     * @param entity
     */
    void save(T entity);

    /**
     * Useful hibernate construct, save if needed, otherwise update
     * 
     * @param entity
     */
    void saveOrUpdate(T entity);

    /**
     * Update the version in the database with the version passed
     * 
     * @param entity
     */
    void update(T entity);

    /**
     * Delete the object in the database
     * 
     * @param entity
     */
    void delete(T entity);

    /**
     * Delete the entire collection of objects
     * 
     * @param persistentCollection
     */
    void delete(Collection<T> persistentCollection);

    /**
     * Provide access to the Dao to the service.
     * 
     * @return
     */
    S getDao();

    /**
     * Sets the DAO (used by Spring)
     * 
     * @param dao
     */
    void setDao(S dao);

    /**
     * give the total count of Peristables of the generic type
     * 
     * @return
     */
    Number count();

    /**
     * Static abstract class that allows most of our DAOs to be very simple and only implement custom class needs. It also enables the @link GenericService to
     * handle a wide range of generic needs for most peristables
     * 
     * @author abrin
     * 
     * @param <E>
     * @param <D>
     */
    public static abstract class TypedDaoBase<E, D extends Dao<E>> implements ServiceInterface<E, D> {

        protected final Logger logger = LoggerFactory.getLogger(getClass());

        private D dao;

        @Transactional(readOnly = true)
        @Override
        public E find(Long id) {
            return dao.find(id);
        }

        @Transactional(readOnly = true)
        @Override
        public List<E> findAll() {
            return dao.findAll();
        }

        @Transactional(readOnly = true)
        @Override
        public List<E> findAll(List<Long> ids) {
            return dao.findAll(ids);
        }

        @Transactional(readOnly = true)
        @Override
        public List<E> findAll(int start, int numberOfrecords) {
            return dao.findAll(start, numberOfrecords);
        }

        @Transactional(readOnly = true)
        @Override
        public List<E> findAllSorted() {
            return dao.findAllSorted();
        }

        @Transactional(readOnly = true)
        @Override
        public List<E> findAllSorted(String orderByClause) {
            return dao.findAllSorted(orderByClause);
        }

        @Transactional(readOnly = true)
        @Override
        public List<E> findByEqCriteria(Map<String, ?> propertyMap) {
            return dao.findByEqCriteria(propertyMap);
        }

        @Transactional(readOnly = false)
        @Override
        public void save(List<E> persistentCollection) {
            if (persistentCollection == null) {
                return;
            }
            dao.save(persistentCollection);
        }

        @Transactional(readOnly = false)
        @Override
        public void save(E entity) {
            if (entity == null) {
                return;
            }
            dao.save(entity);
        }

        @Transactional(readOnly = false)
        @Override
        public void saveOrUpdate(E entity) {
            if (entity == null) {
                return;
            }
            dao.saveOrUpdate(entity);
        }

        @Transactional(readOnly = false)
        @Override
        public void update(E entity) {
            if (entity == null) {
                return;
            }
            dao.update(entity);
        }

        @Transactional(readOnly = false)
        @Override
        public E merge(E entity) {
            if (entity == null) {
                return null;
            }
            return dao.merge(entity);
        }

        @Transactional(readOnly = false)
        @Override
        public void delete(E entity) {
            if (entity == null) {
                return;
            }
            dao.delete(entity);
        }

        /**
         * Returns a new collection containing the results of merge()-ing every entity in the entity collection passed into this method.
         * 
         * @param collection
         * @return
         */
        @Transactional(readOnly = false)
        @Override
        public Collection<E> mergeAll(Collection<E> collection) {
            if (CollectionUtils.isEmpty(collection)) {
                return Collections.emptyList();
            }
            ArrayList<E> mergedEntities = new ArrayList<E>();
            for (E entity : collection) {
                mergedEntities.add(dao.merge(entity));
            }
            // collection.clear();
            // collection.addAll(mergedEntities);
            return mergedEntities;
        }

        @Transactional(readOnly = false)
        @Override
        public void saveOrUpdateAll(Collection<E> c) {
            if (CollectionUtils.isEmpty(c)) {
                return;
            }
            for (E o : c) {
                dao.saveOrUpdate(o);
            }
        }

        @Transactional(readOnly = false)
        @Override
        public void delete(Collection<E> persistentCollection) {
            if (CollectionUtils.isEmpty(persistentCollection)) {
                return;
            }
            dao.delete(persistentCollection);
        }

        @Transactional(readOnly = true)
        @Override
        public Number count() {
            return dao.count();
        }

        @Override
        public D getDao() {
            return dao;
        }

        @Autowired
        @Override
        public void setDao(D dao) {
            this.dao = dao;
        }

        protected Logger getLogger() {
            return logger;
        }
    }


}
