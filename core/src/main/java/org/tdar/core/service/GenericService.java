package org.tdar.core.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.ScrollableResults;
import org.hibernate.stat.Statistics;
import org.tdar.core.bean.HasStatus;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.base.GenericDao.FindOptions;

public interface GenericService {

    int MINIMUM_VALID_ID = 0;

    /**
     * Find a random set of items of the specified class
     * 
     * @param persistentClass
     * @param maxResults
     * @return
     */
    <T> List<T> findRandom(Class<T> persistentClass, int maxResults);

    /**
     * Find a random set of valid ids of the specified class
     * 
     * @param persistentClass
     * @param maxResults
     * @return
     */
    <T extends Persistable> List<Long> findRandomIds(Class<T> persistentClass, int maxResults);

    void setCacheModeForCurrentSession(CacheMode mode);

    /**
     * Find all ids given a specified class
     * 
     * @param persistentClass
     * @return
     */
    <T extends Persistable> List<Long> findAllIds(Class<T> persistentClass);

    /**
     * Find a list of @link Persistable items that fit between the range of specified ids.
     * 
     * @param persistentClass
     * @param firstId
     * @param lastId
     * @param maxResults
     * @return
     */
    <T extends Persistable> List<T> findIdRange(Class<T> persistentClass, long firstId, long lastId, int maxResults);

    /**
     * Extract out the ids for a set of @link Persistable entries
     * 
     * @param persistables
     * @return
     */
    <T extends Persistable> List<Long> extractIds(Collection<T> persistables);

    /**
     * Useful for comparing data across sessions. This gets the session-id for hibernate effectively.
     * 
     * @return
     */
    Integer getCurrentSessionHashCode();

    /**
     * Find a set of @link Persistable objects based on an example object and a set of FindOptions.
     * 
     * @param persistentClass
     * @param entity
     * @param options
     * @return
     */
    <T> List<T> findByExample(Class<T> persistentClass, T entity, FindOptions options);

    /**
     * Find a set of @link Persistable objects based on an example object and a set of FindOptions and ignored properties.
     * 
     * @param persistentClass
     * @param entity
     * @param ignoredProperties
     * @param options
     * @return
     */
    <T> List<T> findByExample(Class<T> persistentClass, T entity, List<String> ignoredProperties, FindOptions options);

    /**
     * Find a set of @link Persistable objects based on an a set of example objects and a set of FindOptions and ignored properties.
     * 
     * @param classToCreate
     * @param incomingCollection
     * @param ignoreProperties
     * @param options
     * @return
     */
    <T> Set<T> findByExamples(Class<T> classToCreate, Collection<T> incomingCollection, List<String> ignoreProperties, FindOptions options);

    /**
     * Returns a list of full hibernate proxies given a list of sparsely (only id field is required) persistable items.
     * 
     * @param incoming
     * @param cls
     * @return a list of full hibernate proxies given a list of sparsely (only id field is required) persistable items.
     */
    <P extends Persistable> List<P> loadFromSparseEntities(Collection<P> incoming, Class<P> cls);

    /**
     * Returns a full hibernate proxy given a sparse (only id field is required) persistable POJO.
     * 
     * @param item
     * @param cls
     * @return a full hibernate proxy given a sparse (only id field is required) persistable POJO.
     */
    <P extends Persistable> P loadFromSparseEntity(P item, Class<P> cls);

    /**
     * Given a set of sparse @link Persistable objects (ID only); hydrate all of them.
     * 
     * @param sparseObjects
     * @param cls
     * @return
     */
    <P extends Persistable> List<P> populateSparseObjectsById(List<P> sparseObjects, Class<?> cls);

    /**
     * Find an @link Persistable by ID and persist if not found
     * 
     * @param persistentClass
     * @param entity
     * @return
     */
    <E extends Persistable> E findOrCreateById(Class<E> persistentClass, E entity);

    /**
     * Find all @link Persistable of class.
     * 
     * @param persistentClass
     * @return
     */
    <T> List<T> findAll(Class<T> persistentClass);

    <T> List<T> findAllWithCache(Class<T> persistentClass);

    /**
     * Find all @link Persistable objects limited to X number.
     * 
     * @param persistentClass
     * @param maxResults
     * @return
     */
    <T> List<T> findAll(Class<T> persistentClass, int maxResults);

    /**
     * Find all @link Persistable objects that implement @link HasStatus and limited to the specified statuses.
     * 
     * @param persistentClass
     * @param statuses
     * @return
     */
    <F extends HasStatus> List<F> findAllWithStatus(Class<F> persistentClass, Status... statuses);

    /**
     * Find all @link Persistable objects, sorted by the defaultSortOption() (id).
     * 
     * @param persistentClass
     * @return
     */
    <T> List<T> findAllSorted(Class<T> persistentClass);

    /**
     * Find all @link Persistable objects, sorted by the specified parameter.
     * 
     * @param persistentClass
     * @param orderBy
     * @return
     */
    <T> List<T> findAllSorted(Class<T> persistentClass, String orderBy);

    /**
     * Find all, but use a scrollable query result so not all are persisted in memory. Scrollable results will stream objects from the database as needed. Great
     * for giant queries.
     * 
     * @param persistentClass
     * @return
     */
    <E> ScrollableResults findAllScrollable(Class<E> persistentClass);

    <E> ScrollableResults findAllActiveScrollable(Class<E> persistentClass);

    <E> ScrollableResults findAllScrollable(Class<E> persistentClass, int batchSize);

    /**
     * Find a specific @link Persistable by id, and class.
     * 
     * @param persistentClass
     * @param id
     * @return
     */
    <T> T find(Class<T> persistentClass, Long id);

