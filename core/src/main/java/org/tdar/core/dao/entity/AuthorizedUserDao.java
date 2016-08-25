package org.tdar.core.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.entity.UserPermissionCacheKey.CacheResult;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * 
 * Provides DAO access for Person entities, including a variety of methods for
 * looking up a Person in tDAR.
 * 
 * @author <a href='Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
public class AuthorizedUserDao extends Dao.HibernateBase<AuthorizedUser> {

    public AuthorizedUserDao() {
        super(AuthorizedUser.class);
    }

    public boolean isAllowedTo(TdarUser person, HasSubmitter resource, GeneralPermissions permission) {
        if (resource instanceof ResourceCollection) {
            return isAllowedTo(person, (ResourceCollection) resource, permission);
        } else {
            return isAllowedTo(person, (Resource) resource, permission);
        }
    }

    /**
     * @param person
     * @param resource
     * @param permission
     * @return
     */
    public boolean isAllowedTo(TdarUser person, Resource resource, GeneralPermissions permission) {
        Set<Long> ids = new HashSet<Long>();

        if (resource.isDeleted()) {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("not allowed to ... deleted: {}", resource.getId());
            }
            return false;
        }
        // if the user is the owner, don't go any further
        if (Objects.equals(resource.getSubmitter(), person)) {
            getLogger().trace("allowed to ... is submitter: {}", resource.getId());
            return true;
        }

