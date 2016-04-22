package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.GenericDao.FindOptions;
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
public class GenericService {

    @Autowired
    @Qualifier("genericDao")
    private GenericDao genericDao;

    public static final int MINIMUM_VALID_ID = 0;
    private final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * Find a random set of items of the specified class
     * 
     * @param persistentClass
     * @param maxResults
     * @return
     */
    @Transactional
    public <T> List<T> findRandom(Class<T> persistentClass, int maxResults) {
        return genericDao.findRandom(persistentClass, maxResults);
    }

    /**
     * Find a random set of valid ids of the specified class
     * 
     * @param persistentClass
     * @param maxResults
     * @return
     */
    @Transactional
    public <T extends Persistable> List<Long> findRandomIds(Class<T> persistentClass, int maxResults) {
        return extractIds(findRandom(persistentClass, maxResults));
    }

    public void setCacheModeForCurrentSession(CacheMode mode) {
        genericDao.setCacheModeForCurrentSession(mode);
    }

    /**
     * Find all ids given a specified class
     * 
     * @param persistentClass
     * @return
     */
    @Transactional
    public <T extends Persistable> List<Long> findAllIds(Class<T> persistentClass) {
        return genericDao.findAllIds(persistentClass);
    }

    /**
     * Find a list of @link Persistable items that fit between the range of specified ids.
     * 
     * @param persistentClass
     * @param firstId
     * @param lastId
     * @param maxResults
     * @return
     */
    @Transactional
    public <T extends Persistable> List<T> findIdRange(Class<T> persistentClass, long firstId, long lastId, int maxResults) {
        return genericDao.findIdRange(persistentClass, firstId, lastId, maxResults);
    }

    /**
     * Extract out the ids for a set of @link Persistable entries
     * 
     * @param persistables
     * @return
     */
    public <T extends Persistable> List<Long> extractIds(Collection<T> persistables) {
        return PersistableUtils.extractIds(persistables);
    }

    /**
     * Useful for comparing data across sessions. This gets the session-id for hibernate effectively.
     * 
     * @return
     */
    @Transactional
    public Integer getCurrentSessionHashCode() {
        return genericDao.getCurrentSessionHashCode();
    }

    /**
     * Find a set of @link Persistable objects based on an example object and a set of FindOptions.
     * 
     * @param persistentClass
     * @param entity
     * @param options
     * @return
     */
    @Transactional(readOnly = false)
    public <T> List<T> findByExample(Class<T> persistentClass, T entity, FindOptions options) {
        return findByExample(persistentClass, entity, null, options);
    }

    /**
     * Find a set of @link Persistable objects based on an example object and a set of FindOptions and ignored properties.
     * 
     * @param persistentClass
     * @param entity
     * @param ignoredProperties
     * @param options
     * @return
     */
    @Transactional(readOnly = false)
    public <T> List<T> findByExample(Class<T> persistentClass, T entity, List<String> ignoredProperties, FindOptions options) {
        return genericDao.findByExample(persistentClass, entity, ignoredProperties, options);
    }

    /**
     * Find a set of @link Persistable objects based on an a set of example objects and a set of FindOptions and ignored properties.
     * 
     * @param classToCreate
     * @param incomingCollection
     * @param ignoreProperties
     * @param options
     * @return
     */
    @Transactional(readOnly = false)
    public <T> Set<T> findByExamples(Class<T> classToCreate, Collection<T> incomingCollection, List<String> ignoreProperties, FindOptions options) {
        return genericDao.findByExamples(classToCreate, incomingCollection, ignoreProperties, options);
    }

    /**
     * Returns a list of full hibernate proxies given a list of sparsely (only id field is required) persistable items.
     * 
     * @param incoming
     * @param cls
     * @return a list of full hibernate proxies given a list of sparsely (only id field is required) persistable items.
     */
    @Transactional(readOnly = true)
    public <P extends Persistable> List<P> loadFromSparseEntities(Collection<P> incoming, Class<P> cls) {
        return genericDao.loadFromSparseEntities(incoming, cls);
    }

