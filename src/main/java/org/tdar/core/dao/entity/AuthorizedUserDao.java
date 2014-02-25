package org.tdar.core.dao.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;
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

    /**
     * @param person
     * @param resource
     * @param permission
     * @return
     */
    public boolean isAllowedTo(Person person, Resource resource, GeneralPermissions permission) {
        Set<Long> ids = new HashSet<Long>();

        if (resource.isDeleted()) {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("not allowed to ... deleted: {}", resource.getId());
            }
            return false;
        }
        // if the user is the owner, don't go any further
        if (ObjectUtils.equals(resource.getSubmitter(), person)) {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("allowed to ... is submitter: {}", resource.getId());
            }
            return true;
        }

        // get all of the resource collections and their hierarchical tree, permissions are additive
        for (ResourceCollection collection : resource.getRightsBasedResourceCollections()) {
            ids.addAll(collection.getParentIds());
            ids.add(collection.getId());
        }
        if (getLogger().isTraceEnabled()) {
            getLogger().debug("allowed to rights collection ids: {}", ids);
        }
        return isAllowedTo(person, permission, ids);
    }

    public boolean isAllowedTo(Person person, GeneralPermissions permission, ResourceCollection collection) {
        if (collection.isPublic()) {
            return false;
        }
        if (ObjectUtils.equals(collection.getOwner(), person)) {
            return true;
        }
        return isAllowedTo(person, permission, collection.getParentIds());
    }

    public boolean isAllowedTo(Person person, GeneralPermissions permission, Collection<Long> collectionIds) {
        if (collectionIds.isEmpty() || Persistable.Base.isNullOrTransient(person)) {
            return false;
        }

        Query query = getCurrentSession().getNamedQuery(QUERY_IS_ALLOWED_TO_MANAGE);
        query.setLong("userId", person.getId());
        query.setInteger("effectivePermission", permission.getEffectivePermissions() - 1);
        query.setParameterList("resourceCollectionIds", collectionIds);

        @SuppressWarnings("unchecked")
        List<Integer> result = query.list();
        getLogger().debug("results: {}", result);
        if (result.isEmpty() || result.get(0) != 1) {
            return false;
        }
        return true;
    }

    /**
     * @param person
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Resource> findSpaseEditableResources(Person person, List<ResourceType> resourceTypes, boolean isAdmin, boolean sorted) {
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
    public List<Resource> findEditableResources(Person person, List<ResourceType> resourceTypes, boolean isAdmin) {
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

    public List<Resource> findEditableResources(Person person, List<ResourceType> resourceTypes, boolean isAdmin, boolean sorted, List<Long> collectionIds) {
        //Hey guess what - you always get sorted results.
        if (CollectionUtils.isEmpty(collectionIds)) {
            collectionIds = new ArrayList<>();
            collectionIds.add(null);
        }
        if (Persistable.Base.isNullOrTransient(person)) {
            return Collections.EMPTY_LIST;
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
//        query.setParameterList("rescolIds", collectionIds);
        List results = query.list();

        return results;
    }

    
    public Set<Resource> findSparseEditableResources(Person person, List<ResourceType> resourceTypes, boolean isAdmin) {
        return new HashSet<>(findSpaseEditableResources(person, resourceTypes, isAdmin, false));
    }

    public Set<Resource> findSparseEditableResources(Person person, boolean isAdmin) {
        return findSparseEditableResources(person, Arrays.asList(ResourceType.values()), isAdmin);
    }

    /**
     * @param person
     * @return
     */
    public Set<Resource> findSparseTitleIdProjectListByPerson(Person person, boolean isAdmin) {
        return findSparseEditableResources(person, Arrays.asList(ResourceType.PROJECT), isAdmin);
    }

    /**
     * @param user
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findAccessibleResourceCollections(Person user) {
        Query query = getCurrentSession().getNamedQuery(QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO);// QUERY_PROJECT_EDITABLE
        query.setLong("userId", user.getId());
        return (List<ResourceCollection>) query.list();
    }

}