        // // get all of the resource collections and their hierarchical tree, permissions are additive
        for (ResourceCollection collection : resource.getRightsBasedResourceCollections()) {
            ids.addAll(collection.getParentIds());
            ids.add(collection.getId());
        }
        getLogger().trace("allowed to rights collection ids: {}", ids);
        return isAllowedTo(person, permission, ids);
    }

    public boolean isAllowedTo(TdarUser person, ResourceCollection collection, GeneralPermissions permission) {
        if (collection.isPublic()) {
            return false;
        }
        if (Objects.equals(collection.getOwner(), person)) {
            return true;
        }
        List<Long> ids = new ArrayList<>(collection.getParentIds());
        ids.add(collection.getId());
        ids.addAll(collection.getParentIds());
        return isAllowedTo(person, permission, ids);
    }

    // basic weak hashMap to cache session and user permissions...
    private final WeakHashMap<Session, Map<UserPermissionCacheKey, Boolean>> userPermissionsCache = new WeakHashMap<>();

    /**
     * Used to simulate change in session or to wipe out cache. Our tests all run in the "same" session, so this is necessary, unfortunately
     */
    public void clearUserPermissionsCache() {
        getLogger().debug("clearing permissions cache");
        userPermissionsCache.clear();
    }

    public boolean isAllowedTo(TdarUser person, GeneralPermissions permission, Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids) || PersistableUtils.isNullOrTransient(person)) {
            return false;
        }

        CacheResult cacheResult = checkUserPermissionsCache(person, permission, ids, getCurrentSession());
        String cached = "";
        if (cacheResult == null || cacheResult == CacheResult.NOT_FOUND) {
            Query query = getCurrentSession().getNamedQuery(QUERY_IS_ALLOWED_TO_MANAGE);
            query.setLong("userId", person.getId());
            query.setInteger("effectivePermission", permission.getEffectivePermissions() - 1);
            query.setParameterList("resourceCollectionIds", ids);

            @SuppressWarnings("unchecked")
            List<Integer> result = query.list();
            getLogger().trace("results: {}", result);
            if (result.isEmpty() || result.get(0) != 1) {
                updateUserPermissionsCache(person, permission, ids, getCurrentSession(), CacheResult.FALSE);
                return false;
            }
            updateUserPermissionsCache(person, permission, ids, getCurrentSession(), CacheResult.TRUE);
            return true;
        } else {
            cached = "CACHED";
        }
        getLogger().trace("  [{} {}] checkUserPermissionCache: {}:{} [sesion: {}] {}", cacheResult, cached, person.getId(), permission,
                getCurrentSession().hashCode(), ids);
        return cacheResult.getBooleanEquivalent();
    }

    private CacheResult checkUserPermissionsCache(TdarUser person, GeneralPermissions permission, Collection<Long> collectionIds, Session currentSession) {
        UserPermissionCacheKey key = new UserPermissionCacheKey(person, permission, collectionIds);
        // could be enhanced to check each ID
        Map<UserPermissionCacheKey, Boolean> sessionMap = userPermissionsCache.get(currentSession);
        CacheResult result = CacheResult.NOT_FOUND;
        if (sessionMap != null) {
            Boolean res = sessionMap.get(key);
            if (res == Boolean.TRUE) {
                result = CacheResult.TRUE;
            } else if (res == Boolean.FALSE) {
                result = CacheResult.FALSE;
            }
        }
        getLogger().trace("  [{}] checkUserPermissionCache: {}:{} [sesion: {}] {}", result, person.getId(), permission, currentSession.hashCode(),
                collectionIds);
        return result;
    }

    private void updateUserPermissionsCache(TdarUser person, GeneralPermissions permission, Collection<Long> collectionIds, Session currentSession,
            CacheResult result) {
        Map<UserPermissionCacheKey, Boolean> sessionMap = userPermissionsCache.get(currentSession);
        if (sessionMap == null) {
            sessionMap = new HashMap<>();
            userPermissionsCache.put(currentSession, sessionMap);
        }
        /*
         * FIXME: this can be enhanced to add keys for: each collectionId?
         */
        getLogger().trace("{} ==> {}", permission, permission.getLesserAndEqualPermissions());
        for (GeneralPermissions subPermission : permission.getLesserAndEqualPermissions()) {
            UserPermissionCacheKey key = new UserPermissionCacheKey(person, subPermission, collectionIds);
            // if things are positive, then set lower permissions to positive too. Don't make negative assumptions.
            if (result != null && result == CacheResult.TRUE) {
                sessionMap.put(key, result.getBooleanEquivalent());
            }
        }
        UserPermissionCacheKey key = new UserPermissionCacheKey(person, permission, collectionIds);
        sessionMap.put(key, result.getBooleanEquivalent());
    }

    /**
     * @param person
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Resource> findSpaseEditableResources(TdarUser person, List<ResourceType> resourceTypes, boolean isAdmin, boolean sorted) {
        String namedQuery = sorted ? QUERY_SPARSE_EDITABLE_SORTED_RESOURCES : QUERY_SPARSE_EDITABLE_RESOURCES;
        Query query = getCurrentSession().getNamedQuery(namedQuery);// QUERY_PROJECT_EDITABLE
        query.setLong("userId", person.getId());
        query.setParameter("admin", isAdmin);
        query.setParameterList("resourceTypes", resourceTypes);
        query.setParameter("effectivePermission", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
        if (resourceTypes.size() == ResourceType.values().length) {
            query.setParameter("allResourceTypes", true);
        } else {
            query.setParameter("allResourceTypes", false);
        }
        query.setParameter("allStatuses", false);
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE, Status.DRAFT));
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findEditableResources(TdarUser person, List<ResourceType> resourceTypes, boolean isAdmin) {
        String namedQuery = QUERY_EDITABLE_RESOURCES;
        Query query = getCurrentSession().getNamedQuery(namedQuery);// QUERY_PROJECT_EDITABLE
        query.setLong("userId", person.getId());
        query.setParameter("admin", isAdmin);
        query.setParameterList("resourceTypes", resourceTypes);
        query.setParameter("effectivePermission", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
        if (resourceTypes.size() == ResourceType.values().length) {
            query.setParameter("allResourceTypes", true);
        } else {
            query.setParameter("allResourceTypes", false);
        }
        query.setParameter("allStatuses", false);
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE, Status.DRAFT));
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findEditableResources(TdarUser person, List<ResourceType> resourceTypes, boolean isAdmin, boolean sorted, List<Long> collectionIds_) {
        List<Long> collectionIds = collectionIds_;
        // Hey guess what - you always get sorted results.
        if (CollectionUtils.isEmpty(collectionIds)) {
            collectionIds = new ArrayList<>();
            collectionIds.add(null);
        }
        if (PersistableUtils.isNullOrTransient(person)) {
            return Collections.emptyList();
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED_SORTED);
        query.setInteger("effectivePermission", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
        query.setLong("userId", person.getId());
        query.setParameter("admin", isAdmin);
        query.setParameterList("resourceTypes", resourceTypes);
        if (resourceTypes.size() == ResourceType.values().length) {
            query.setParameter("allResourceTypes", true);
        } else {
            query.setParameter("allResourceTypes", false);
        }
        query.setParameter("allStatuses", false);
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE, Status.DRAFT));
        // query.setParameterList("rescolIds", collectionIds);
        return query.list();
    }

    public Set<Resource> findSparseEditableResources(TdarUser person, List<ResourceType> resourceTypes, boolean isAdmin) {
        return new HashSet<>(findSpaseEditableResources(person, resourceTypes, isAdmin, false));
    }

    public Set<Resource> findSparseEditableResources(TdarUser person, boolean isAdmin) {
        return findSparseEditableResources(person, Arrays.asList(ResourceType.values()), isAdmin);
    }

    /**
     * @param person
     * @return
     */
    public Set<Resource> findSparseTitleIdProjectListByPerson(TdarUser person, boolean isAdmin) {
        return findSparseEditableResources(person, Arrays.asList(ResourceType.PROJECT), isAdmin);
    }

    /**
     * @param user
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findAccessibleResourceCollections(TdarUser user) {
        Query query = getCurrentSession().getNamedQuery(QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO);// QUERY_PROJECT_EDITABLE
        query.setLong("userId", user.getId());
        return (List<ResourceCollection>) query.list();
    }

}
