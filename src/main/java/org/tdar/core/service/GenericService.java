package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Persistable;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.GenericDao.FindOptions;

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

    @Autowired @Qualifier("genericDao")
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

    public <T extends Persistable> List<T> findIdRange(Class<T> persistentClass, long firstId, long lastId, int maxResults) {
        return genericDao.findIdRange(persistentClass, firstId, lastId, maxResults);
    }

    public <T extends Persistable> List<Long> extractIds(Collection<T> persistables) {
        List<Long> ids = new ArrayList<Long>();
        for (T persistable : persistables) {
            ids.add(persistable.getId());
        }
        return ids;
    }

    public <T extends Persistable> List<Long> extractIds(Collection<T> persistables, int max) {
        List<Long> ids = new ArrayList<Long>();
        int count = 0;
        for (T persistable : persistables) {
            ids.add(persistable.getId());
            count++;
            if (count == max)
                break;
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

    /*
     * Takes a list of persistable items in and tries to get them back by looking up the id
     */
    @Transactional
    public <P extends Persistable> List<P> rehydrateSparseIdBeans(List<P> incoming, Class<P> cls) {
        List<P> toReturn = new ArrayList<P>();
        for (P item : incoming) {
            P tmp = rehydrateSparseIdBean(item, cls);
            if (tmp != null) {
                toReturn.add(tmp);
            }
        }
        return toReturn;
    }

    /*
     * takes a persistable in and tries to get it back by looking up the id
     */
    @Transactional
    public <P extends Persistable> P rehydrateSparseIdBean(P item, Class<P> cls) {
        if (item != null && item.getId() != null && item.getId() > 0) {
            return find(cls, item.getId());
        }
        return null;
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
    
    @Transactional(readOnly=true)
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
    public void saveOrUpdate(Object... obj) {
        for (Object _obj : obj) {
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
}
