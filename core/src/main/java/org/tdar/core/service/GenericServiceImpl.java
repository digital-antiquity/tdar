package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.CacheMode;
import org.hibernate.ScrollableResults;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.base.GenericDao;
import org.tdar.core.dao.base.GenericDao.FindOptions;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * 
 * This service is intended to handle most of the general service functions and is the superclass of any of the entity specific
 * serivces. Ideally, most calls would run through the generic service and Daos. Most of these classes take a persistent class
 * which is what the service actually passess to the Dao to actually run the query. Eg, Resource.class... any persistable class
 * should work here.
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@Service("genericService")
public class GenericServiceImpl implements GenericService {

    @Autowired
    @Qualifier("genericDao")
    private GenericDao genericDao;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findRandom(java.lang.Class, int)
     */
    @Override
    @Transactional
    public <T> List<T> findRandom(Class<T> persistentClass, int maxResults) {
        return genericDao.findRandom(persistentClass, maxResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findRandomIds(java.lang.Class, int)
     */
    @Override
    @Transactional
    public <T extends Persistable> List<Long> findRandomIds(Class<T> persistentClass, int maxResults) {
        return extractIds(findRandom(persistentClass, maxResults));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#setCacheModeForCurrentSession(org.hibernate.CacheMode)
     */
    @Override
    public void setCacheModeForCurrentSession(CacheMode mode) {
        genericDao.setCacheModeForCurrentSession(mode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllIds(java.lang.Class)
     */
    @Override
    @Transactional
    public <T extends Persistable> List<Long> findAllIds(Class<T> persistentClass) {
        return genericDao.findAllIds(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findIdRange(java.lang.Class, long, long, int)
     */
    @Override
    @Transactional
    public <T extends Persistable> List<T> findIdRange(Class<T> persistentClass, long firstId, long lastId, int maxResults) {
        return genericDao.findIdRange(persistentClass, firstId, lastId, maxResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#extractIds(java.util.Collection)
     */
    @Override
    public <T extends Persistable> List<Long> extractIds(Collection<T> persistables) {
        return PersistableUtils.extractIds(persistables);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#getCurrentSessionHashCode()
     */
    @Override
    @Transactional
    public Integer getCurrentSessionHashCode() {
        return genericDao.getCurrentSessionHashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findByExample(java.lang.Class, T, org.tdar.core.dao.base.GenericDao.FindOptions)
     */
    @Override
    @Transactional(readOnly = false)
    public <T> List<T> findByExample(Class<T> persistentClass, T entity, FindOptions options) {
        return findByExample(persistentClass, entity, null, options);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findByExample(java.lang.Class, T, java.util.List, org.tdar.core.dao.base.GenericDao.FindOptions)
     */
    @Override
    @Transactional(readOnly = false)
    public <T> List<T> findByExample(Class<T> persistentClass, T entity, List<String> ignoredProperties, FindOptions options) {
        return genericDao.findByExample(persistentClass, entity, ignoredProperties, options);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findByExamples(java.lang.Class, java.util.Collection, java.util.List,
     * org.tdar.core.dao.base.GenericDao.FindOptions)
     */
    @Override
    @Transactional(readOnly = false)
    public <T> Set<T> findByExamples(Class<T> classToCreate, Collection<T> incomingCollection, List<String> ignoreProperties, FindOptions options) {
        return genericDao.findByExamples(classToCreate, incomingCollection, ignoreProperties, options);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#loadFromSparseEntities(java.util.Collection, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <P extends Persistable> List<P> loadFromSparseEntities(Collection<P> incoming, Class<P> cls) {
        return genericDao.loadFromSparseEntities(incoming, cls);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#loadFromSparseEntity(P, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <P extends Persistable> P loadFromSparseEntity(P item, Class<P> cls) {
        return genericDao.loadFromSparseEntity(item, cls);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#populateSparseObjectsById(java.util.List, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <P extends Persistable> List<P> populateSparseObjectsById(List<P> sparseObjects, Class<?> cls) {
        if (!DeHydratable.class.isAssignableFrom(cls)) {
            throw new TdarRecoverableRuntimeException("error.not_implemented");
        }
        // get a unique set of Ids
        Map<Long, P> ids = PersistableUtils.createIdMap(sparseObjects);
        Set<Long> keySet = ids.keySet();
        // populate and put into a unique map
        @SuppressWarnings("unchecked")
        Map<Long, P> skeletons = PersistableUtils.createIdMap((List<P>) genericDao.populateSparseObjectsById(keySet, cls));

        List<P> toReturn = new ArrayList<P>();

        // iterate over original list and return in order with nulls
        for (P sparseObject : sparseObjects) {
            if (sparseObject == null) {
                toReturn.add(null);
            } else {
                toReturn.add(skeletons.get(sparseObject.getId()));
            }
        }

        return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findOrCreateById(java.lang.Class, E)
     */
    @Override
    @Transactional
    public <E extends Persistable> E findOrCreateById(Class<E> persistentClass, E entity) {
        return genericDao.findOrCreateById(persistentClass, entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAll(java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass) {
        return genericDao.findAll(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllWithCache(java.lang.Class)
     */
    @Override
    @Deprecated
    /**
     * Find all @link Persistable but use the cache object. If the cache is empty, populate it first
     * 
     * @deprecated This method no longer uses a cache. Consider using hibernate query caches instead.
     * @param persistentClass
     * @return
     */
    @Transactional(readOnly = true)
    public <T> List<T> findAllWithCache(Class<T> persistentClass) {
        return findAll(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAll(java.lang.Class, int)
     */
    @Override
    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass, int maxResults) {
        return genericDao.findAll(persistentClass, maxResults);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllWithStatus(java.lang.Class, org.tdar.core.bean.resource.Status)
     */
    @Override
    @Transactional(readOnly = true)
    public <F extends HasStatus> List<F> findAllWithStatus(Class<F> persistentClass, Status... statuses) {
        return genericDao.findAllWithStatus(persistentClass, statuses);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllSorted(java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <T> List<T> findAllSorted(Class<T> persistentClass) {
        return genericDao.findAllSorted(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllSorted(java.lang.Class, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public <T> List<T> findAllSorted(Class<T> persistentClass, String orderBy) {
        return genericDao.findAllSorted(persistentClass, orderBy);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllScrollable(java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <E> ScrollableResults findAllScrollable(Class<E> persistentClass) {
        return genericDao.findAllScrollable(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllActiveScrollable(java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <E> ScrollableResults findAllActiveScrollable(Class<E> persistentClass) {
        return genericDao.findAllActiveScrollable(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllScrollable(java.lang.Class, int)
     */
    @Override
    @Transactional(readOnly = true)
    public <E> ScrollableResults findAllScrollable(Class<E> persistentClass, int batchSize) {
        return genericDao.findAllScrollable(persistentClass, batchSize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#find(java.lang.Class, java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public <T> T find(Class<T> persistentClass, Long id) {
        return genericDao.find(persistentClass, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAll(java.lang.Class, java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass, List<Long> idlist) {
        return genericDao.findAll(persistentClass, idlist);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findByProperty(java.lang.Class, java.lang.String, java.lang.Object)
     */
    @Override
    @Transactional(readOnly = true)
    public <T> T findByProperty(Class<T> persistentClass, String propertyName, Object propertyValue) {
        return genericDao.findByProperty(persistentClass, propertyName, propertyValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#count(java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public <T> Number count(Class<T> persistentClass) {
        return genericDao.count(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#merge(T)
     */
    @Override
    @Transactional
    public <T> T merge(T entity) {
        return genericDao.merge(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#update(java.lang.Object)
     */
    @Override
    @Transactional
    public void update(Object obj) {
        // background on different transactional settings and the implications of them
        // http://www.ibm.com/developerworks/java/library/j-ts1.html
        genericDao.update(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#save(java.lang.Object)
     */
    @Override
    @Transactional
    public void save(Object obj) {
        enforceValidation(obj);
        genericDao.save(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#synchronize()
     */
    @Override
    @Deprecated
    public void synchronize() {
        genericDao.synchronize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#persist(java.lang.Object)
     */
    @Override
    @Transactional
    public void persist(Object entity) {
        enforceValidation(entity);
        genericDao.persist(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#save(java.util.Collection)
     */
    @Override
    @Transactional
    public void save(Collection<?> persistentCollection) {
        enforceValidation(persistentCollection);
        genericDao.save(persistentCollection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#detachFromSession(java.lang.Object)
     */
    @Override
    @Transactional
    public void detachFromSession(Object obj) {
        genericDao.detachFromSession(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#detachFromSessionAndWarn(java.lang.Object)
     */
    @Override
    @Transactional
    public void detachFromSessionAndWarn(Object obj) {
        genericDao.detachFromSessionAndWarn(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#saveOrUpdate(java.lang.Object)
     */
    @Override
    @Transactional
    public void saveOrUpdate(Object obj) {
        enforceValidation(obj);
        genericDao.saveOrUpdate(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#saveOrUpdate(java.util.Collection)
     */
    @Override
    @Transactional
    public void saveOrUpdate(Collection<?> persistentCollection) {
        enforceValidation(persistentCollection);
        genericDao.saveOrUpdate(persistentCollection);
    }

    /**
     * Pass the bean through the HibernateValidation API
     * 
     * @param obj
     */
    private void enforceValidation(Object obj) {
        if (obj instanceof Collection) {
            for (Object item : (Collection<?>) obj) {
                enforceValidation(item);
            }
        } else {
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<Object>> violations = validator.validate(obj);
            List<Object> errors = new ArrayList<>();
            errors.add(obj);
            if (violations.size() > 0) {
                logger.debug(String.format("violations: %s", violations));
                errors.add(violations);
                throw new TdarValidationException("genericService.object_not_valid_with_violations", errors);
            }
            if ((obj instanceof Validatable) && !((Validatable) obj).isValid()) {
                throw new TdarValidationException("genericService.object_not_valid", errors);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#saveOrUpdate(java.lang.Object)
     */
    @Override
    @Transactional
    public void saveOrUpdate(Object... obj) {
        for (Object _obj : obj) {
            enforceValidation(obj);
            saveOrUpdate(_obj);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#delete(java.lang.Object)
     */
    @Override
    @Transactional
    public void delete(Object obj) {
        genericDao.markWritableOnExistingSession(obj);
        genericDao.delete(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#delete(java.util.Collection)
     */
    @Override
    @Transactional
    public void delete(Collection<?> persistentEntities) {
        genericDao.delete(persistentEntities);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#refresh(java.lang.Object)
     */
    @Override
    @Transactional(readOnly = false)
    public void refresh(Object object) {
        genericDao.refresh(object);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#markReadOnly(java.lang.Object)
     */
    @Override
    public void markReadOnly(Object obj) {
        genericDao.markReadOnly(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#markWritable(O)
     */
    @Override
    public <O> O markWritable(O obj) {
        return genericDao.markWritable(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#markUpdatable(O)
     */
    @Override
    public <O> void markUpdatable(O obj) {
        genericDao.markUpdatable(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#markWritableOnExistingSession(O)
     */
    @Override
    public <O> O markWritableOnExistingSession(O obj) {
        return genericDao.markWritableOnExistingSession(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#clearCurrentSession()
     */
    @Override
    public void clearCurrentSession() {
        genericDao.clearCurrentSession();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#markReadOnly()
     */
    @Override
    public void markReadOnly() {
        genericDao.markReadOnly();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#markWritable()
     */
    @Override
    public void markWritable() {
        genericDao.markWritable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#isSessionOpen()
     */
    @Override
    public boolean isSessionOpen() {
        return genericDao.isSessionOpen();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#getSessionStatistics()
     */
    @Override
    @Transactional(readOnly = true)
    public Statistics getSessionStatistics() {
        return genericDao.getSessionStatistics();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#getActiveSessionCount()
     */
    @Override
    @Transactional(readOnly = true)
    public long getActiveSessionCount() {
        Statistics stats = genericDao.getSessionStatistics();
        return stats.getSessionOpenCount() - stats.getSessionCloseCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#deleteAll(java.lang.Class)
     */
    @Override
    @Transactional(readOnly = false)
    public <E extends Persistable> int deleteAll(Class<E> persistentClass) {
        return genericDao.deleteAll(persistentClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#forceDelete(java.lang.Object)
     */
    @Override
    @Transactional(readOnly = false)
    public void forceDelete(Object entity) {
        genericDao.forceDelete(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findActiveIds(java.lang.Class)
     */
    @Override
    @Transactional
    public List<Long> findActiveIds(Class<? extends HasStatus> class1) {
        return genericDao.findActiveIds(class1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllWithProfile(java.lang.Class, java.util.List, java.lang.String)
     */
    @Override
    @Transactional
    public <T> List<T> findAllWithProfile(Class<T> class1, List<Long> ids, String profileName) {
        return genericDao.findAllWithProfile(class1, ids, profileName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#sessionContains(T)
     */
    @Override
    public <T> boolean sessionContains(T entity) {
        return genericDao.sessionContains(entity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#findAllWithL2Cache(java.lang.Class)
     */
    @Override
    public <T> List<T> findAllWithL2Cache(Class<T> persistentClass) {
        return genericDao.findAllWithL2Cache(persistentClass, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#evictFromCache(org.tdar.core.bean.Persistable)
     */
    @Override
    public void evictFromCache(Persistable res) {
        genericDao.evictFromCache((Persistable) res);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.GenericService#countActive(java.lang.Class)
     */
    @Override
    @Transactional
    public <T> Number countActive(Class<? extends HasStatus> cls) {
        return genericDao.countActive(cls);
    }

}