    /**
     * Find all specified @link Persistable entries by their ids and class.
     * 
     * @param persistentClass
     * @param idlist
     * @return
     */
    <T> List<T> findAll(Class<T> persistentClass, List<Long> idlist);

    /**
     * Find @link Persistable by class, property and value.
     * 
     * @param persistentClass
     * @param propertyName
     * @param propertyValue
     * @return
     */
    <T> T findByProperty(Class<T> persistentClass, String propertyName, Object propertyValue);

    /**
     * Get total count of @link Persistable in the database
     * 
     * @param persistentClass
     * @return
     */
    <T> Number count(Class<T> persistentClass);

    /**
     * Merge the @link Persistable with the version in the DB (bring onto session)
     * 
     * @param entity
     * @return merged entity
     */
    <T> T merge(T entity);

    /**
     * Update the @link Persistable in the database with the version passed in
     * 
     * @param obj
     */
    void update(Object obj);

    /**
     * Save the @link Persistable in the database will throw an error if it already exists; validate prior to save.
     * 
     * @param obj
     */
    void save(Object obj);

    /**
     * Exposed mostly for tests, @Deprecated to suggest that it shouldn't be used. It forces Hibernate to synchronize or commit() transactions that are pending
     * to the database. In msot cases, hibernate should do this for us and choose the right time to do it. There are a few times, often with database managed
     * duplicate keys between multiple columns where we may need to help hibernate because it doesn't fully understand these.
     */
    void synchronize();

    /**
     * Save the obeject in the database and validate before it's done
     * 
     * @param entity
     */
    void persist(Object entity);

    /**
     * @see #save(Object)
     * 
     * @param persistentCollection
     */
    void save(Collection<?> persistentCollection);

    /**
     * Detach an object from the Hibernate session
     * 
     * @param obj
     */
    void detachFromSession(Object obj);

    /**
     * Detach an object from the Hibernate session, and warn if the object is actually on the session when detached. This is used in a few cases where we've
     * created model beans that should not be persisted, and are double-checking that they stay off the session. (@link LookupController, for example)
     * 
     * @param obj
     */
    void detachFromSessionAndWarn(Object obj);

    /**
     * Save or Update a @link Persistable based on whether it's been persisted before or not.
     * 
     * @param obj
     */
    void saveOrUpdate(Object obj);

    /**
     * @see #saveOrUpdate(Object)
     * @param persistentCollection
     */
    void saveOrUpdate(Collection<?> persistentCollection);

    /**
     * @see #saveOrUpdate(Object)
     * @param obj
     */
    void saveOrUpdate(Object... obj);

    /**
     * Delete the object
     * 
     * @param obj
     */
    void delete(Object obj);

    /**
     * @see #delete(Object)
     * @param persistentEntities
     */
    void delete(Collection<?> persistentEntities);

    /**
     * Refresh the bean with the data from the database (overwrite)
     * 
     * @param object
     */
    void refresh(Object object);

    /**
     * Mark an object as "read only" so that changes are not persisted to the database (used in obfuscation, for example)..
     * 
     * @param obj
     */
    void markReadOnly(Object obj);

    /**
     * If the entire session is read-only (view layer). this allows us to persist an object that we know we want to change. E.g. a view statistic that we log.
     * 
     * @param obj
     * @return
     */
    <O> O markWritable(O obj);

    <O> void markUpdatable(O obj);

    /**
     * Check to see if the object is already writable, if not, mark it as such
     * 
     * @param obj
     * @return
     */
    <O> O markWritableOnExistingSession(O obj);

    /**
     * This is dangerous. This evicts all transactions on the session and closes the session. It should be used sparingly, but a good example is when an
     * exception is thrown in the view layer.
     */
    void clearCurrentSession();

    /**
     * Mark the entire session as read-only.
     * 
     */
    void markReadOnly();

    /**
     * Mark the entire session as writable.
     * 
     */
    void markWritable();

    /**
     * Checks whether the session is open. Note, if {@link #clearCurrentSession()} is called, this will still return true, though the session will be
     * effectively closed for most objects as they've been detached.
     * 
     * @return
     */
    boolean isSessionOpen();

    /**
     * Get the hibernate statistics for the session, # of transactions, slow queries, etc.
     * 
     * @return
     */
    Statistics getSessionStatistics();

    /**
     * Get the active session count from Hibernate
     * 
     * @return
     */
    long getActiveSessionCount();

    /**
     * Deletes all entities from the given persistent class. Use with caution!
     * 
     * @param persistentClass
     * @return the number of deleted entities
     * @see org.tdar.core.dao.base.GenericDao#deleteAll(java.lang.Class)
     */
    <E extends Persistable> int deleteAll(Class<E> persistentClass);

    /**
     * {@link #delete(Object)} may be overwritten, and for things that implement @link HasStatus, simply marks them as deleted, forceDelete always deletes the
     * object from the database.
     * 
     * @param entity
     */
    void forceDelete(Object entity);

    /**
     * Find Ids of @link Persistable objects that have a @link Status of ACTIVE.
     * 
     * @param class1
     * @return
     */
    List<Long> findActiveIds(Class<? extends HasStatus> class1);

    /**
     * Find objects using a Hibernate FetchProfile.
     * 
     * @param class1
     * @param ids
     * @param profileName
     * @return
     */
    <T> List<T> findAllWithProfile(Class<T> class1, List<Long> ids, String profileName);

    <T> boolean sessionContains(T entity);

    <T> List<T> findAllWithL2Cache(Class<T> persistentClass);

    void evictFromCache(Persistable res);

    <T> Number countActive(Class<? extends HasStatus> cls);

}