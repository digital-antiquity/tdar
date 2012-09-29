package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.GenericDao.FindOptions;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@Service
public class GenericService {

    private GenericDao genericDao;

    public static final int MINIMUM_VALID_ID = 0;
    protected Logger logger = Logger.getLogger(getClass());

    @Transactional
    public <T> List<T> findRandom(Class<T> persistentClass, int maxResults) {
        return genericDao.findRandom(persistentClass, maxResults);
    }

    public <T extends Persistable> List<Long> findRandomIds(Class<T> persistentClass, int maxResults) {
        return extractIds(findRandom(persistentClass, maxResults));
    }

    public <T extends Persistable> List<Long> extractIds(Collection<T> persistables) {
        List<Long> ids = new ArrayList<Long>();
        for (T persistable : persistables) {
            ids.add(persistable.getId());
        }
        return ids;
    }

    @Transactional(readOnly = false)
    public <T> List<T> findByExample(Class<T> persistentClass, T entity, FindOptions options) {
        return findByExample(persistentClass, entity, null, options);
    }

    @Transactional(readOnly = false)
    public <T> List<T> findByExample(Class<T> persistentClass, T entity, List<String> ignoredProperties, FindOptions options) {
        List<T> found = genericDao.findByExample(persistentClass, entity, ignoredProperties, options);
        return found;
    }

    @Transactional(readOnly = false)
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

    @Transactional
    public <E extends Persistable> E findOrCreateById(Class<E> persistentClass, E entity) {
        return genericDao.findOrCreateById(persistentClass, entity);
    }

    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass) {
        return genericDao.findAll(persistentClass);
    }

    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass, int start, int numberOfRecords) {
        return genericDao.findAll(persistentClass, start, numberOfRecords);
    }

    @Transactional(readOnly = true)
    public <T> List<T> findAllSorted(Class<T> persistentClass, String orderBy) {
        return genericDao.findAllSorted(persistentClass, orderBy);
    }

    @Transactional(readOnly = true)
    public <E> ScrollableResults findAllScrollable(Class<E> persistentClass) {
        return genericDao.findAllScrollable(persistentClass);
    }

    @Transactional(readOnly = true)
    public <T> T find(Class<T> persistentClass, Number id) {
        return (T) genericDao.find(persistentClass, id);
    }

    @Transactional(readOnly = true)
    public <T> Number count(Class<T> persistentClass) {
        return genericDao.count(persistentClass);
    }

    @Transactional
    public <T> T merge(T entity) {
        return (T) genericDao.merge(entity);
    }

    // background on different transactional settings and the implications of
    // them
    // http://www.ibm.com/developerworks/java/library/j-ts1.html
    @Transactional
    public void update(Object obj) {
        genericDao.update(obj);
    }

    @Transactional
    public void save(Object obj) {
        genericDao.save(obj);
    }

    @Deprecated
    public void synchronize() {
        genericDao.synchronize();
    }

    @Transactional
    public void persist(Object entity) {
        genericDao.persist(entity);
    }

    @Transactional
    public void save(Collection<?> persistentCollection) {
        genericDao.save(persistentCollection);
    }

    @Transactional
    public void detachFromSession(Object obj) {
        genericDao.detachFromSession(obj);
    }

    @Transactional
    public void saveOrUpdate(Object obj) {
        genericDao.saveOrUpdate(obj);
    }

    @Transactional
    public void delete(Object obj) {
        genericDao.delete(obj);
    }

    @Transactional
    public void delete(Collection<?> persistentEntities) {
        genericDao.delete(persistentEntities);
    }

    @Autowired
    @Qualifier("genericDao")
    public void setGenericDao(GenericDao genericDao) {
        this.genericDao = genericDao;
    }

    protected GenericDao getGenericDao() {
        return genericDao;
    }

}
