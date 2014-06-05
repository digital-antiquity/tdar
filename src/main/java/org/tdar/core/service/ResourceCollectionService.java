/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;

/**
 * @author Adam Brin
 * 
 */

@Service
public class ResourceCollectionService extends ServiceInterface.TypedDaoBase<ResourceCollection, ResourceCollectionDao> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    /**
     * Reconcile an existing set of @link Resource entities on a @link ResourceCollection with a set of incomming @link Resource entities, remove unmatching
     * 
     * @param persistable
     * @param authenticatedUser
     * @param resources
     * @return
     */
    @Transactional
    public List<Resource> reconcileIncomingResourcesForCollection(ResourceCollection persistable, TdarUser authenticatedUser, List<Resource> resources) {
        Map<Long, Resource> incomingIdMap = Persistable.Base.createIdMap(resources);
        List<Resource> toRemove = new ArrayList<Resource>(); // not in incoming but in existing collection
        List<Resource> toEvaluate = new ArrayList<Resource>(resources); // in incoming but not existing
        List<Resource> ineligibleResources = new ArrayList<Resource>(); // existing resources the user doesn't have the rights to add
        for (Resource resource : persistable.getResources()) {
            if (!incomingIdMap.containsKey(resource.getId())) {
                resource.getResourceCollections().remove(persistable);
                toRemove.add(resource);
            }
            toEvaluate.remove(resource);
        }
        logger.info("incoming: {} existing: {} new: {}", resources.size(), persistable.getResources().size(), toEvaluate.size());
        // toEvaluate should retain the "new" resources to the collection

        // set the deleted resources aside first
        List<Resource> deletedResources = findAllResourcesWithStatus(persistable, Status.DELETED, Status.FLAGGED, Status.DUPLICATE,
                Status.FLAGGED_ACCOUNT_BALANCE);

        persistable.getResources().removeAll(toRemove);
        saveOrUpdate(persistable);
        List<Resource> rehydratedIncomingResources = getDao().loadFromSparseEntities(toEvaluate, Resource.class);
        logger.info("{} ", authenticatedUser);
        for (Resource resource : rehydratedIncomingResources) {
            if (!authenticationAndAuthorizationService.canEditResource(authenticatedUser, resource, GeneralPermissions.MODIFY_RECORD)) {
                ineligibleResources.add(resource);
            } else {
                resource.getResourceCollections().add(persistable);
            }
        }
        // remove all of the undesirable resources that that the user just tried to add
        rehydratedIncomingResources.removeAll(ineligibleResources);
        // getResourceCollectionService().findAllChildCollections(persistable, CollectionType.SHARED);
        persistable.getResources().addAll(rehydratedIncomingResources);

        // add all the deleted resources that were already in the colleciton
        persistable.getResources().addAll(deletedResources);
        saveOrUpdate(persistable);
        if (ineligibleResources.size() > 0) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_add", ineligibleResources);
        }
        return rehydratedIncomingResources;
    }

    /**
     * Reconcile @link AuthorizedUser entries on a @link ResourceCollection, save if told to.
     * 
     * @param resource
     * @param authorizedUsers
     * @param shouldSave
     */
    @Transactional
    public void saveAuthorizedUsersForResource(Resource resource, List<AuthorizedUser> authorizedUsers, boolean shouldSave, TdarUser actor) {
        logger.info("saving authorized users...");

        // if the incoming set is empty and the current has nothing ... NO-OP
        if (CollectionUtils.isEmpty(authorizedUsers)
                && (resource.getInternalResourceCollection() == null || resource.getInternalResourceCollection().getAuthorizedUsers().size() == 0)) {
            logger.debug("Skipping creation of internalResourceCollection -- no incomming, no current");
            return;
        }

        // find the internal collection for this resource
        ResourceCollection internalCollection = null;
        for (ResourceCollection collection : resource.getResourceCollections()) {
            if (collection.getType() == CollectionType.INTERNAL) {
                internalCollection = collection;
                if (shouldSave) {
                    internalCollection = getDao().merge(internalCollection);
                }
            }
        }

        // if none, create one
        if (internalCollection == null) {
            internalCollection = createInternalResourceCollectionWithResource(resource.getSubmitter(), resource, shouldSave);
        }
        // note: we assume here that the authorizedUser validation will happen in saveAuthorizedUsersForResourceCollection
        saveAuthorizedUsersForResourceCollection(resource, internalCollection, authorizedUsers, shouldSave, actor);
        // if (CollectionUtils.isNotEmpty(internalCollection.getAuthorizedUsers())) {
        // resource.getResourceCollections().remove(internalCollection);
        // getDao().delete(internalCollection);
        // }
    }

    private ResourceCollection createInternalResourceCollectionWithResource(TdarUser owner, Resource resource, boolean shouldSave) {
        ResourceCollection internalCollection;
        internalCollection = new ResourceCollection();
        internalCollection.setType(CollectionType.INTERNAL);
        internalCollection.setOwner(owner);
        internalCollection.markUpdated(owner);
        if (resource != null) {
            resource.getResourceCollections().add(internalCollection);
        }
        // internalCollection.getResources().add(resource); // WATCH -- may cause failure, if so, remove
        if (shouldSave) {
            getDao().saveOrUpdate(internalCollection);
            getDao().refresh(internalCollection);
        }
        return internalCollection;
    }

    /**
     * Get All @link AuthorizedUser entries for a @Link Resource based on all @link ResourceCollection entries.
     * 
     * @param resource
     * @param authenticatedUser
     * @return
     */
    public List<AuthorizedUser> getAuthorizedUsersForResource(Resource resource, TdarUser authenticatedUser) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<AuthorizedUser>();


        boolean canModify = authenticationAndAuthorizationService.canUploadFiles(authenticatedUser, resource);

        applyTransientEnabledPermission(authenticatedUser, resource.getInternalResourceCollection(), canModify);

        return authorizedUsers;
    }

    /**
     * Set the transient @link enabled boolean on a @link AuthorizedUser
     *
     * Generally speaking, we use the enabled property to indicate to the UI whether removing the authorizedUser from the authorizedUser is "safe". An operation
     * is "safe" if it doesnt remove the permissions that enabled the user to modify the resource collection in the first place.
     * 
     * @param authenticatedUser
     * @param authorizedUsers
     * @param canModify
     */
    private void applyTransientEnabledPermission(Person authenticatedUser, ResourceCollection resourceCollection, boolean canModify) {
        Set<AuthorizedUser> authorizedUsers = resourceCollection.getAuthorizedUsers();
        for (AuthorizedUser au : authorizedUsers) {
            //enable if:  permission is irrelevant (authuser is owner)
            //    or if:  user has modify permission but is not same as authuser
            au.setEnabled(
                    resourceCollection.getOwner().equals(au.getUser())
                            || (canModify && !au.getUser().equals(authenticatedUser)));
        }
    }

    /**
     * Find all collections that have no parent or have a parent that's hidden
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAllTopLevelCollections() {
        Set<ResourceCollection> resultSet = new HashSet<ResourceCollection>(getDao().findCollectionsOfParent(null, true, CollectionType.SHARED));
        resultSet.addAll(getDao().findPublicCollectionsWithHiddenParents());
        List<ResourceCollection> toReturn = new ArrayList<ResourceCollection>(resultSet);
        Collections.sort(toReturn, new Comparator<ResourceCollection>() {
            @Override
            public int compare(ResourceCollection o1, ResourceCollection o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        return toReturn;
    }

    /**
     * Find all direct child @link ResourceCollection entries of a @link ResourceCollection
     * 
     * @param id
     * @param public
     * @return
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findDirectChildCollections(Long id, Boolean visible, CollectionType... type) {
        return getDao().findCollectionsOfParent(id, visible, type);
    }

    /**
     * Delete the @link ResourceCollection and it's @link AuthorizedUser entries.
     * 
     * @param resourceCollection
     */
    @Override
    @Transactional
    public void delete(ResourceCollection resourceCollection) {
        getDao().delete(resourceCollection.getAuthorizedUsers());
        getDao().delete(resourceCollection);
    }

    /**
     * Save the incoming @link AuthorizedUser entries for the @link ResourceCollection resolving as needed, avoiding duplicates
     * 
     * @param resourceCollection
     * @param authorizedUsers
     * @param shouldSaveResource
     */
    @Transactional
    public void saveAuthorizedUsersForResourceCollection(HasSubmitter source, ResourceCollection resourceCollection, List<AuthorizedUser> authorizedUsers,
            boolean shouldSaveResource, TdarUser actor) {
        if (resourceCollection == null) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_save");
        }
        Set<AuthorizedUser> currentUsers = resourceCollection.getAuthorizedUsers();
        logger.debug("current users (start): {}", currentUsers);
        logger.debug("incoming authorized users (start): {}", authorizedUsers);

        // the request may have edited the an existing authUser's permissions, so clear out the old set and go w/ most recent set.
        currentUsers.clear();

        ResourceCollection.normalizeAuthorizedUsers(authorizedUsers);

        if (CollectionUtils.isNotEmpty(authorizedUsers)) {
            for (AuthorizedUser incomingUser : authorizedUsers) {
                if (incomingUser == null) {
                    continue;
                }
                addUserToCollection(shouldSaveResource, currentUsers, incomingUser, actor, resourceCollection, source);
            }
        }
        // CollectionUtils.removeAll(currentUsers, Collections.);
        logger.debug("users after save: {}", currentUsers);
        if (shouldSaveResource)
            getDao().saveOrUpdate(resourceCollection);
    }

    /**
     * Add a @link AuthorizedUser to the @link ResourceCollection if it's valid
     * 
     * @param shouldSaveResource
     * @param currentUsers
     * @param incomingUser
     * @param resourceCollection
     */
    private void addUserToCollection(boolean shouldSaveResource, Set<AuthorizedUser> currentUsers, AuthorizedUser incomingUser, TdarUser actor,
            ResourceCollection resourceCollection, HasSubmitter source) {
        if (Persistable.Base.isNotNullOrTransient(incomingUser.getUser())) {
            TdarUser user = getDao().find(TdarUser.class, incomingUser.getUser().getId());
            if (user != null) {
                // it's important to ensure that we replace the proxy user w/ the persistent user prior to calling isValid(), because isValid()
                // may evaluate fields that aren't set in the proxy object.
                incomingUser.setUser(user);
                if (!incomingUser.isValid()) {
                    return;
                }

                if (actor.equals(incomingUser.getUser()) && ObjectUtils.notEqual(source.getSubmitter(), actor)) {
                    if (!authenticationAndAuthorizationService.canDo(actor, source, InternalTdarRights.EDIT_ANYTHING, incomingUser.getGeneralPermission())) {
                        throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_add_user", Arrays.asList(incomingUser.getUser(),
                                incomingUser.getGeneralPermission()));
                    }
                    // find highest permission for actor
                    // check that permission is valid for actor to assign

                }
                currentUsers.add(incomingUser);
                if (shouldSaveResource)
                    getDao().saveOrUpdate(incomingUser);
            }
        }
    }

    /**
     * Find root @link ResourceCollection entries for a @link Person (shown on the dashboard).
     * 
     * @param person
     * @return
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findParentOwnerCollections(Person person) {
        return getDao().findParentOwnerCollections(person, Arrays.asList(CollectionType.SHARED));
    }

    /**
     * Find all @link ResourceCollection entries that are potential parents of the specified @link ResourceCollection
     * 
     * @param person
     * @param collection
     * @return
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findPotentialParentCollections(Person person, ResourceCollection collection) {
        List<ResourceCollection> potentialCollections = getDao().findParentOwnerCollections(person, Arrays.asList(CollectionType.SHARED));
        if (collection == null) {
            return potentialCollections;
        }
        Iterator<ResourceCollection> iterator = potentialCollections.iterator();
        while (iterator.hasNext()) {
            ResourceCollection parent = iterator.next();
            while (parent != null) {
                if (parent.equals(collection)) {
                    logger.trace("removing {} from parent list to prevent infinite loops", collection);
                    iterator.remove();
                    break;
                }
                parent = parent.getParent();
            }
        }
        return potentialCollections;
    }

    /**
     * Save the shared @link ResourceCollection entries for a @link Resource (add/remove as needed)
     * 
     * @param resource
     * @param incoming
     * @param current
     * @param authenticatedUser
     * @param shouldSave
     * @param errorHandling
     */
    @Transactional
    public void saveSharedResourceCollections(Resource resource, Collection<ResourceCollection> incoming, Set<ResourceCollection> current,
            TdarUser authenticatedUser, boolean shouldSave, ErrorHandling errorHandling) {
        Collection<ResourceCollection> incoming_ = incoming;
        logger.debug("incoming ResourceCollections: {} ({})", incoming, incoming.size());
        logger.debug("current ResourceCollections: {} ({})", current, current.size());
        if (incoming == current && !CollectionUtils.isEmpty(incoming)) {
            incoming_ = new ArrayList<ResourceCollection>();
            incoming_.addAll(incoming);
            current.clear();
        }

        CollectionUtils.filter(incoming_, NotNullPredicate.INSTANCE);

        List<ResourceCollection> toRemove = new ArrayList<ResourceCollection>();
        Iterator<ResourceCollection> iterator = current.iterator();
        while (iterator.hasNext()) {
            ResourceCollection resourceCollection = iterator.next();

            // retain internal collections, but remove any existing shared collections that don't exist in the incoming list of shared collections
            if (!incoming_.contains(resourceCollection) && resourceCollection.isShared()) {
                toRemove.add(resourceCollection);

                logger.trace("removing unmatched: {}", resourceCollection);
            }
        }

        logger.info("collections to remove: {}", toRemove);
        for (ResourceCollection collection : toRemove) {
            current.remove(collection);
            collection.getResources().remove(resource);
            resource.getResourceCollections().remove(collection);
        }

        for (ResourceCollection collection : incoming_) {
            addResourceCollectionToResource(resource, current, authenticatedUser, shouldSave, errorHandling, collection);
        }
        logger.debug("after save: {} ({})", current, current.size());

    }

    /**
     * Add a @Link ResourceCollection to a @link Resource, create as needed.
     * 
     * @param resource
     * @param current
     * @param authenticatedUser
     * @param shouldSave
     * @param errorHandling
     * @param collection
     */
    @Transactional(readOnly = false)
    public void addResourceCollectionToResource(Resource resource, Set<ResourceCollection> current, TdarUser authenticatedUser, boolean shouldSave,
            ErrorHandling errorHandling,
            ResourceCollection collection) {
        ResourceCollection collectionToAdd = null;
        logger.trace("{}", collection);
        if (collection.isTransient() && !collection.isInternal()) {
            ResourceCollection potential = getDao().findCollectionWithName(authenticatedUser, collection, GeneralPermissions.ADMINISTER_GROUP);
            if (potential != null) {
                collectionToAdd = potential;
            } else {
                collection.setOwner(authenticatedUser);
                collection.markUpdated(resource.getSubmitter());
                collection.setType(CollectionType.SHARED);
                if (collection.getSortBy() == null) {
                    collection.setSortBy(ResourceCollection.DEFAULT_SORT_OPTION);
                }
                collection.setVisible(true);
                collectionToAdd = collection;
            }
        } else if (collection.isInternal()) {
            collectionToAdd = collection;
        } else {
            collectionToAdd = find(collection.getId());
        }
        logger.trace("{}", collectionToAdd);

        if (collectionToAdd != null && collectionToAdd.isValid()) {
            if (Persistable.Base.isNotNullOrTransient(collectionToAdd) && !current.contains(collectionToAdd)
                    && !authenticationAndAuthorizationService.canEditCollection(authenticatedUser, collectionToAdd)) {
                throw new TdarRecoverableRuntimeException("resourceCollectionSerice.resource_collection_rights_error",
                        Arrays.asList(collectionToAdd.getTitle()));
            }
            collectionToAdd.markUpdated(authenticatedUser);
            if (collectionToAdd.isTransient()) { // && shouldSave abrin commented out for logging (3/6/2014)
                collectionToAdd.setChangesNeedToBeLogged(true);
            }

            // jtd the following line changes collectionToAdd's hashcode. all sets it belongs to are now corrupt.
            collectionToAdd.getResources().add(resource);
            resource.getResourceCollections().add(collectionToAdd);
        } else {
            if (errorHandling == ErrorHandling.VALIDATE_WITH_EXCEPTION) {
                String collectionName = collectionToAdd != null ? collectionToAdd.getName() : "null collection";
                throw new TdarRecoverableRuntimeException("resourceCollectionService.invalid", Arrays.asList(collectionName));
            }
        }
    }

    /**
     * Find all @link ResourceCollection entries (shared only)
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAllResourceCollections() {
        return getDao().findAllSharedResourceCollections();
    }

    /**
     * Recursively build the transient child collection fields of a specified resource collection, and return a list
     * containing the parent collection and all descendants
     * 
     * @param collection
     *            the parent collection
     * @param collectionType
     *            the type of collections to return (e.g. internal, shared, public)
     * @param authenticatedUser
     *            authuser instance to serve as a frame of reference when deriving the approprate
     *            value for the 'viewable' property of each child ResourceCollection in the returned
     *            list.
     * @return a list containing the provided 'parent' collection and any descendant collections (if any). Futhermore
     *         this method iteratively populates the transient children resource collection fields of the specified
     *         collection.
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> buildCollectionTreeForController(ResourceCollection collection, TdarUser authenticatedUser, CollectionType collectionType) {
        // List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
        // List<ResourceCollection> toEvaluate = new ArrayList<ResourceCollection>();
        // toEvaluate.add(collection);
        List<ResourceCollection> allChildren = getAllChildCollections(collection);
        // FIXME: iterate over all children to reconcile tree
        Iterator<ResourceCollection> iter = allChildren.iterator();
        while (iter.hasNext()) {
            ResourceCollection child = iter.next();
            authenticationAndAuthorizationService.applyTransientViewableFlag(child, authenticatedUser);
            ResourceCollection parent = child.getParent();
            if (parent != null) {
                parent.getTransientChildren().add(child);
                iter.remove();
            }
        }
        return allChildren;
    }

    /**
     * Recursively build the transient child collection fields of a specified resource collection, and return a list
     * containing the parent collection and all descendants.
     * 
     * @param collection
     *            the parent collection
     * @param collectionType
     *            the type of collections to return (e.g. internal, shared, public)
     * @return a list containing the provided 'parent' collection and any descendant collections (if any).
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAllChildCollectionsOnly(ResourceCollection collection, CollectionType collectionType) {
        return getDao().findAllChildCollectionsOnly(collection, collectionType);
    }

    /**
     * Return a collection of all (shared) collections that convey permissions to the specified user that are equal
     * or greater to the specified permission, regardless of whether the permissions are directly associated with
     * the collection or whether the collection's permissions are inherited from a parent collection.
     * 
     * Note: This method does not populate any of the transient properties of the collections in the returned set.
     * 
     * @param user
     * @param generalPermissions
     * @return
     */
    @Transactional(readOnly = true)
    public Set<ResourceCollection> findFlattenedCollections(Person user, GeneralPermissions generalPermissions) {
        return getDao().findFlattendCollections(user, generalPermissions);
    }

    /**
     * Find the root @link ResourceCollection of the specified collection.
     * 
     * @param node
     * @return
     */
    private ResourceCollection getRootResourceCollection(ResourceCollection node) {
        return node.getHierarchicalResourceCollections().get(0);
    };

    /**
     * Return the root resource collection of the provided resource collection. This method also populates the
     * transient children resource collection for every node in the tree.
     * 
     * @param anyNode
     * @return
     */
    @Transactional(readOnly = true)
    public ResourceCollection getFullyInitializedRootResourceCollection(ResourceCollection anyNode, TdarUser authenticatedUser) {
        ResourceCollection root = getRootResourceCollection(anyNode);
        buildCollectionTreeForController(getRootResourceCollection(anyNode), authenticatedUser, CollectionType.SHARED);
        return root;
    }

    /**
     * Find all @link ResourceCollection entries that were both public and shared.
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<Long> findAllPublicActiveCollectionIds() {
        return getDao().findAllPublicActiveCollectionIds();
    }

    /**
     * Find all @link ResourceCollection entries that have the specified @link Status.
     * 
     * @param persistable
     * @param statuses
     * @return
     */
    @Transactional(readOnly = true)
    public List<Resource> findAllResourcesWithStatus(ResourceCollection persistable, Status... statuses) {
        return getDao().findAllResourcesWithStatus(persistable, statuses);
    }

    /**
     * Get all @link AuthorizedUser entries for a collection (could be recursive up through parents)
     * 
     * @param persistable
     * @param authenticatedUser
     * @return
     */
    @Transactional(readOnly = true)
    public List<AuthorizedUser> getAuthorizedUsersForCollection(ResourceCollection persistable, TdarUser authenticatedUser) {
        List<AuthorizedUser> users = new ArrayList<>(persistable.getAuthorizedUsers());
        applyTransientEnabledPermission(authenticatedUser, persistable, authenticationAndAuthorizationService.canEditCollection(authenticatedUser, persistable));
        return users;
    }

    /**
     * For each @link ResourceCollection in the collection if it's in the list of id's, remove it. For a set of id's try and build a single tree and avoid
     * duplicate trees. This is used by the two tree's in the Dashboard (your's and shared).
     * 
     * @param collection
     * @param authenticatedUser
     * @param collectionIds
     */
    @Transactional(readOnly = true)
    public void reconcileCollectionTree(Collection<ResourceCollection> collection, TdarUser authenticatedUser, List<Long> collectionIds) {
        Iterator<ResourceCollection> iter = collection.iterator();
        while (iter.hasNext()) {
            ResourceCollection rc = iter.next();
            List<Long> list = new ArrayList<>(rc.getParentIds());
            list.remove(rc.getId());
            if (CollectionUtils.containsAny(collectionIds, list)) {
                iter.remove();
            }
            buildCollectionTreeForController(rc, authenticatedUser, ResourceCollection.CollectionType.SHARED);
        }
    }

    /**
     * Return a read-only list of sparse Resource objects that belong to a ResourceCollection with the specified ID
     * The only populated fields are Resource.id, Resource.title, and Resource.resourceType.
     * 
     * @param collectionId
     *            id of the ResourceCollection that contains the resources returned by this method
     * @return
     */
    @Transactional(readOnly = true)
    public List<Resource> findCollectionSparseResources(Long collectionId) {
        return getDao().findCollectionSparseResources(collectionId);
    }

    /**
     * Find aggregate view count for collection
     * 
     * @param persistable
     * @return
     */
    @Transactional(readOnly = true)
    public Long getCollectionViewCount(ResourceCollection persistable) {
        if (Persistable.Base.isNullOrTransient(persistable))
            return 0L;
        return getDao().getCollectionViewCount(persistable);
    }

    @Transactional(readOnly = false)
    public void updateCollectionParentTo(TdarUser authorizedUser, ResourceCollection persistable, ResourceCollection parent) {
        // find all children with me as a parent
        if (!authenticationAndAuthorizationService.canEditCollection(authorizedUser, persistable) ||
                parent != null && !authenticationAndAuthorizationService.canEditCollection(authorizedUser, parent)) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.user_does_not_have_permisssions");
        }

        List<ResourceCollection> children = getAllChildCollections(persistable);
        List<Long> oldParentIds = new ArrayList<>(persistable.getParentIds());

        persistable.setParent(parent);
        List<Long> parentIds = new ArrayList<>();
        if (Persistable.Base.isNotNullOrTransient(parent)) {
            if (CollectionUtils.isNotEmpty(parent.getParentIds())) {
                parentIds.addAll(parent.getParentIds());
            }
            parentIds.add(parent.getId());
        }
        persistable.getParentIds().clear();
        persistable.getParentIds().addAll(parentIds);
        for (ResourceCollection child : children) {
            child.getParentIds().removeAll(oldParentIds);
            child.getParentIds().addAll(parentIds);
            saveOrUpdate(child);
        }
        saveOrUpdate(persistable);

    }

    @Transactional(readOnly = true)
    public List<ResourceCollection> getAllChildCollections(ResourceCollection persistable) {
        return getDao().getAllChildCollections(persistable);
    }
}
