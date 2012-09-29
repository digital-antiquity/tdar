package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ScrollableResults;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.DeHydratable;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.Validatable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.GenericDao.FindOptions;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

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
@Service
public class GenericService {

    @Autowired
    @Qualifier("genericDao")
    private GenericDao genericDao;

    public static final int MINIMUM_VALID_ID = 0;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Transactional
    public <T> List<T> findRandom(Class<T> persistentClass, int maxResults) {
        return genericDao.findRandom(persistentClass, maxResults);
    }

    public <T extends Persistable> List<Long> findRandomIds(Class<T> persistentClass, int maxResults) {
        return extractIds(findRandom(persistentClass, maxResults));
    }

    public <T extends Persistable> List<T> findIdRange(Class<T> persistentClass, long firstId, long lastId, int maxResults) {
        return genericDao.findIdRange(persistentClass, firstId, lastId, maxResults);
    }

    public <T extends Persistable> List<Long> extractIds(Collection<T> persistables) {
        return Persistable.Base.extractIds(persistables);
    }


    @Transactional(readOnly = false)
    public <T> List<T> findByExample(Class<T> persistentClass, T entity, FindOptions options) {
        return findByExample(persistentClass, entity, null, options);
    }

    @Transactional(readOnly = false)
    public <T> List<T> findByExample(Class<T> persistentClass, T entity, List<String> ignoredProperties, FindOptions options) {
        return genericDao.findByExample(persistentClass, entity, ignoredProperties, options);
    }

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

    /*
     * FIXME: I don't like the generics on this, but I'm not sure what else to do
     */
    @Transactional(readOnly = true)
    public <P extends Persistable> List<P> populateSparseObjectsById(List<P> sparseObjects, Class<?> cls) {
        if (!DeHydratable.class.isAssignableFrom(cls)) {
            throw new TdarRecoverableRuntimeException("not implemented");
        }
        // get a unique set of Ids
        Map<Long, P> ids = Persistable.Base.createIdMap(sparseObjects);
        logger.info("{}", ids);
        // populate and put into a unique map
        Map<Long, P> skeletons = Persistable.Base.createIdMap((List<P>) genericDao.populateSparseObjectsById(ids.keySet(), cls));

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

    @Transactional
    public <E extends Persistable> E findOrCreateById(Class<E> persistentClass, E entity) {
        return genericDao.findOrCreateById(persistentClass, entity);
    }

    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass) {
        return genericDao.findAll(persistentClass);
    }

    private Map<Class<?>, List<?>> cache = new ConcurrentHashMap<Class<?>, List<?>>();

    @Transactional(readOnly = true)
    public <T> List<T> findAllWithCache(Class<T> persistentClass) {
        if (!cache.containsKey(persistentClass)) {
            cache.put(persistentClass, findAll(persistentClass));
        }
        return (List<T>) cache.get(persistentClass);
    }

    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass, int start, int numberOfRecords) {
        return genericDao.findAll(persistentClass, start, numberOfRecords);
    }

    @Transactional(readOnly = true)
    public <T> List<T> findAllSorted(Class<T> persistentClass) {
        return genericDao.findAllSorted(persistentClass);
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
    public <T> T find(Class<T> persistentClass, Long id) {
        return (T) genericDao.find(persistentClass, id);
    }

    @Transactional(readOnly = true)
    public <T> List<T> findAll(Class<T> persistentClass, List<Long> idlist) {
        return genericDao.findAll(persistentClass, idlist);
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
        enforceValidation(obj);
        genericDao.save(obj);
    }

    @Deprecated
    public void synchronize() {
        genericDao.synchronize();
    }

    @Transactional
    public void persist(Object entity) {
        enforceValidation(entity);
        genericDao.persist(entity);
    }

    @Transactional
    public void save(Collection<?> persistentCollection) {
        enforceValidation(persistentCollection);
        genericDao.save(persistentCollection);
    }

    @Transactional
    public void detachFromSession(Object obj) {
        genericDao.detachFromSession(obj);
    }

    @Transactional
    public void saveOrUpdate(Object obj) {
        enforceValidation(obj);
        genericDao.saveOrUpdate(obj);
    }
    
    @Transactional
    public void saveOrUpdate(Collection<?> persistentCollection) {
        enforceValidation(persistentCollection);
        genericDao.saveOrUpdate(persistentCollection);
    }


    private void enforceValidation(Object obj) {
        if (obj instanceof Collection) {
            for (Object item : (Collection) obj) {
                enforceValidation(item);
            }
        } else {
            Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            Set<ConstraintViolation<Object>> violations = validator.validate(obj);
            if (violations.size() > 0) {
                logger.debug(String.format("violations: %s", violations));
                throw new TdarRecoverableRuntimeException(String.format("This object %s is not valid %s", obj, violations));
            }
            if (obj instanceof Validatable && !((Validatable) obj).isValid()) {
                throw new TdarRecoverableRuntimeException(String.format("This object %s is not valid", obj));
            }
        }
    }

    @Transactional
    public void saveOrUpdate(Object... obj) {
        for (Object _obj : obj) {
            enforceValidation(obj);
            saveOrUpdate(_obj);
        }
    }

    @Transactional
    public void delete(Object obj) {
        genericDao.delete(obj);
    }

    @Transactional
    public void delete(Collection<?> persistentEntities) {
        genericDao.delete(persistentEntities);
    }

    protected GenericDao getGenericDao() {
        return genericDao;
    }

    public void refresh(Object object) {
        genericDao.refresh(object);
    }

    public static String extractStringValue(Object val) {
        if (val == null) {
            return "";
        } else if (val instanceof HasLabel) {
            return ((HasLabel) val).getLabel();
        } else if (val instanceof Collection<?>) {
            Collection<?> values = (Collection<?>) val;
            StringBuilder sb = new StringBuilder();
            Iterator<?> iter = values.iterator();
            while (iter.hasNext()) {
                String val_ = extractStringValue(iter.next());
                if (StringUtils.isNotBlank(val_)) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(val_);
                }
            }
            return sb.toString();
        } else {
            String string = val.toString();
            if (StringUtils.isNotEmpty(string)) {
                return string;
            }
        }
        return "";
    }

    public void markReadOnly(Object obj) {
        genericDao.markReadOnly(obj);
    }

    public <O> O markWritable(O obj) {
        return genericDao.markWritable(obj);
    }

    public void clearCurrentSession() {
        genericDao.clearCurrentSession();
    }

    public void markReadOnly() {
        genericDao.markReadOnly();
    }

    public void markWritable() {
        genericDao.markWritable();
    }

    @Transactional(readOnly = true)
    public Statistics getSessionStatistics() {
        return genericDao.getSessionStatistics();
    }

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
    public <E extends Persistable> int deleteAll(Class<E> persistentClass) {
        return genericDao.deleteAll(persistentClass);
    }

    protected GenericDao getDao() {
        return genericDao;
    }

    public void forceDelete(Object entity) {
        genericDao.forceDelete(entity);
    }
}