    /**
     * Returns a full hibernate proxy given a sparse (only id field is required) persistable POJO.
     * 
     * @param item
     * @param cls
     * @return a full hibernate proxy given a sparse (only id field is required) persistable POJO.
     */
    @Transactional(readOnly = true)
    public <P extends Persistable> P loadFromSparseEntity(P item, Class<P> cls) {
        return genericDao.loadFromSparseEntity(item, cls);
    }

    /**
     * Given a set of sparse @link Persistable objects (ID only); hydrate all of them.
     * 
     * @param sparseObjects
     * @param cls
     * @return
     */
    @Transactional(readOnly = true)
    public <P extends Persistable> List<P> populateSparseObjectsById(List<P> sparseObjects, Class<?> cls) {
        if (!DeHydratable.class.isAssignableFrom(cls)) {
            throw new TdarRecoverableRuntimeException("error.not_implemented");
        }
        // get a unique set of Ids
        Map<Long, P> ids = PersistableUtils.createIdMap(sparseObjects);
//        logger.info("{}", ids);
        // populate and put into a unique map
        @SuppressWarnings("unchecked")
        Map<Long, P> skeletons = PersistableUtils.createIdMap((List<P>) genericDao.populateSparseObjectsById(ids.keySet(), cls));

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

    /**
     * Find an @link Persistable by ID and persist if not found
     * 
     * @param persistentClass
     * @param entity
     * @return
     */
    @Transactional
    public <E extends Persistable> E findOrCreateById(Class<E> persistentClass, E entity) {
        return genericDao.findOrCreateById(persistentClass, entity);
    }

    /**
     * Find all @link Persistable of class.
     * 
     * @param persistentClass
     * @return
     */
    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass) {
        return genericDao.findAll(persistentClass);
    }

