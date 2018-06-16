package org.tdar.core.dao.base;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    T find(Long id);

    <C> C find(Class<C> cls, Long id);

    /**
     * Returns all persistent T entities in the database.
     * 
     * @return a List<T> of all persistent T entities in the database.
     */
    List<T> findAll();

    List<T> findAll(int start, int numberOfRecords);

    /**
     * Returns a list of entities that match the set of ids passed in, i.e.,
     * FROM entity-class-name WHERE id in (:ids)
     * 
     * @param ids
     * @return
     */
    List<T> findAll(List<Long> ids);

    /**
     * Returns all persistent T entities in the database, sorted according to
     * some ordering criteria.
     * 
     * @return
     */
    List<T> findAllSorted();

    List<T> findAllSorted(String orderByClause);

    /**
     * Returns the total number of T entities in the database. Equivalent to
     * select count(*) from T.
     * 
     * @return
     */
    Number count();

    /**
     * Persists each entity in the collection to the database. Assumes that all
     * items in the collection are fresh entities.
     * 
     * @param persistentCollection
     */
    <E> void save(Collection<E> persistentCollection);

    /**
     * Saves a fresh entity to the database. Not recommended for updating
     * existing entities, use {@link #saveOrUpdate(Object)} or {@link #update(Object)} in that case.
     * 
     * @param o
     */
    <E> void save(E o);

    <E> void persist(E o);

    /**
     * Saves a fresh entity or updates an existing entity to the database.
     * 
     * @param o
     */
    <E> void saveOrUpdate(E o);

    /**
     * Merges a detached entity (see
     * http://docs.jboss.org/hibernate/stable/entitymanager
     * /reference/en/html/objectstate.html#d0e1318 for more details).
     */
    <E> E merge(E entity);

    /**
     * Updates an existing entity.
     * 
     * @param o
     */
    <E> void update(E o);

    /**
     * Deletes the given entity.
     * 
     * @param o
     */
    <E> void delete(E o);

    /**
     * Deletes each entity in persistentCollection.
     * 
     * @param persistentCollection
     */
    <E> void delete(Collection<E> persistentCollection);

    List<T> findByEqCriteria(Map<String, ?> criteria);

    List<T> findAllByProperty(String propertyName, Object propertyValue);

    List<T> findAllFromList(String propertyName, List<?> propertyValues);

    T findOrCreateById(T entity);
}
