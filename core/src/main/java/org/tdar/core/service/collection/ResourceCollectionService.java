/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.service.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.HasSubmitter;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.collection.CollectionDisplayProperties;
import org.tdar.core.bean.collection.CollectionRevisionLog;
import org.tdar.core.bean.collection.CustomizableCollection;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.TimedAccessRestriction;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.SimpleFileProcessingDao;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.exception.TdarAuthorizationException;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.transform.jsonld.SchemaOrgCollectionTransformer;
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
    private transient AuthorizationService authorizationService;
    @Autowired
    private transient SimpleFileProcessingDao simpleFileProcessingDao;
    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private SerializationService serializationService;
    @Autowired
    private EmailService emailService;

    /**
     * Reconcile @link AuthorizedUser entries on a @link ResourceCollection, save if told to.
     * 
     * @param resource
     * @param authorizedUsers
     * @param shouldSave
     */
    @Transactional
    public void saveAuthorizedUsersForResource(Resource resource, List<AuthorizedUser> authorizedUsers_, boolean shouldSave, TdarUser actor) {
        logger.info("saving authorized users...");

        List<AuthorizedUser> authorizedUsers = new ArrayList<>();
        // Filter out the completely empty AuthorizedUsers that might have been setup by Struts...
        if (!CollectionUtils.isEmpty(authorizedUsers_)) {
            Iterator<AuthorizedUser> iterator = authorizedUsers_.iterator();
            while (iterator.hasNext()) {
                AuthorizedUser user = iterator.next();
                if (user == null) {
                    continue;
                }
                if (user.getGeneralPermission() == null) {
                    continue;
                }

                TdarUser tdarUser = user.getUser();
                if (PersistableUtils.isNullOrTransient(tdarUser) || tdarUser.hasNoPersistableValues()) {
                    logger.debug("{}, {} ", user, tdarUser);
                    continue;
                }
                authorizedUsers.add(user);
            }

        }
        // if the incoming set is empty and the current has nothing ... NO-OP
        if (CollectionUtils.isEmpty(authorizedUsers)
                && (resource.getInternalResourceCollection() == null || resource.getInternalResourceCollection().getAuthorizedUsers().size() == 0)) {
            logger.debug("Skipping creation of internalResourceCollection -- no incomming, no current");
            return;
        }

        // find the internal collection for this resource
        ResourceCollection internalCollection = null;
        for (RightsBasedResourceCollection collection : resource.getRightsBasedResourceCollections()) {
            if (collection instanceof InternalCollection) {
                internalCollection = (InternalCollection) collection;
                if (shouldSave) {
                    internalCollection = getDao().merge(internalCollection);
                }
            }
        }

        // if none, create one
        if (internalCollection == null) {
            internalCollection = getDao().createInternalResourceCollectionForResource(resource.getSubmitter(), resource, shouldSave);
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
            boolean canModify = authorizationService.canUploadFiles(authenticatedUser, resource);
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
    public <C extends VisibleCollection> List<C> findAllTopLevelCollections() {
        Set<C> resultSet = new HashSet<>();
        resultSet.addAll((List<C>) getDao().findCollectionsOfParent(null, false, SharedCollection.class));
        resultSet.addAll((List<C>) getDao().findCollectionsOfParent(null, false, ListCollection.class));
        List<C> toReturn = new ArrayList<>(resultSet);
        Collections.sort(toReturn, new Comparator<C>() {
            @Override
            public int compare(C o1, C o2) {
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
    public <C extends HierarchicalCollection> List<C> findDirectChildCollections(Long id, Boolean hidden, Class<C> cls) {
        return getDao().findCollectionsOfParent(id, hidden, cls);
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
            for (Resource resource : ((RightsBasedResourceCollection) resourceCollection).getResources()) {
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

            if (!authorizationService.canAdminiserUsersOn(source, actor)) {
                throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
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
            if (!authorizationService.canDo(actor, source, InternalTdarRights.EDIT_ANYTHING, generalPermission)) {
                throw new TdarAuthorizationException("resourceCollectionService.could_not_add_user", Arrays.asList(transientUser,
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
    public <C extends HierarchicalCollection> List<C> findParentOwnerCollections(Person person, Class<C> cls) {
        return getDao().findParentOwnerCollections(person, cls);
    }

    /**
     * Find all @link ResourceCollection entries that are potential parents of the specified @link ResourceCollection
     * 
     * @param person
     * @param collection
     * @return
     */
    @Transactional(readOnly = true)
    public <C extends HierarchicalCollection> List<C> findPotentialParentCollections(Person person, C collection, Class<C> cls) {
        List<C> potentialCollections = getDao().findParentOwnerCollections(person, cls);
        if (collection == null) {
            return potentialCollections;
        }
        Iterator<C> iterator = potentialCollections.iterator();
        while (iterator.hasNext()) {
            ResourceCollection parent = iterator.next();
            if (parent instanceof SharedCollection) {
                while (parent != null) {
                    if (parent.equals(collection)) {
                        logger.trace("removing {} from parent list to prevent infinite loops", collection);
                        iterator.remove();
                        break;
                    }
                    parent = ((SharedCollection) parent).getParent();
                }
            }
        }
        return potentialCollections;
    }

    @Transactional(readOnly = false)
    public <C extends ResourceCollection> void saveResourceCollections(Resource resource, Collection<C> incoming, Set<C> current,
            TdarUser authenticatedUser, boolean shouldSave, ErrorHandling errorHandling, Class<C> cls) {

        logger.debug("incoming {}: {} ({})", cls.getSimpleName(), incoming, incoming.size());
        logger.debug(" current {}: {} ({})", cls.getSimpleName(), current, current.size());

        ResourceCollectionSaveHelper<C> helper = new ResourceCollectionSaveHelper<C>(incoming, current, cls);
        logger.info("collections to remove: {}", helper.getToDelete());
        for (C collection : helper.getToDelete()) {
            current.remove(collection);
            if (collection instanceof RightsBasedResourceCollection) {
                ((RightsBasedResourceCollection) collection).getResources().remove(resource);
                if (collection instanceof SharedCollection) {
                    resource.getSharedCollections().remove(collection);
                } else {
                    resource.getInternalCollections().remove(collection);
                }
            } else {
                ((ListCollection) collection).getUnmanagedResources().remove(resource);
                resource.getUnmanagedResourceCollections().remove((ListCollection) collection);
            }
        }

        for (C collection : helper.getToAdd()) {
            logger.debug("adding: {} ", collection);
            addResourceCollectionToResource(resource, current, authenticatedUser, shouldSave, errorHandling, collection, cls);
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
    public <C extends ResourceCollection> void addResourceCollectionToResource(Resource resource, Set<C> current, TdarUser authenticatedUser,
            boolean shouldSave,
            ErrorHandling errorHandling, C collection, Class<C> cls) {
        C collectionToAdd = null;
        logger.trace("addResourceCollectionToResource({}) {} - {}", cls, collection, resource);
        if (collection instanceof InternalCollection) {
            collectionToAdd = collection;
        } else {
            if (collection.isTransient()) {
                if (collection instanceof SharedCollection) {
                    collectionToAdd = (C) findOrCreateCollection(resource, authenticatedUser, (SharedCollection) collection, SharedCollection.class);
                }
                if (collection instanceof ListCollection) {
                    collectionToAdd = (C) findOrCreateCollection(resource, authenticatedUser, (ListCollection) collection, ListCollection.class);
                }
            } else {
                collectionToAdd = getDao().find(cls, collection.getId());
            }
        }
        logger.trace("{}, {}", collectionToAdd, collectionToAdd.isValid());
        String name = getName(collectionToAdd);
        if (collectionToAdd != null && collectionToAdd.isValid()) {
            if (PersistableUtils.isNotNullOrTransient(collectionToAdd) && !current.contains(collectionToAdd)
                    && !authorizationService.canAddToCollection(collectionToAdd, authenticatedUser)) {
                throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_error",
                        Arrays.asList(name));
            }
            collectionToAdd.markUpdated(authenticatedUser);
            if (collectionToAdd.isTransient()) {
                collectionToAdd.setChangesNeedToBeLogged(true);
            }

            // jtd the following line changes collectionToAdd's hashcode. all sets it belongs to are now corrupt.
            if (collectionToAdd instanceof RightsBasedResourceCollection) {
                addToCollection(resource, (RightsBasedResourceCollection) collectionToAdd);
            } else {
                ((ListCollection) collectionToAdd).getUnmanagedResources().add(resource);
                resource.getUnmanagedResourceCollections().add((ListCollection) collectionToAdd);
            }
        } else {
            logger.debug("collection is not valid: {}", collection);
            if (errorHandling == ErrorHandling.VALIDATE_WITH_EXCEPTION) {
                String collectionName = "null collection";
                if (collectionToAdd != null && StringUtils.isNotBlank(name)) {
                    collectionName = name;
                }
                throw new TdarRecoverableRuntimeException("resourceCollectionService.invalid", Arrays.asList(collectionName));
            }
        }
    }

    private <C extends ResourceCollection> String getName(C collectionToAdd) {
        String name = "Internal";
        if (collectionToAdd instanceof VisibleCollection) {
            name = ((VisibleCollection) collectionToAdd).getName();
        }
        return name;
    }

    private void addToCollection(Resource resource, RightsBasedResourceCollection collectionToAdd) {

        if (collectionToAdd instanceof InternalCollection) {
            resource.getInternalCollections().add((InternalCollection) collectionToAdd);
        }
        if (collectionToAdd instanceof SharedCollection) {
            resource.getSharedCollections().add((SharedCollection) collectionToAdd);
        }
        ((RightsBasedResourceCollection) collectionToAdd).getResources().add(resource);
    }

    private <C extends VisibleCollection> C findOrCreateCollection(Resource resource, TdarUser authenticatedUser, C collection, Class<C> cls) {
        boolean isAdmin = authorizationService.can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, authenticatedUser);
        C potential = getDao().findCollectionWithName(authenticatedUser, isAdmin, collection.getName(), cls);
        if (potential != null) {
            return potential;
        } else {
            collection.setOwner(authenticatedUser);
            collection.markUpdated(resource.getSubmitter());
            if (collection instanceof CustomizableCollection && ((CustomizableCollection) collection).getSortBy() == null) {
                ((CustomizableCollection) collection).setSortBy(ResourceCollection.DEFAULT_SORT_OPTION);
            }
            publisher.publishEvent(new TdarEvent(collection, EventType.CREATE_OR_UPDATE));
            return collection;
        }
    }

    /**
     * Find all @link ResourceCollection entries (shared only)
     * 
     * @return
     */
    @Transactional(readOnly = true)
    public List<SharedCollection> findAllResourceCollections() {
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
    public <C extends HierarchicalCollection<C>> List<C> buildCollectionTreeForController(C collection, TdarUser authenticatedUser, Class<C> cls) {
        List<C> allChildren = new ArrayList<>();
        allChildren.addAll(getAllChildCollections(collection, cls));
        // FIXME: iterate over all children to reconcile tree
        Iterator<C> iter = allChildren.iterator();
        while (iter.hasNext()) {
            C child = iter.next();
            authorizationService.applyTransientViewableFlag(child, authenticatedUser);
            C parent = child.getParent();
            if (parent != null) {
                parent.getTransientChildren().add(child);
                iter.remove();
            }
        }
        // second pass - sort all children lists (we add root into "allchildren" so we can sort the top level)
        allChildren.add(collection);
        allChildren.forEach(child -> {
            if (child != null && CollectionUtils.isNotEmpty(child.getTransientChildren())) {
                child.getTransientChildren().sort(VisibleCollection.TITLE_COMPARATOR);
                logger.trace("new list: {}", child.getTransientChildren());
            }
        });
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
    public <E extends HierarchicalCollection<E>> List<E> findAllChildCollectionsOnly(E collection, Class<E> cls) {
        return getDao().findAllChildCollectionsOnly(collection, cls);
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
    public <C extends HierarchicalCollection<?>> Set<C> findFlattenedCollections(Person user, GeneralPermissions generalPermissions, Class<C> cls) {
        return getDao().findFlattendCollections(user, generalPermissions, cls);
    }

    /**
     * Find the root @link ResourceCollection of the specified collection.
     * 
     * @param node
     * @return
     */
    private SharedCollection getRootResourceCollection(SharedCollection node) {
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
    public SharedCollection getFullyInitializedRootResourceCollection(SharedCollection anyNode, TdarUser authenticatedUser) {
        SharedCollection root = getRootResourceCollection(anyNode);
        buildCollectionTreeForController(getRootResourceCollection(anyNode), authenticatedUser, SharedCollection.class);
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
                authorizationService.canEditCollection(authenticatedUser, persistable));
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
    public <C extends HierarchicalCollection<C>> void reconcileCollectionTree(Collection<C> collection, TdarUser authenticatedUser, List<Long> collectionIds,
            Class<C> cls) {
        Iterator<C> iter = collection.iterator();
        while (iter.hasNext()) {
            C rc = iter.next();
            List<Long> list = new ArrayList<>(rc.getParentIds());
            list.remove(rc.getId());
            if (CollectionUtils.containsAny(collectionIds, list)) {
                iter.remove();
            }
            buildCollectionTreeForController(rc, authenticatedUser, cls);
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
    public <C extends HierarchicalCollection<C>> void updateCollectionParentTo(TdarUser authorizedUser, C persistable, C parent, Class<C> cls) {
        // find all children with me as a parent
        if (!authorizationService.canEditCollection(authorizedUser, persistable) ||
                parent != null && !authorizationService.canEditCollection(authorizedUser, parent)) {
            throw new TdarAuthorizationException("resourceCollectionService.user_does_not_have_permisssions");
        }

        List<C> children = getAllChildCollections(persistable, cls);
        List<Long> oldParentIds = new ArrayList<>(persistable.getParentIds());
        logger.debug("updating parent for {} from {} to {}", persistable.getId(), persistable.getParent(), parent);
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
        for (C child : children) {
            child.getParentIds().removeAll(oldParentIds);
            child.getParentIds().addAll(parentIds);
            saveOrUpdate(child);
        }
        saveOrUpdate(persistable);

    }

    @Transactional(readOnly = true)
    public <C extends HierarchicalCollection> List<C> getAllChildCollections(C persistable, Class<C> cls) {
        return getDao().getAllChildCollections(persistable, cls);
    }

    @Transactional
    public void addUserToInternalCollection(Resource resource, TdarUser user, GeneralPermissions permission) {
        getDao().addToInternalCollection(resource, user, permission);
    }

    @Transactional(readOnly = true)
    public Set<RightsBasedResourceCollection> getEffectiveSharesForResource(Resource resource) {
        Set<RightsBasedResourceCollection> tempSet = new HashSet<>();
        for (SharedCollection collection : resource.getSharedResourceCollections()) {
            if (collection != null) {
                tempSet.addAll(collection.getHierarchicalResourceCollections());
            }
        }
        InternalCollection internal = resource.getInternalResourceCollection();
        if ((internal != null) &&
                CollectionUtils.isNotEmpty(internal.getAuthorizedUsers())) {
            tempSet.add(internal);
        }

        // Iterator<RightsBasedResourceCollection> iter = tempSet.iterator();
        // while (iter.hasNext()) {
        // RightsBasedResourceCollection next = iter.next();
        // if (CollectionUtils.isEmpty(((ResourceCollection)next).getAuthorizedUsers())) {
        // iter.remove();
        // }
        // }

        return tempSet;
    }

    @Transactional(readOnly = true)
    public Set<ListCollection> getEffectiveResourceCollectionsForResource(Resource resource) {
        Set<ListCollection> tempSet = new HashSet<>();
        for (ListCollection collection : resource.getUnmanagedResourceCollections()) {
            if (collection != null) {
                tempSet.addAll(collection.getHierarchicalResourceCollections());
            }
        }

        Iterator<ListCollection> iter = tempSet.iterator();
        while (iter.hasNext()) {
            ListCollection next = iter.next();
            if (CollectionUtils.isEmpty(((ListCollection) next).getAuthorizedUsers())) {
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

        if (CollectionUtils.isNotEmpty(resourcesToAdd)) {
            if (!authorizationService.canAddToCollection((ResourceCollection) persistable, authenticatedUser)) {
                throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_error",
                        Arrays.asList(getName((ResourceCollection) persistable)));
            }
        }

        if (CollectionUtils.isNotEmpty(resourcesToRemove)) {
            if (!authorizationService.canRemoveFromCollection((ResourceCollection) persistable, authenticatedUser)) {
                throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_error",
                        Arrays.asList(getName((ResourceCollection) persistable)));
            }
        }

        for (Resource resource : resourcesToAdd) {
            if (!authorizationService.canEditResource(authenticatedUser, resource, GeneralPermissions.MODIFY_RECORD)) {
                ineligibleToAdd.add(resource);
            } else {
                addToCollection(resource, persistable);
                publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
            }
        }

        for (Resource resource : resourcesToRemove) {
            if (!authorizationService.canEditResource(authenticatedUser, resource, GeneralPermissions.MODIFY_RECORD)) {
                ineligibleToAdd.add(resource);
            } else {
                removeFromCollection(resource, (ResourceCollection) persistable);
                publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
                resources.remove(resource);
            }
        }
        getDao().saveOrUpdate(persistable);
        getDao().saveOrUpdate(resourcesToAdd);
        getDao().saveOrUpdate(resourcesToRemove);
        if (ineligibleToAdd.size() > 0) {
            throw new TdarAuthorizationException("resourceCollectionService.could_not_add", ineligibleToAdd);
        }

        if (ineligibleToRemove.size() > 0) {
            throw new TdarAuthorizationException("resourceCollectionService.could_not_remove", ineligibleToRemove);
        }
    }

    @Transactional(readOnly = false)
    public void removeResourceFromCollection(Resource resource, VisibleCollection collection, TdarUser authenticatedUser) {
        if (!authorizationService.canEditResource(authenticatedUser, resource, GeneralPermissions.MODIFY_RECORD) ||
                authorizationService.canRemoveFromCollection(collection, authenticatedUser)) {
            throw new TdarAuthorizationException("resourceCollectionService.could_not_remove");
        } else {
            removeFromCollection(resource, collection);
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }

    }

    @Transactional(readOnly = false)
    public void reconcileIncomingResourcesForCollectionWithoutRights(ListCollection persistable, TdarUser authenticatedUser, List<Resource> resourcesToAdd,
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
    public void deleteForController(VisibleCollection persistable, String deletionReason, TdarUser authenticatedUser) {
        // should I do something special?
        if (persistable instanceof SharedCollection) {
            for (Resource resource : ((RightsBasedResourceCollection) persistable).getResources()) {
                removeFromCollection(resource, persistable);
                getDao().saveOrUpdate(resource);
                publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
            }
        }
        if (persistable instanceof ListCollection) {
            ListCollection listCollection = (ListCollection) persistable;
            for (Resource resource : listCollection.getUnmanagedResources()) {
                removeFromCollection(resource, listCollection);
                getDao().saveOrUpdate(resource);
                publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
            }
        }

        getDao().delete(persistable.getAuthorizedUsers());
        getDao().deleteDownloadAuthorizations(persistable);
        // FIXME: need to handle parents and children
        String msg = String.format("%s deleted %s (%s);\n%s ", authenticatedUser.getProperName(), persistable.getTitle(), persistable.getId(), deletionReason);
        CollectionRevisionLog revision = new CollectionRevisionLog(msg, persistable, authenticatedUser, RevisionLogType.DELETE);
        getDao().saveOrUpdate(revision);

        getDao().delete(persistable);
        publisher.publishEvent(new TdarEvent(persistable, EventType.DELETE));
        // getSearchIndexService().index(persistable.getResources().toArray(new Resource[0]));

    }

    @Transactional(readOnly = true)
    public DeleteIssue getDeletionIssues(TextProvider provider, ResourceCollection persistable) {
        List<SharedCollection> findAllChildCollections = findDirectChildCollections(persistable.getId(), null, SharedCollection.class);
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
    public <C extends HierarchicalCollection> void saveCollectionForController(C persistable, Long parentId, C parent, TdarUser authenticatedUser,
            List<AuthorizedUser> authorizedUsers, List<Long> toAdd, List<Long> toRemove,
            boolean shouldSaveResource, FileProxy fileProxy, Class<C> cls, Long startTime) {

        if (persistable == null) {
            throw new TdarRecoverableRuntimeException();
        }

        if (!Objects.equals(parentId, persistable.getParentId())) {
            updateCollectionParentTo(authenticatedUser, persistable, parent, cls);
        }

        RevisionLogType type = RevisionLogType.CREATE;
        if (PersistableUtils.isNotTransient(persistable)) {
            type = RevisionLogType.EDIT;
        }

        List<Resource> resourcesToRemove = getDao().findAll(Resource.class, toRemove);
        List<Resource> resourcesToAdd = getDao().findAll(Resource.class, toAdd);
        getLogger().debug("toAdd: {}", resourcesToAdd);
        getLogger().debug("toRemove: {}", resourcesToRemove);

        if (persistable instanceof SharedCollection) {
            SharedCollection shared = (SharedCollection) persistable;
            reconcileIncomingResourcesForCollection(shared, authenticatedUser, resourcesToAdd, resourcesToRemove);
            saveAuthorizedUsersForResourceCollection(shared, shared, authorizedUsers, shouldSaveResource, authenticatedUser);
        }

        if (persistable instanceof ListCollection) {
            getLogger().debug("pToAdd: {}", resourcesToAdd);
            getLogger().debug("pToRemove: {}", resourcesToRemove);
            ListCollection list = (ListCollection) persistable;
            reconcileIncomingResourcesForCollectionWithoutRights(list, authenticatedUser, resourcesToAdd, resourcesToRemove);
            saveAuthorizedUsersForResourceCollection(list, list, authorizedUsers, shouldSaveResource, authenticatedUser);
        }

        if (persistable instanceof ListCollection) {
            ListCollection hasProps = (ListCollection) persistable;
            if (hasProps.getProperties() == null) {
                hasProps.setProperties(new CollectionDisplayProperties());
                getDao().saveOrUpdate(hasProps.getProperties());
            }
            simpleFileProcessingDao.processFileProxyForCreatorOrCollection(hasProps.getProperties(), fileProxy);
        }

        String msg = String.format("%s modified %s", authenticatedUser, persistable.getTitle());
        CollectionRevisionLog revision = new CollectionRevisionLog(msg, persistable, authenticatedUser, type);
        revision.setTimeBasedOnStart(startTime);
        getDao().saveOrUpdate(revision);
        publisher.publishEvent(new TdarEvent(persistable, EventType.CREATE_OR_UPDATE));
    }

    @Transactional(readOnly = false)
    public void makeResourcesInCollectionActive(ResourceCollection col, TdarUser person) {
        if (!authorizationService.canEditCollection(person, col)) {
            throw new TdarAuthorizationException("resourceCollectionService.make_active_permissions");
        }
        getDao().makeResourceInCollectionActive(col, person);
    }

    @Transactional(readOnly = true)
    public ResourceCollection getRandomFeaturedCollection() {
        return getDao().findRandomFeaturedCollection();
    }

    @Transactional(readOnly = true)
    public CustomizableCollection getWhiteLabelCollectionForResource(Resource resource) {
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
    public <C extends HierarchicalCollection> C findCollectionsWithName(TdarUser user, String name, Class<C> cls) {
        boolean isAdmin = authorizationService.isEditor(user);
        return getDao().findCollectionWithName(user, isAdmin, name, cls);
    }

    @Transactional
    /**
     * Convert a resource collection into a persisted white-label collection with all default values.
     * Note that this has the effect of detaching the input collection from the session.
     * 
     * @param rc
     * @return
     */
    public CustomizableCollection convertToWhitelabelCollection(CustomizableCollection rc) {
        if (rc.getProperties() != null && rc.getProperties().isWhitelabel()) {
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
    public CustomizableCollection convertToResourceCollection(CustomizableCollection wlc) {
        return getDao().convertToResourceCollection(wlc);
    }

    @Transactional(readOnly = false)
    public void changeSubmitter(ResourceCollection collection, TdarUser submitter, TdarUser authenticatedUser) {
        getDao().changeSubmitter(collection, submitter, authenticatedUser);
    }

    @Transactional(readOnly = false)
    public <C extends VisibleCollection> void moveResource(Resource resource, C fromCollection, C toCollection, TdarUser tdarUser) {
        if (!authorizationService.canEdit(tdarUser, resource) || !authorizationService.canEdit(tdarUser, fromCollection)
                || !authorizationService.canEdit(tdarUser, toCollection)) {
            throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
        }
        if (fromCollection instanceof SharedCollection) {
            resource.getSharedCollections().remove(fromCollection);
            ((SharedCollection) fromCollection).getResources().remove(resource);
            resource.getSharedCollections().add((SharedCollection) toCollection);
            ((SharedCollection) toCollection).getResources().add(resource);
        }
        if (fromCollection instanceof ListCollection) {
            resource.getUnmanagedResourceCollections().remove(fromCollection);
            ((ListCollection) fromCollection).getUnmanagedResources().remove(resource);
            resource.getUnmanagedResourceCollections().add((ListCollection) toCollection);
            ((ListCollection) toCollection).getUnmanagedResources().add(resource);
        }
        getDao().saveOrUpdate(resource);
        saveOrUpdate(fromCollection);
        saveOrUpdate(toCollection);

    }

    private void removeFromCollection(Resource resource, ResourceCollection collection) {
        if (collection instanceof InternalCollection) {
            resource.getInternalCollections().remove(collection);
            // ((InternalCollection) collection).getResources().remove(resource);
        }
        if (collection instanceof SharedCollection) {
            resource.getSharedCollections().remove(collection);
            // ((SharedCollection)collection).getResources().remove(resource);
        }
        if (collection instanceof ListCollection) {
            resource.getUnmanagedResourceCollections().remove(collection);
            // ((ListCollection)collection).getUnmanagedResources().remove(resource);
        }

    }

    @Transactional(readOnly = true)
    public String getSchemaOrgJsonLD(VisibleCollection resource) throws IOException {
        SchemaOrgCollectionTransformer transformer = new SchemaOrgCollectionTransformer();
        return transformer.convert(serializationService, resource);
    }

    @Transactional(readOnly = true)
    public List<InternalCollection> findAllInternalCollections(TdarUser authenticatedUser) {
        return getDao().findCollectionByOwner(authenticatedUser);
    }

    @Transactional(readOnly = false)
    public RightsBasedResourceCollection createShareFromAdhoc(AdhocShare share, List<Resource> resources, ResourceCollection collectionFrom,
            BillingAccount account,
            TdarUser authenticatedUser) {
        RightsBasedResourceCollection collection = null;
        SharedCollection _share = new SharedCollection();
        List<Resource> issues = new ArrayList<>();
        _share.setName("Share");
        _share.markUpdated(authenticatedUser);
        String from = "";
        HashSet<Resource> toTrack = new HashSet<>();
        if (CollectionUtils.isNotEmpty(resources)) {
            toTrack.addAll(resources);
        }
        if (account != null) {
            from = String.format("%s: %s, %s", "Billing Account", account.getName(), account.getId());
            toTrack.addAll(account.getResources());
        }

        if (collectionFrom != null) {
            String name = "";
            if (collectionFrom instanceof HasName) {
                name = ((HasName) collectionFrom).getName();
            }
            from = String.format("%s: %s, %s", "Collection or Share", name, collectionFrom.getId());
            if (collectionFrom instanceof ListCollection) {
                toTrack.addAll(((ListCollection) collectionFrom).getUnmanagedResources());
            }
            if (collectionFrom instanceof RightsBasedResourceCollection) {
                toTrack.addAll(((RightsBasedResourceCollection) collectionFrom).getResources());
            }
        }

        // If what we're really trying to do is add a user to a collection, then bypass the create logic and just do that...

        // FIXME: how do we determine whether we're creating a new share or not??? Not sure pure canEdit() check is right as the issue might be a true admin,
        // and thus... could be a new share???

        if (collectionFrom instanceof RightsBasedResourceCollection && toTrack.size() == ((RightsBasedResourceCollection) collectionFrom).getResources().size()
                &&
                authorizationService.canEditCollection(authenticatedUser, collectionFrom)) {
            collection = (RightsBasedResourceCollection) collectionFrom;
        } else if (CollectionUtils.isNotEmpty(resources) && resources.size() == 1 && toTrack.size() == 1) {
            // if we're dealing with something that could be an internal collection, use that

            Resource resource = resources.get(0);
            Set<InternalCollection> internalCollections = resource.getInternalCollections();
            InternalCollection internal = null;
            if (internalCollections.size() > 0) { // should never be > 1, but don't add to the problem
                internal = internalCollections.iterator().next();
                collection = internal;
            } else {
                internal = new InternalCollection();
                collection = internal;
                internal.markUpdated(authenticatedUser);
            }
            collection.getResources().add(resource);
            resource.getInternalCollections().add(internal);
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
            getDao().saveOrUpdate(internal);
        } else if (CollectionUtils.isNotEmpty(toTrack)) {
            // otherwise, we're really starting from scratch
            collection = _share;
            toTrack.forEach(r -> {
                if (authorizationService.canEditResource(authenticatedUser, r, GeneralPermissions.MODIFY_RECORD)) {
                    _share.getResources().add(r);
                    r.getSharedCollections().add(_share);
                    publisher.publishEvent(new TdarEvent(r, EventType.CREATE_OR_UPDATE));
                } else {
                    issues.add(r);
                }
            });
        }

        if (CollectionUtils.isNotEmpty(issues)) {
            throw new TdarAuthorizationException("resourceCollectionService.could_not_add", Arrays.asList(issues));
        }
        TdarUser user = getDao().find(TdarUser.class, share.getUserId());
        String _for = "";
        getDao().saveOrUpdate((ResourceCollection) collection);
        if (user != null) {
            _for = user.getUsername();
            collection.getAuthorizedUsers().add(new AuthorizedUser(user, share.getPermission()));
            if (share.getExpires() != null) {
                TimedAccessRestriction tar = new TimedAccessRestriction(share.getExpires());
                tar.setCollection((ResourceCollection) collection);
                tar.setUser(user);
                tar.setCreatedBy(authenticatedUser);
                getDao().saveOrUpdate(tar);
            }

        } else {
            UserInvite invite = new UserInvite();
            _for = share.getEmail();
            invite.setEmailAddress(share.getEmail());
            invite.setPermissions(share.getPermission());
            invite.setDateCreated(new Date());
            invite.setAuthorizer(authenticatedUser);
            invite.setResourceCollection((ResourceCollection) collection);
            if (share.getExpires() != null) {
                TimedAccessRestriction tar = new TimedAccessRestriction(share.getExpires());
                tar.setCollection((ResourceCollection) collection);
                tar.setInvite(invite);
                getDao().saveOrUpdate(tar);
            }
            getDao().saveOrUpdate(invite);
            emailService.sendUserInviteEmail(invite, authenticatedUser);
        }
        _share.setName(String.format("Share with %s", _for));
        _share.setDescription(String.format("auto generated share for %s with %s resources based on %s", _for, collection.getResources().size(), from));
        getDao().saveOrUpdate((ResourceCollection) collection);
        return collection;
    }

    @Transactional(readOnly = false)
    public <C extends ResourceCollection> void saveCollectionForRightsController(C persistable, TdarUser authenticatedUser,
            List<AuthorizedUser> authorizedUsers,
            Class<C> class1, Long startTime) {
        saveAuthorizedUsersForResourceCollection(persistable, persistable, authorizedUsers, true, authenticatedUser);

        if (persistable instanceof VisibleCollection) {
            String msg = String.format("%s modified rights on %s", authenticatedUser, ((VisibleCollection) persistable).getTitle());
            CollectionRevisionLog revision = new CollectionRevisionLog(msg, persistable, authenticatedUser, RevisionLogType.EDIT);
            revision.setTimeBasedOnStart(startTime);
            getDao().saveOrUpdate(revision);
        }
        publisher.publishEvent(new TdarEvent(persistable, EventType.CREATE_OR_UPDATE));

    }

    @Transactional(readOnly = true)
    public List<Resource> findResourcesSharedWith(TdarUser authenticatedUser, TdarUser user) {
        boolean admin = false;
        if (authorizationService.isEditor(authenticatedUser)) {
            admin = true;
        }
        return getDao().findResourcesSharedWith(authenticatedUser, user, admin);
    }

    @Transactional(readOnly = true)
    public <C extends ResourceCollection> List<SharedCollection> findCollectionsSharedWith(TdarUser authenticatedUser, TdarUser user, Class<C> cls) {
        boolean admin = false;
        if (authorizationService.isEditor(authenticatedUser)) {
            admin = true;
        }
        return getDao().findCollectionsSharedWith(authenticatedUser, user, cls, admin);
    }

    @Transactional(readOnly = true)
    public List<TdarUser> findUsersSharedWith(TdarUser authenticatedUser) {
        return getDao().findUsersSharedWith(authenticatedUser);
    }
}
