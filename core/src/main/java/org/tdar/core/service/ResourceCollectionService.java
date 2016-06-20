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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.collection.CollectionDisplayProperties;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.SimpleFileProcessingDao;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.TextProvider;

/**
 * @author Adam Brin
 * 
 */

@Service
public class ResourceCollectionService extends ServiceInterface.TypedDaoBase<ResourceCollection, ResourceCollectionDao> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient AuthorizationService authenticationAndAuthorizationService;
    @Autowired
    private transient SimpleFileProcessingDao simpleFileProcessingDao;
    @Autowired
    private ApplicationEventPublisher publisher;

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
            internalCollection = getDao().createInternalResourceCollectionWithResource(resource.getSubmitter(), resource, shouldSave);
        }
        // note: we assume here that the authorizedUser validation will happen in saveAuthorizedUsersForResourceCollection
        saveAuthorizedUsersForResourceCollection(resource, internalCollection, authorizedUsers, shouldSave, actor);
    }

    /**
     * Get All @link AuthorizedUser entries for a @Link Resource based on all @link ResourceCollection entries.
     * 
     * @param resource
     * @param authenticatedUser
     * @return
     */
    @Transactional(readOnly = true)
    public List<AuthorizedUser> getAuthorizedUsersForResource(Resource resource, TdarUser authenticatedUser) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<>();
        if (resource.getInternalResourceCollection() != null) {
            boolean canModify = authenticationAndAuthorizationService.canUploadFiles(authenticatedUser, resource);
            ResourceCollection resourceCollection = resource.getInternalResourceCollection();
            applyTransientEnabledPermission(authenticatedUser, resourceCollection, canModify);
            authorizedUsers.addAll(resourceCollection.getAuthorizedUsers());
        }
        return authorizedUsers;
    }

    /**
     * Set the transient @link enabled boolean on a @link AuthorizedUser
     * 
     * Generally speaking, we use the enabled property to indicate to the UI whether removing the authorizedUser from the authorizedUser is "safe". An operation
     * is "safe" if it doesnt remove the permissions that enabled the user to modify the resource collection in the first place.
     * 
     * @param authenticatedUser
     * @param resourceCollection
     * @param canModify
     */
    private void applyTransientEnabledPermission(Person authenticatedUser, ResourceCollection resourceCollection, boolean canModify) {
        Set<AuthorizedUser> authorizedUsers = resourceCollection.getAuthorizedUsers();
        for (AuthorizedUser au : authorizedUsers) {
            // enable if: permission is irrelevant (authuser is owner)
            // or if: user has modify permission but is not same as authuser
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
        Set<ResourceCollection> resultSet = new HashSet<ResourceCollection>(getDao().findCollectionsOfParent(null, false, CollectionType.SHARED));
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
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findDirectChildCollections(Long id, Boolean hidden, CollectionType... type) {
        return getDao().findCollectionsOfParent(id, hidden, type);
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
        if (resourceCollection instanceof RightsBasedResourceCollection) {
            for (Resource resource : ((RightsBasedResourceCollection)resourceCollection).getResources()) {
                publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
            }
        }
        getDao().delete(resourceCollection);
    }

    /**
     * Remove entries from provided list of AuthorizedUsers that contain duplicate User values. Retained
     * AuthorizedUsers will always have equal or greater permissions relative to the removed duplicate items.
     * 
     * @param authorizedUsers
     */
    @Transactional(readOnly = false)
    public void normalizeAuthorizedUsers(Collection<AuthorizedUser> authorizedUsers) {
        logger.trace("incoming " + authorizedUsers);
        Map<Long, AuthorizedUser> bestMap = new HashMap<>();
        Iterator<AuthorizedUser> iterator = authorizedUsers.iterator();
        while (iterator.hasNext()) {
            AuthorizedUser incoming = iterator.next();
            if ((incoming == null) || (incoming.getUser() == null)) {
                continue;
            }
            Long user = incoming.getUser().getId();

            AuthorizedUser existing = bestMap.get(user);
            logger.trace(incoming + " <==>" + existing);
            if (existing != null) {
                if (existing.getGeneralPermission().getEffectivePermissions() >= incoming.getGeneralPermission().getEffectivePermissions()) {
                    continue;
                }
            }
            bestMap.put(user, incoming);
        }

        authorizedUsers.clear();
        authorizedUsers.addAll(bestMap.values());
        logger.trace("outgoing" + authorizedUsers);

    }

    /**
     * Save the incoming @link AuthorizedUser entries for the @link ResourceCollection resolving as needed, avoiding duplicates
     * 
     * @param resourceCollection
     * @param incomingUsers
     * @param shouldSaveResource
     */
    @Transactional(readOnly = false)
    public void saveAuthorizedUsersForResourceCollection(HasSubmitter source, ResourceCollection resourceCollection, List<AuthorizedUser> incomingUsers,
            boolean shouldSaveResource, TdarUser actor) {
        if (resourceCollection == null) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_save");
        }
        logger.trace("------------------------------------------------------");
        logger.debug("current users (start): {}", resourceCollection.getAuthorizedUsers());
        logger.debug("incoming authorized users (start): {}", resourceCollection.getAuthorizedUsers());

        normalizeAuthorizedUsers(incomingUsers);
        CollectionRightsComparator comparator = new CollectionRightsComparator(getDao().getUsersFromDb(resourceCollection), incomingUsers);
        if (comparator.rightsDifferent()) {
            logger.debug("{} - {} ({})", actor, source.getSubmitter(), resourceCollection.getOwner());

            if (!authenticationAndAuthorizationService.canAdminiserUsersOn(source, actor)) {
                throw new TdarRecoverableRuntimeException("resourceCollectionService.insufficient_rights");
            }
                
            for (AuthorizedUser user : comparator.getAdditions()) {
                addUserToCollection(shouldSaveResource, resourceCollection.getAuthorizedUsers(), user, actor, resourceCollection, source);
            }

            resourceCollection.getAuthorizedUsers().removeAll(comparator.getDeletions());

            if (CollectionUtils.isNotEmpty(comparator.getChanges())) {
                Map<Long, AuthorizedUser> idMap2 = null;

                Map<Long, AuthorizedUser> idMap = PersistableUtils.createIdMap(resourceCollection.getAuthorizedUsers());
                for (AuthorizedUser user : comparator.getChanges()) {
                    AuthorizedUser actual = idMap.get(user.getId());
                    if (actual == null) {
                        // it's possible that the authorizedUserId was not passed back from the client
                        // if so, build a secondary map using the TdarUser (authorizedUser.user) id.
                        if (idMap2 == null) {
                            idMap2 = new HashMap<>();
                            for (AuthorizedUser au : resourceCollection.getAuthorizedUsers()) {
                                idMap2.put(au.getUser().getId(), au);
                            }
                        }

                        actual = idMap2.get(user.getUser().getId());
                        logger.debug("actual was null, now: {}", actual);
                    }
                    checkSelfEscalation(actor, user.getUser(), source, user.getGeneralPermission());
                    actual.setGeneralPermission(user.getGeneralPermission());
                    ;
                }
            }
        }
        comparator = null;

        logger.debug("users after save: {}", resourceCollection.getAuthorizedUsers());
        if (shouldSaveResource) {
            getDao().saveOrUpdate(resourceCollection);
        }
        logger.trace("------------------------------------------------------");
    }

    private void checkSelfEscalation(TdarUser actor, TdarUser transientUser, HasSubmitter source, GeneralPermissions generalPermission) {
        // specifically checking for rights escalation
        if (actor.equals(transientUser) && ObjectUtils.notEqual(source.getSubmitter(), actor)) {
            if (!authenticationAndAuthorizationService.canDo(actor, source, InternalTdarRights.EDIT_ANYTHING, generalPermission)) {
                throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_add_user", Arrays.asList(transientUser,
                        generalPermission));
            }
            // find highest permission for actor
            // check that permission is valid for actor to assign

        }

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
        TdarUser transientUser = incomingUser.getUser();
        if (PersistableUtils.isNotNullOrTransient(transientUser)) {
            TdarUser user = null;
            Long tranientUserId = transientUser.getId();
            try {
                user = getDao().find(TdarUser.class, tranientUserId);
            } catch (Exception e) {
                throw new TdarRecoverableRuntimeException("resourceCollectionService.user_does_not_exists", e, Arrays.asList(transientUser));
            }
            if (user == null) {
                throw new TdarRecoverableRuntimeException("resourceCollectionService.user_does_not_exists", Arrays.asList(transientUser));
            }

            // it's important to ensure that we replace the proxy user w/ the persistent user prior to calling isValid(), because isValid()
            // may evaluate fields that aren't set in the proxy object.
            incomingUser.setUser(user);
            if (!incomingUser.isValid()) {
                return;
            }

            checkSelfEscalation(actor, user, source, incomingUser.getGeneralPermission());
            currentUsers.add(incomingUser);
            if (shouldSaveResource) {
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
    public void saveSharedResourceCollections(Resource resource, Collection<SharedCollection> incoming, Set<ResourceCollection> current,
            TdarUser authenticatedUser, boolean shouldSave, ErrorHandling errorHandling) {

        logger.debug("incoming ResourceCollections: {} ({})", incoming, incoming.size());
        logger.debug("current ResourceCollections: {} ({})", current, current.size());

        ResourceCollectionSaveHelper helper = new ResourceCollectionSaveHelper(incoming, current, CollectionType.SHARED);
        logger.info("collections to remove: {}", helper.getToDelete());
        for (ResourceCollection collection : helper.getToDelete()) {
            current.remove(collection);
            ((RightsBasedResourceCollection)collection).getResources().remove(resource);
            resource.getResourceCollections().remove(collection);
        }

        for (ResourceCollection collection : helper.getToAdd()) {
            addResourceCollectionToResource(resource, current, authenticatedUser, shouldSave, errorHandling, (RightsBasedResourceCollection)collection);
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
    public void addResourceCollectionToResource(Resource resource, Set<? extends ResourceCollection> current, TdarUser authenticatedUser, boolean shouldSave,
            ErrorHandling errorHandling,
            ResourceCollection collection) {
        RightsBasedResourceCollection collectionToAdd = null;
        logger.trace("{}", collection);
        if (collection.isTransient() && collection instanceof SharedCollection) {
            boolean isAdmin = authenticationAndAuthorizationService.can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, authenticatedUser);
            RightsBasedResourceCollection potential = getDao().findCollectionWithName(authenticatedUser, isAdmin, collection);
            if (potential != null) {
                collectionToAdd = potential;
            } else {
                collection.setOwner(authenticatedUser);
                collection.markUpdated(resource.getSubmitter());
                if (collection.getSortBy() == null) {
                    collection.setSortBy(ResourceCollection.DEFAULT_SORT_OPTION);
                }
                collection.setHidden(false);
                collectionToAdd = (SharedCollection)collection;
                publisher.publishEvent(new TdarEvent(collection, EventType.CREATE_OR_UPDATE));
            }
        } else if (collection instanceof InternalCollection) {
            collectionToAdd = (InternalCollection)collection;
        }
        logger.trace("{}", collectionToAdd);

        if (collectionToAdd != null && collectionToAdd.isValid()) {
            if (PersistableUtils.isNotNullOrTransient(collectionToAdd) && !current.contains(collectionToAdd)
                    && !authenticationAndAuthorizationService.canEditCollection(authenticatedUser, collectionToAdd)) {
                throw new TdarRecoverableRuntimeException("resourceCollectionSerice.resource_collection_rights_error",
                        Arrays.asList(collectionToAdd.getTitle()));
            }
            collectionToAdd.markUpdated(authenticatedUser);
            if (collectionToAdd.isTransient()) { 
                collectionToAdd.setChangesNeedToBeLogged(true);
            }

            // jtd the following line changes collectionToAdd's hashcode. all sets it belongs to are now corrupt.
            collectionToAdd.getResources().add(resource);
            resource.getResourceCollections().add(collectionToAdd);
        } else {
            if (errorHandling == ErrorHandling.VALIDATE_WITH_EXCEPTION) {
                String collectionName = "null collection";
                if (collectionToAdd != null && StringUtils.isNotBlank(collectionToAdd.getName())) {
                    collectionName = collectionToAdd.getName();
                }
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
     * @return a list containing the other 'root' collections besides the one initially specified
     *         this method iteratively populates the transient children resource collection fields of the specified
     *         collection.
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> buildCollectionTreeForController(ResourceCollection collection, TdarUser authenticatedUser, CollectionType collectionType) {
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
        applyTransientEnabledPermission(authenticatedUser, persistable,
                authenticationAndAuthorizationService.canEditCollection(authenticatedUser, persistable));
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
            buildCollectionTreeForController(rc, authenticatedUser, CollectionType.SHARED);
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
        if (PersistableUtils.isNullOrTransient(persistable))
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
        if (PersistableUtils.isNotNullOrTransient(parent)) {
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

    @Transactional
    public void addUserToInternalCollection(Resource resource, TdarUser user, GeneralPermissions permission) {
        getDao().addToInternalCollection(resource, user, permission);
    }

    @Transactional(readOnly = true)
    public Set<ResourceCollection> getEffectiveResourceCollectionsForResource(Resource resource) {
        Set<ResourceCollection> tempSet = new HashSet<>();
        for (ResourceCollection collection : resource.getSharedResourceCollections()) {
            if (collection != null) {
                tempSet.addAll(collection.getHierarchicalResourceCollections());
            }
        }
        ResourceCollection internal = resource.getInternalResourceCollection();
        if ((internal != null) &&
                CollectionUtils.isNotEmpty(internal.getAuthorizedUsers())) {
            tempSet.add(internal);
        }

        Iterator<ResourceCollection> iter = tempSet.iterator();
        while (iter.hasNext()) {
            ResourceCollection next = iter.next();
            if (CollectionUtils.isEmpty(next.getAuthorizedUsers())) {
                iter.remove();
            }
        }

        return tempSet;
    }

    @Transactional(readOnly = false)
    public void reconcileIncomingResourcesForCollection(RightsBasedResourceCollection persistable, TdarUser authenticatedUser, List<Resource> resourcesToAdd,
            List<Resource> resourcesToRemove) {
        Set<Resource> resources = persistable.getResources();
        List<Resource> ineligibleToAdd = new ArrayList<Resource>(); // existing resources the user doesn't have the rights to add
        List<Resource> ineligibleToRemove = new ArrayList<Resource>(); // existing resources the user doesn't have the rights to add
        for (Resource resource : resourcesToAdd) {
            if (!persistable.isPublic()
                    && !authenticationAndAuthorizationService.canEditResource(authenticatedUser, resource, GeneralPermissions.MODIFY_RECORD)) {
                ineligibleToAdd.add(resource);
            } else {
                resource.getResourceCollections().add(persistable);
                resources.add(resource);
                publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
            }
        }

        for (Resource resource : resourcesToRemove) {
            if (!persistable.isPublic()
                    && !authenticationAndAuthorizationService.canEditResource(authenticatedUser, resource, GeneralPermissions.MODIFY_RECORD)) {
                ineligibleToAdd.add(resource);
            } else {
                resource.getResourceCollections().remove(persistable);
                publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
                resources.remove(resource);
            }
        }
        saveOrUpdate(persistable);
        getDao().saveOrUpdate(resourcesToAdd);
        getDao().saveOrUpdate(resourcesToRemove);
        if (ineligibleToAdd.size() > 0) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_add", ineligibleToAdd);
        }

        if (ineligibleToRemove.size() > 0) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_remove", ineligibleToRemove);
        }
    }

    @Transactional(readOnly = false)
    public void reconcileIncomingResourcesForCollectionWithoutRights(ResourceCollection persistable, TdarUser authenticatedUser, List<Resource> resourcesToAdd,
            List<Resource> resourcesToRemove) {
        Set<Resource> resources = persistable.getUnmanagedResources();

        // FIXME: check that there's no overlap with existing resources
        for (Resource resource : resourcesToAdd) {
            resource.getUnmanagedResourceCollections().add(persistable);
            resources.add(resource);
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }

        for (Resource resource : resourcesToRemove) {
            resource.getUnmanagedResourceCollections().remove(persistable);
            resources.remove(resource);
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }
        saveOrUpdate(persistable);
    }

    @Transactional(readOnly = false)
    public void deleteForController(SharedCollection persistable, String deletionReason, TdarUser authenticatedUser) {
        // should I do something special?
        for (Resource resource : persistable.getResources()) {
            resource.getResourceCollections().remove(persistable);
            getDao().saveOrUpdate(resource);
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }
        getDao().delete(persistable.getAuthorizedUsers());
        // FIXME: need to handle parents and children
        getDao().delete(persistable);
        publisher.publishEvent(new TdarEvent(persistable, EventType.DELETE));
        // getSearchIndexService().index(persistable.getResources().toArray(new Resource[0]));

    }

    @Transactional(readOnly = true)
    public DeleteIssue getDeletionIssues(TextProvider provider, ResourceCollection persistable) {
        List<ResourceCollection> findAllChildCollections = findDirectChildCollections(persistable.getId(), null, CollectionType.SHARED);
        if (CollectionUtils.isNotEmpty(findAllChildCollections)) {
            getLogger().info("we still have children: {}", findAllChildCollections);
            DeleteIssue issue = new DeleteIssue();
            issue.getRelatedItems().addAll(findAllChildCollections);
            issue.setIssue(provider.getText("resourceCollectionService.cannot_delete_collection"));
            return issue;
        }
        return null;
    }

    @Transactional(readOnly = false)
    public void saveCollectionForController(SharedCollection persistable, Long parentId, ResourceCollection parent, TdarUser authenticatedUser,
            List<AuthorizedUser> authorizedUsers, List<Resource> resourcesToAdd, List<Resource> resourcesToRemove, List<Resource> publicResourcesToAdd,
            List<Resource> publicResourcesToRemove, boolean shouldSaveResource,

            FileProxy fileProxy) {
        if (persistable == null) {
            throw new TdarRecoverableRuntimeException();
        }
        if (persistable.getType() == null) {
            persistable.setType(CollectionType.SHARED);
        }


        List<Resource> resourcesToRemove = getDao().findAll(Resource.class, toRemove);
        List<Resource> resourcesToAdd = getDao().findAll(Resource.class, toAdd);
        getLogger().debug("toAdd: {}", resourcesToAdd);
        getLogger().debug("toRemove: {}", resourcesToRemove);

        List<Resource> publicResourcesToRemove = getDao().findAll(Resource.class, publicToRemove);
        List<Resource> publicResourcesToAdd = getDao().findAll(Resource.class, publicToAdd);
        getLogger().debug("toAdd: {}", resourcesToAdd);
        getLogger().debug("toRemove: {}", resourcesToRemove);

        if (!Objects.equals(parentId, persistable.getParentId())) {
            updateCollectionParentTo(authenticatedUser, persistable, parent);
        }

        reconcileIncomingResourcesForCollection(persistable, authenticatedUser, resourcesToAdd, resourcesToRemove);
        reconcileIncomingResourcesForCollectionWithoutRights(persistable, authenticatedUser, publicResourcesToAdd, publicResourcesToRemove);
        saveAuthorizedUsersForResourceCollection(persistable, persistable, authorizedUsers, shouldSaveResource, authenticatedUser);
        if (persistable.getProperties() == null) {
            persistable.setProperties(new CollectionDisplayProperties());
            getDao().saveOrUpdate(persistable.getProperties());
        }
        simpleFileProcessingDao.processFileProxyForCreatorOrCollection(persistable.getProperties(), fileProxy);
        publisher.publishEvent(new TdarEvent(persistable, EventType.CREATE_OR_UPDATE));
    }

    @Transactional(readOnly = false)
    public void makeResourcesInCollectionActive(ResourceCollection col, TdarUser person) {
        if (!authenticationAndAuthorizationService.canEditCollection(person, col)) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.make_active_permissions");
        }
        getDao().makeResourceInCollectionActive(col, person);
    }

    @Transactional(readOnly = true)
    public ResourceCollection getRandomFeaturedCollection() {
        return getDao().findRandomFeaturedCollection();
    }

    @Transactional(readOnly = true)
    public SharedCollection getWhiteLabelCollectionForResource(Resource resource) {
        return getDao().getWhiteLabelCollectionForResource(resource);
    }

    @Transactional(readOnly = true)
    public List<Long> findCollectionIdsWithTimeLimitedAccess() {
        return getDao().findCollectionIdsWithTimeLimitedAccess();
    }

    /**
     * Return list of shared resources that match a specified name and are editable for a specified user. This
     * method does not evaluate inherited permissions.
     *
     * @param user
     *            user that the method will use when filtering results by access rights
     * @param name
     *            name that the method will use when filtering by exact name
     * @return
     */
    public List<SharedCollection> findCollectionsWithName(TdarUser user, String name) {
        boolean isAdmin = authenticationAndAuthorizationService.isEditor(user);
        return getDao().findCollectionsWithName(user, isAdmin, name);
    }

    @Transactional
    /**
     * Convert a resource collection into a persisted white-label collection with all default values.
     * Note that this has the effect of detaching the input collection from the session.
     * 
     * @param rc
     * @return
     */
    public SharedCollection convertToWhitelabelCollection(SharedCollection rc) {
        if (rc.isWhiteLabelCollection()) {
            return rc;
        }
        return getDao().convertToWhitelabelCollection(rc);
    }

    @Transactional
    /**
     * Detach the provided white-label collection and return a persisted resource collection object.
     *
     * @param wlc
     * @return
     */
    public ResourceCollection convertToResourceCollection(SharedCollection wlc) {
        return getDao().convertToResourceCollection(wlc);
    }

    @Transactional(readOnly=false)
    public void changeSubmitter(ResourceCollection collection, TdarUser submitter, TdarUser authenticatedUser) {
        getDao().changeSubmitter(collection, submitter, authenticatedUser);
    }

}
