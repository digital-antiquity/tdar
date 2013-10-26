package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.tdar.core.dao.Dao;

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

    T find(Long id);

    List<T> findAll();

    List<T> findAll(int start, int numberOfRecords);

    List<T> findAll(List<Long> ids);

    List<T> findAllSorted();

    List<T> findAllSorted(String orderingClause);

    void save(List<?> persistentCollection);

    void save(Object entity);

    void saveOrUpdate(Object entity);

    void update(Object entity);

    void delete(Object entity);

    <C> void delete(Object parent, Collection<C> persistentCollection);

    void delete(Collection<?> persistentCollection);

    S getDao();

    void setDao(S dao);

    Number count();

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
        public List<E> findByEqCriteria(Map<String, ?> propertyMap) {
            return dao.findByEqCriteria(propertyMap);
        }

        @Transactional(readOnly = false)
        @Override
        public void save(List<?> persistentCollection) {
            if (persistentCollection == null)
                return;
            dao.save(persistentCollection);
        }

        @Transactional(readOnly = false)
        @Override
        public void save(Object entity) {
            if (entity == null)
                return;
            dao.save(entity);
        }

        @Transactional(readOnly = false)
        @Override
        public void saveOrUpdate(Object entity) {
            if (entity == null)
                return;
            dao.saveOrUpdate(entity);
        }

        @Transactional(readOnly = false)
        @Override
        public void update(Object entity) {
            if (entity == null)
                return;
            dao.update(entity);
        }

        @Transactional(readOnly = false)
        public E merge(E entity) {
            if (entity == null)
                return null;
            return dao.merge(entity);
        }

        @Transactional(readOnly = false)
        @Override
        public void delete(Object entity) {
            if (entity == null)
                return;
            dao.delete(entity);
        }

        /**
         * Returns a new collection containing the results of merge()-ing every entity in the entity collection passed into this method.
         * 
         * @param collection
         * @return
         */
        @Transactional(readOnly = false)
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
        public void saveOrUpdateAll(Collection<?> c) {
            if (CollectionUtils.isEmpty(c))
                return;
            for (Object o : c) {
                dao.saveOrUpdate(o);
            }
        }

        /**
         * Removes collection from the parent and deletes the orphans.
         */
        @Transactional(readOnly = false)
        @Override
        public <C> void delete(Object parent, Collection<C> persistentCollection) {
            // no-op if we try to delete a null collection.
            if (CollectionUtils.isEmpty(persistentCollection))
                return;
            ArrayList<C> orphans = new ArrayList<C>(persistentCollection);
            persistentCollection.clear();
            delete(orphans);
        }

        @Transactional(readOnly = false)
        @Override
        public void delete(Collection<?> persistentCollection) {
            if (CollectionUtils.isEmpty(persistentCollection))
                return;
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