    @Deprecated
    /**
     * Find all @link Persistable but use the cache object. If the cache is empty, populate it first
     * @deprecated  This method no longer uses a cache.  Consider using hibernate query caches instead.
     * @param persistentClass
     * @return
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public <T> List<T> findAllWithCache(Class<T> persistentClass) {
        return findAll(persistentClass);
    }

    /**
     * Find all @link Persistable objects limited to X number.
     * 
     * @param persistentClass
     * @param maxResults
     * @return
     */
    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass, int maxResults) {
        return genericDao.findAll(persistentClass, maxResults);
    }

    /**
     * Find all @link Persistable objects that implement @link HasStatus and limited to the specified statuses.
     * 
     * @param persistentClass
     * @param statuses
     * @return
     */
    @Transactional(readOnly = true)
    public <F extends HasStatus> List<F> findAllWithStatus(Class<F> persistentClass, Status... statuses) {
        return genericDao.findAllWithStatus(persistentClass, statuses);
    }

    /**
     * Find all @link Persistable objects, sorted by the defaultSortOption() (id).
     * 
     * @param persistentClass
     * @return
     */
    @Transactional(readOnly = true)
    public <T> List<T> findAllSorted(Class<T> persistentClass) {
        return genericDao.findAllSorted(persistentClass);
    }

    /**
     * Find all @link Persistable objects, sorted by the specified parameter.
     * 
     * @param persistentClass
     * @param orderBy
     * @return
     */
    @Transactional(readOnly = true)
    public <T> List<T> findAllSorted(Class<T> persistentClass, String orderBy) {
        return genericDao.findAllSorted(persistentClass, orderBy);
    }

    /**
     * Find all, but use a scrollable query result so not all are persisted in memory. Scrollable results will stream objects from the database as needed. Great
     * for giant queries.
     * 
     * @param persistentClass
     * @return
     */
    @Transactional(readOnly = true)
    public <E> ScrollableResults findAllScrollable(Class<E> persistentClass) {
        return genericDao.findAllScrollable(persistentClass);
    }

    @Transactional(readOnly = true)
    public <E> ScrollableResults findAllActiveScrollable(Class<E> persistentClass) {
        return genericDao.findAllActiveScrollable(persistentClass);
    }

    @Transactional(readOnly = true)
    public <E> ScrollableResults findAllScrollable(Class<E> persistentClass, int batchSize) {
        return genericDao.findAllScrollable(persistentClass, batchSize);
    }

    /**
     * Find a specific @link Persistable by id, and class.
     * 
     * @param persistentClass
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public <T> T find(Class<T> persistentClass, Long id) {
        return genericDao.find(persistentClass, id);
    }

    /**
     * Find all specified @link Persistable entries by their ids and class.
     * 
     * @param persistentClass
     * @param idlist
     * @return
     */
    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass, List<Long> idlist) {
        return genericDao.findAll(persistentClass, idlist);
    }

    /**
     * Find @link Persistable by class, property and value.
     * 
     * @param persistentClass
     * @param propertyName
     * @param propertyValue
     * @return
     */
    @Transactional(readOnly = true)
    public <T> T findByProperty(Class<T> persistentClass, String propertyName, Object propertyValue) {
        return genericDao.findByProperty(persistentClass, propertyName, propertyValue);
    }

    /**
     * Get total count of @link Persistable in the database
     * 
     * @param persistentClass
     * @return
     */
    @Transactional(readOnly = true)
    public <T> Number count(Class<T> persistentClass) {
        return genericDao.count(persistentClass);
    }

    /**
     * Merge the @link Persistable with the version in the DB (bring onto session)
     * 
     * @param entity
     * @return merged entity
     */
    @Transactional
    public <T> T merge(T entity) {
        return genericDao.merge(entity);
    }

    /**
     * Update the @link Persistable in the database with the version passed in
     * 
     * @param obj
     */
    @Transactional
    public void update(Object obj) {
        // background on different transactional settings and the implications of them
        // http://www.ibm.com/developerworks/java/library/j-ts1.html
        genericDao.update(obj);
    }

    /**
     * Save the @link Persistable in the database will throw an error if it already exists; validate prior to save.
     * 
     * @param obj
     */
    @Transactional
    public void save(Object obj) {
        enforceValidation(obj);
        genericDao.save(obj);
    }

    /**
     * Exposed mostly for tests, @Deprecated to suggest that it shouldn't be used. It forces Hibernate to synchronize or commit() transactions that are pending
     * to the database. In msot cases, hibernate should do this for us and choose the right time to do it. There are a few times, often with database managed
     * duplicate keys between multiple columns where we may need to help hibernate because it doesn't fully understand these.
     */
    @Deprecated
    public void synchronize() {
        genericDao.synchronize();
    }

    /**
     * Save the obeject in the database and validate before it's done
     * 
     * @param entity
     */
    @Transactional
    public void persist(Object entity) {
        enforceValidation(entity);
        genericDao.persist(entity);
    }

    /**
     * @see #save(Object)
     * 
     * @param persistentCollection
     */
    @Transactional
    public void save(Collection<?> persistentCollection) {
        enforceValidation(persistentCollection);
        genericDao.save(persistentCollection);
    }

    /**
     * Detach an object from the Hibernate session
     * 
     * @param obj
     */
    @Transactional
    public void detachFromSession(Object obj) {
        genericDao.detachFromSession(obj);
    }

    /**
     * Detach an object from the Hibernate session, and warn if the object is actually on the session when detached. This is used in a few cases where we've
     * created model beans that should not be persisted, and are double-checking that they stay off the session. (@link LookupController, for example)
     * 
     * @param obj
     */
    @Transactional
    public void detachFromSessionAndWarn(Object obj) {
        genericDao.detachFromSessionAndWarn(obj);
    }

    /**
     * Save or Update a @link Persistable based on whether it's been persisted before or not.
     * 
     * @param obj
     */
    @Transactional
    public void saveOrUpdate(Object obj) {
        enforceValidation(obj);
        genericDao.saveOrUpdate(obj);
    }

    /**
     * @see #saveOrUpdate(Object)
     * @param persistentCollection
     */
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

    /**
     * @see #saveOrUpdate(Object)
     * @param obj
     */
    @Transactional
    public void saveOrUpdate(Object... obj) {
        for (Object _obj : obj) {
            enforceValidation(obj);
            saveOrUpdate(_obj);
        }
    }

    /**
     * Delete the object
     * 
     * @param obj
     */
    @Transactional
    public void delete(Object obj) {
        genericDao.markWritableOnExistingSession(obj);
        genericDao.delete(obj);
    }

    /**
     * @see #delete(Object)
     * @param persistentEntities
     */
    @Transactional
    public void delete(Collection<?> persistentEntities) {
        genericDao.delete(persistentEntities);
    }

    /**
     * Refresh the bean with the data from the database (overwrite)
     * 
     * @param object
     */
    @Transactional(readOnly = false)
    public void refresh(Object object) {
        genericDao.refresh(object);
    }

    /**
     * Mark an object as "read only" so that changes are not persisted to the database (used in obfuscation, for example)..
     * 
     * @param obj
     */
    public void markReadOnly(Object obj) {
        genericDao.markReadOnly(obj);
    }

    /**
     * If the entire session is read-only (view layer). this allows us to persist an object that we know we want to change. E.g. a view statistic that we log.
     * 
     * @param obj
     * @return
     */
    public <O> O markWritable(O obj) {
        return genericDao.markWritable(obj);
    }

    public <O> void markUpdatable(O obj) {
        genericDao.markUpdatable(obj);
    }

    /**
     * Check to see if the object is already writable, if not, mark it as such
     * 
     * @param obj
     * @return
     */
    public <O> O markWritableOnExistingSession(O obj) {
        return genericDao.markWritableOnExistingSession(obj);
    }

    /**
     * This is dangerous. This evicts all transactions on the session and closes the session. It should be used sparingly, but a good example is when an
     * exception is thrown in the view layer.
     */
    public void clearCurrentSession() {
        genericDao.clearCurrentSession();
    }

    /**
     * Mark the entire session as read-only.
     * 
     */
    public void markReadOnly() {
        genericDao.markReadOnly();
    }

    /**
     * Mark the entire session as writable.
     * 
     */
    public void markWritable() {
        genericDao.markWritable();
    }

    /**
     * Checks whether the session is open. Note, if {@link #clearCurrentSession()} is called, this will still return true, though the session will be
     * effectively closed for most objects as they've been detached.
     * 
     * @return
     */
    public boolean isSessionOpen() {
        return genericDao.isSessionOpen();
    }

    /**
     * Get the hibernate statistics for the session, # of transactions, slow queries, etc.
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public Statistics getSessionStatistics() {
        return genericDao.getSessionStatistics();
    }

    /**
     * Get the active session count from Hibernate
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public long getActiveSessionCount() {
        Statistics stats = genericDao.getSessionStatistics();
        return stats.getSessionOpenCount() - stats.getSessionCloseCount();
    }

    /**
     * Deletes all entities from the given persistent class. Use with caution!
     * 
     * @param persistentClass
     * @return the number of deleted entities
     * @see org.tdar.core.dao.GenericDao#deleteAll(java.lang.Class)
     */
    @Transactional(readOnly = false)
    public <E extends Persistable> int deleteAll(Class<E> persistentClass) {
        return genericDao.deleteAll(persistentClass);
    }

    /**
     * {@link #delete(Object)} may be overwritten, and for things that implement @link HasStatus, simply marks them as deleted, forceDelete always deletes the
     * object from the database.
     * 
     * @param entity
     */
    @Transactional(readOnly = false)
    public void forceDelete(Object entity) {
        genericDao.forceDelete(entity);
    }

    /**
     * Find Ids of @link Persistable objects that have a @link Status of ACTIVE.
     * 
     * @param class1
     * @return
     */
    @Transactional
    public List<Long> findActiveIds(Class<? extends HasStatus> class1) {
        return genericDao.findActiveIds(class1);
    }

    /**
     * Find objects using a Hibernate FetchProfile.
     * 
     * @param class1
     * @param ids
     * @param profileName
     * @return
     */
    @Transactional
    public <T> List<T> findAllWithProfile(Class<T> class1, List<Long> ids, String profileName) {
        return genericDao.findAllWithProfile(class1, ids, profileName);
    }

    public <T> boolean sessionContains(T entity) {
        return genericDao.sessionContains(entity);
    }

    public <T> List<T> findAllWithL2Cache(Class<T> persistentClass) {
        return genericDao.findAllWithL2Cache(persistentClass, null);
    }

    public void evictFromCache(Persistable res) {
        genericDao.evictFromCache((Persistable) res);

    }

    @Transactional
    public <T> Number countActive(Class<? extends HasStatus> cls) {
        return genericDao.countActive(cls);
    }

}
