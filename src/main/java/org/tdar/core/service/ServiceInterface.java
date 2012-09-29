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

    public T find(Long id);

    public List<T> findAll();

    public List<T> findAll(int start, int numberOfRecords);

    public List<T> findAllSorted();

    public List<T> findAllSorted(String orderingClause);

    public void save(List<?> persistentCollection);

    public void save(Object entity);

    public void saveOrUpdate(Object entity);

    public void update(Object entity);

    public void delete(Object entity);

    public <C> void delete(Object parent, Collection<C> persistentCollection);

    public void delete(Collection<?> persistentCollection);

    public S getDao();

    public void setDao(S dao);

    public Number count();

    public static abstract class TypedDaoBase<E, D extends Dao<E>> implements ServiceInterface<E, D> {

        protected final Logger logger = LoggerFactory.getLogger(getClass());

        private D dao;

        @Transactional(readOnly = true)
        public E find(Long id) {
            return dao.find(id);
        }

        @Transactional(readOnly = true)
        public List<E> findAll() {
            return dao.findAll();
        }

        @Transactional(readOnly = true)
        public List<E> findAll(int start, int numberOfrecords) {
            return dao.findAll(start, numberOfrecords);
        }

        @Transactional(readOnly = true)
        public List<E> findAllSorted() {
            return dao.findAllSorted();
        }

        @Transactional(readOnly = true)
        public List<E> findAllSorted(String orderByClause) {
            return dao.findAllSorted(orderByClause);
        }

        @Transactional(readOnly = true)
        public List<E> findByEqCriteria(Map<String, ?> propertyMap) {
            return dao.findByEqCriteria(propertyMap);
        }

        @Transactional(readOnly = false)
        public void save(List<?> persistentCollection) {
            if (persistentCollection == null)
                return;
            dao.save(persistentCollection);
        }

        @Transactional(readOnly = false)
        public void save(Object entity) {
            if (entity == null)
                return;
            dao.save(entity);
        }

        @Transactional(readOnly = false)
        public void saveOrUpdate(Object entity) {
            if (entity == null)
                return;
            dao.saveOrUpdate(entity);
        }

        @Transactional(readOnly = false)
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
        public void delete(Object entity) {
            if (entity == null)
                return;
            dao.delete(entity);
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
        public <C> void delete(Object parent, Collection<C> persistentCollection) {
            // no-op if we try to delete a null collection.
            if (CollectionUtils.isEmpty(persistentCollection))
                return;
            ArrayList<C> orphans = new ArrayList<C>(persistentCollection);
            persistentCollection.clear();
            delete(orphans);
        }

        @Transactional(readOnly = false)
        public void delete(Collection<?> persistentCollection) {
            if (CollectionUtils.isEmpty(persistentCollection))
                return;
            dao.delete(persistentCollection);
        }

        @Transactional(readOnly = true)
        public Number count() {
            return dao.count();
        }

        public D getDao() {
            return dao;
        }

        @Autowired
        public void setDao(D dao) {
            this.dao = dao;
        }

        protected Logger getLogger() {
            return logger;
        }
    }

}
