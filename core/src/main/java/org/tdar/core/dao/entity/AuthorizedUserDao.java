package org.tdar.core.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.base.HibernateBase;
import org.tdar.core.dao.entity.UserPermissionCacheKey.CacheResult;
import org.tdar.core.dao.external.auth.InternalTdarRights;
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
public class AuthorizedUserDao extends HibernateBase<AuthorizedUser> {

    public AuthorizedUserDao() {
        super(AuthorizedUser.class);
    }

    public boolean isAllowedTo(TdarUser person, HasAuthorizedUsers resource, GeneralPermissions permission) {
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

        if (getLogger().isTraceEnabled()) {
            getLogger().debug("authUser: {}", resource.getAuthorizedUsers());
            getLogger().debug("  shares: {}", resource.getSharedCollections());
        }

        for (AuthorizedUser au : resource.getAuthorizedUsers()) {
            if (au.getUser().equals(person) && permission.getEffectivePermissions() <= au.getEffectiveGeneralPermission()) {
                return true;
            }
        }

        // // get all of the resource collections and their hierarchical tree, permissions are additive
        for (RightsBasedResourceCollection collection : resource.getRightsBasedResourceCollections()) {
            if (collection instanceof SharedCollection) {
                ids.addAll(((SharedCollection)collection).getParentIds());
            }
            ids.add(collection.getId());
        }
        getLogger().trace("allowed to rights collection ids: {}", ids);
        return isAllowedTo(person, permission, ids);
    }

    public boolean isAllowedTo(TdarUser person, ResourceCollection collection, GeneralPermissions permission) {

        List<Long> ids = new ArrayList<>();
        if (collection instanceof HierarchicalCollection) {
            HierarchicalCollection<?> hierarchicalCollection = (HierarchicalCollection<?>)collection;
            ids.addAll(hierarchicalCollection.getParentIds());
        }

        ids.add(collection.getId());
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
            Query<Integer> query = getCurrentSession().createNamedQuery(QUERY_IS_ALLOWED_TO_MANAGE, Integer.class);
            query.setParameter("userId", person.getId());
            query.setParameter("effectivePermission", permission.getEffectivePermissions() - 1);
            query.setParameter("resourceCollectionIds", ids);
            getLogger().trace("{} {} {} ", person.getId(), permission.getEffectivePermissions() - 1, ids);
            List<Integer> result = query.getResultList();
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
    public List<Resource> findSpaseEditableResources(TdarUser person, List<ResourceType> resourceTypes, boolean isAdmin, boolean sorted) {
        String namedQuery = sorted ? QUERY_SPARSE_EDITABLE_SORTED_RESOURCES : QUERY_SPARSE_EDITABLE_RESOURCES;
        Query<Resource> query = getCurrentSession().createNamedQuery(namedQuery, Resource.class);// QUERY_PROJECT_EDITABLE
        query.setParameter("userId", person.getId());
        query.setParameter("admin", isAdmin);
        query.setParameter("resourceTypes", resourceTypes);
        query.setParameter("effectivePermission", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
        if (resourceTypes.size() == ResourceType.values().length) {
            query.setParameter("allResourceTypes", true);
        } else {
            query.setParameter("allResourceTypes", false);
        }
        query.setParameter("allStatuses", false);
        query.setParameter("statuses", Arrays.asList(Status.ACTIVE, Status.DRAFT));
        return query.getResultList();
    }

    public List<Resource> findEditableResources(TdarUser person, List<ResourceType> resourceTypes, boolean isAdmin) {
        String namedQuery = QUERY_EDITABLE_RESOURCES;
        Query<Resource> query = getCurrentSession().createNamedQuery(namedQuery, Resource.class);// QUERY_PROJECT_EDITABLE
        query.setParameter("userId", person.getId());
        query.setParameter("admin", isAdmin);
        query.setParameter("resourceTypes", resourceTypes);
        query.setParameter("effectivePermission", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
        if (resourceTypes.size() == ResourceType.values().length) {
            query.setParameter("allResourceTypes", true);
        } else {
            query.setParameter("allResourceTypes", false);
        }
        query.setParameter("allStatuses", false);
        query.setParameter("statuses", Arrays.asList(Status.ACTIVE, Status.DRAFT));
        return query.getResultList();
    }

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
        Query<Resource> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_SPARSE_EDITABLE_SORTED_RESOURCES_INHERITED_SORTED, Resource.class);
        query.setParameter("effectivePermission", GeneralPermissions.MODIFY_METADATA.getEffectivePermissions() - 1);
        query.setParameter("userId", person.getId());
        query.setParameter("admin", isAdmin);
        query.setParameter("resourceTypes", resourceTypes);
        if (resourceTypes.size() == ResourceType.values().length) {
            query.setParameter("allResourceTypes", true);
        } else {
            query.setParameter("allResourceTypes", false);
        }
        query.setParameter("allStatuses", false);
        query.setParameter("statuses", Arrays.asList(Status.ACTIVE, Status.DRAFT));
        // query.setParameter("rescolIds", collectionIds);
        return query.getResultList();
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
    public List<ResourceCollection> findAccessibleResourceCollections(TdarUser user) {
        Query<ResourceCollection> query = getCurrentSession().createNamedQuery(QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO, ResourceCollection.class);// QUERY_PROJECT_EDITABLE
        query.setParameter("userId", user.getId());
        query.setParameter("perm", null);
        return (List<ResourceCollection>) query.getResultList();
    }

    /**
     * find all AuthorizedUsers on Persistable where permissions are greater than specified (or all if null)
     *  
     * @param actor
     * @param source
     * @param editAnything
     * @param generalPermission
     * @return
     */
    public List<AuthorizedUser> checkSelfEscalation(TdarUser actor, HasAuthorizedUsers source, InternalTdarRights editAnything, GeneralPermissions generalPermission) {
        String q = QUERY_RIGHTS_EXPIRY_COLLECTION;
        if (source instanceof Resource) {
            q = QUERY_RIGHTS_EXPIRY_RESOURCE;
        }
        Query<AuthorizedUser> query = getCurrentSession().createNamedQuery(q, AuthorizedUser.class);// QUERY_PROJECT_EDITABLE
        query.setParameter("userId", actor.getId());
        if (generalPermission == null) {
            query.setParameter("perm", null);
        } else {
        query.setParameter("perm", generalPermission.getEffectivePermissions() - 1);
        }
        query.setParameter("id", source.getId());
        return query.list();
    }

}
