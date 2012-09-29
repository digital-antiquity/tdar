package org.tdar.core.dao.entity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.Dao;

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

        // if the user is the owner, don't go any further
        if (resource.getSubmitter().equals(person)) {
            return true;
        }

        // get all of the resource collections and their hierarchical tree, permissions are additive
        for (ResourceCollection collection : resource.getResourceCollections()) {
            ids.addAll(collection.getParentIdList());
        }

        // try to add project to get direct inherited permissions
        // FIXME: DISABLING PROJECT INHERITANCE OF RIGHTS: SEE Resource.getUsersWhoCanModify()
        /*
         * if (resource instanceof InformationResource) {
         * InformationResource informationResource = (InformationResource) resource;
         * for other part
         * if (informationResource.getProjectId() != null) {
         * ids.add(informationResource.getProjectId());
         * }
         * }
         */

        return isAllowedTo(person, permission, ids);
    }

    // private boolean userCanDoLookupForCollectionList(Person person, GeneralPermissions permission, Collection<Long> resourceCollectionIds) {
    // Query query = getCurrentSession().getNamedQuery(QUERY_IS_ALLOWED_TO_NEW);
    // query.setLong("userId", person.getId());
    // query.setInteger("effectivePermission", permission.getEffectivePermissions());
    // if (resourceCollectionIds.isEmpty()) {
    // return false;
    // }
    //
    // query.setParameterList("resourceCollectionIds", resourceCollectionIds);
    // @SuppressWarnings("unchecked")
    // List<Integer> result = query.list();
    // if (!result.isEmpty() && result.get(0) == 1) {
    // return true;
    // }
    // return false;
    // }

    // public boolean hasRightsToCollection(Person person, ResourceCollection collection, GeneralPermissions permission) {
    // if (person == null || collection == null || permission == null) {
    // return false;
    // }
    // return userCanDoLookupForCollectionList(person, permission, collection.getParentIdList());
    // }

    public boolean isAllowedTo(Person person, GeneralPermissions permission, ResourceCollection collection) {
        if (collection.getOwner() != null && collection.getOwner().equals(person)) {
            return true;
        }
        return isAllowedTo(person, permission, collection.getParentIdList());
    }

    public boolean isAllowedTo(Person person, GeneralPermissions permission, Collection<Long> collectionIds) {
        if (collectionIds.isEmpty() || person == null || person.getId() == -1) {
            return false;
        }

        Query query = getCurrentSession().getNamedQuery(QUERY_IS_ALLOWED_TO_MANAGE);
        query.setLong("userId", person.getId());
        query.setInteger("effectivePermission", permission.getEffectivePermissions() - 1);
        query.setParameterList("resourceCollectionIds", collectionIds);

        @SuppressWarnings("unchecked")
        List<Integer> result = query.list();
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
    public Set<Resource> findEditableResources(Person person, List<ResourceType> resourceTypes, boolean isAdmin) {
        Query query = getCurrentSession().getNamedQuery(QUERY_SPARSE_EDITABLE_RESOURCES);// QUERY_PROJECT_EDITABLE
        query.setLong("userId", person.getId());
        query.setParameter("admin", isAdmin);
        query.setParameterList("resourceTypes", resourceTypes);
        query.setParameter("effectivePermission", GeneralPermissions.MODIFY_RECORD.getEffectivePermissions() - 1);
        if (resourceTypes.size() == ResourceType.values().length) {
            query.setParameter("allResourceTypes", true);
        } else {
            query.setParameter("allResourceTypes", false);
        }
        query.setParameter("allStatuses", false);
        query.setParameterList("statuses", Arrays.asList(Status.ACTIVE, Status.DRAFT));
        return new HashSet<Resource>((List<Resource>) query.list());
    }

    public Set<Resource> findEditableResources(Person person, boolean isAdmin) {
        return findEditableResources(person, Arrays.asList(ResourceType.values()), isAdmin);
    }

    /**
     * @param person
     * @return
     */
    public Set<Resource> findSparseTitleIdProjectListByPerson(Person person, boolean isAdmin) {
        return findEditableResources(person, Arrays.asList(ResourceType.PROJECT), isAdmin);
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
