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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.WrongClassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.CollectionRevisionLog;
import org.tdar.core.bean.collection.CustomizableCollection;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.dao.SimpleFileProcessingDao;
import org.tdar.core.dao.external.auth.InternalTdarRights;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.exception.TdarAuthorizationException;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarValidationException;
import org.tdar.core.service.CollectionSaveObject;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.transform.jsonld.SchemaOrgCollectionTransformer;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.TitleSortComparator;

import com.opensymphony.xwork2.TextProvider;

/**
 * @author Adam Brin
 * 
 */

@Service
public class ResourceCollectionService extends ServiceInterface.TypedDaoBase<ResourceCollection, ResourceCollectionDao> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private transient EntityService entityService;
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
    public void saveAuthorizedUsersForResource(Resource resource, List<AuthorizedUser> authorizedUsers, boolean shouldSave, TdarUser actor) {
        logger.info("saving authorized users...");


        
        logger.trace("------------------------------------------------------");
        logger.debug("current users (start): {}", resource.getAuthorizedUsers());
        logger.debug("incoming authorized users (start): {}", authorizedUsers);

        CollectionRightsComparator comparator = new CollectionRightsComparator(resource.getAuthorizedUsers(), authorizedUsers);
        if (comparator.rightsDifferent()) {
            logger.debug("{} ", actor, resource.getSubmitter());

            if (!authorizationService.canEditResource(actor,resource, GeneralPermissions.MODIFY_RECORD)) {
                throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
            }

            for (AuthorizedUser user : comparator.getDeletions()) {
                resource.getAuthorizedUsers().remove(user);
            }

            for (AuthorizedUser user : comparator.getAdditions()) {
                if (PersistableUtils.isNullOrTransient(user.getCreatedBy())) {
                    user.setCreatedBy(actor);
                }
                resource.getAuthorizedUsers().add(user);
            }


            handleDifferences(resource, actor, comparator);
        }
        comparator = null;

        logger.debug("users after save: {}", resource.getAuthorizedUsers());
        if (shouldSave) {
            getDao().saveOrUpdate(resource);
        }
        logger.trace("------------------------------------------------------");

    }

    private void handleDifferences(HasAuthorizedUsers resource, TdarUser actor, CollectionRightsComparator comparator) {
        if (CollectionUtils.isNotEmpty(comparator.getChanges())) {
            Map<Long, AuthorizedUser> idMap2 = null;

            Map<Long, AuthorizedUser> idMap = PersistableUtils.createIdMap(resource.getAuthorizedUsers());
            for (AuthorizedUser user : comparator.getChanges()) {
                AuthorizedUser actual = idMap.get(user.getId());
                if (actual == null) {
                    // it's possible that the authorizedUserId was not passed back from the client
                    // if so, build a secondary map using the TdarUser (authorizedUser.user) id.
                    if (idMap2 == null) {
                        idMap2 = new HashMap<>();
                        for (AuthorizedUser au : resource.getAuthorizedUsers()) {
                            idMap2.put(au.getUser().getId(), au);
                        }
                    }

                    actual = idMap2.get(user.getUser().getId());
                    logger.debug("actual was null, now: {}", actual);
                }
                checkSelfEscalation(actor, user.getUser(), resource, user.getGeneralPermission());
                actual.setGeneralPermission(user.getGeneralPermission());
                actual.setDateExpires(user.getDateExpires());
            }
        }
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
        List<AuthorizedUser> authorizedUsers = new ArrayList<>(resource.getAuthorizedUsers());
//        if (resource.getInternalResourceCollection() != null) {
            boolean canModify = authorizationService.canUploadFiles(authenticatedUser, resource);
//            ResourceCollection resourceCollection = resource.getInternalResourceCollection();
            applyTransientEnabledPermission(authenticatedUser,authorizedUsers,  canModify);
//            authorizedUsers.addAll(resourceCollection.getAuthorizedUsers());
//        }
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
    private void applyTransientEnabledPermission(Person authenticatedUser, List<AuthorizedUser> authorizedUsers, boolean canModify) {
        for (AuthorizedUser au : authorizedUsers) {
            // enable if: permission is irrelevant (authuser is owner)
            // or if: user has modify permission but is not same as authuser
            au.setEnabled((canModify && !au.getUser().equals(authenticatedUser)));
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
     * Save the incoming @link AuthorizedUser entries for the @link ResourceCollection resolving as needed, avoiding duplicates
     * 
     * @param resourceCollection
     * @param incomingUsers
     * @param shouldSaveResource
     * @param type 
     */
    @Transactional(readOnly = false)
    public void saveAuthorizedUsersForResourceCollection(HasAuthorizedUsers source, ResourceCollection resourceCollection, Collection<AuthorizedUser> incomingUsers,
            boolean shouldSaveResource, TdarUser actor, RevisionLogType type) {
        if (resourceCollection == null) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_save");
        }
        logger.trace("------------------------------------------------------");
        logger.debug("current users (start): {}", resourceCollection.getAuthorizedUsers());
        logger.debug("incoming authorized users (start): {}", incomingUsers);

        
        CollectionRightsComparator comparator = new CollectionRightsComparator(getDao().getUsersFromDb(resourceCollection), incomingUsers);
        if (comparator.rightsDifferent()) {
            logger.debug("{} - {}", actor, resourceCollection.getOwner());

            if (!authorizationService.canAdminiserUsersOn(source, actor)) {
                throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
            }

            for (AuthorizedUser user : comparator.getAdditions()) {
                addUserToCollection(shouldSaveResource, resourceCollection.getAuthorizedUsers(), user, actor, resourceCollection, source, type);
            }

            resourceCollection.getAuthorizedUsers().removeAll(comparator.getDeletions());

            handleDifferences(resourceCollection, actor, comparator);
        }
        comparator = null;

        logger.debug("users after save: {}", resourceCollection.getAuthorizedUsers());
        if (shouldSaveResource) {
            getDao().saveOrUpdate(resourceCollection);
        }
        logger.trace("------------------------------------------------------");
    }

    private void checkSelfEscalation(TdarUser actor, TdarUser userToAdd, HasAuthorizedUsers source, GeneralPermissions generalPermission) {
        // specifically checking for rights escalation
        logger.debug("actor: {}, sourceRights:{} , transientUser:{}", actor, source.getAuthorizedUsers(), userToAdd);
            if (!authorizationService.canDo(actor, source, InternalTdarRights.EDIT_ANYTHING, generalPermission)) {
                throw new TdarAuthorizationException("resourceCollectionService.could_not_add_user", Arrays.asList(userToAdd,
                        generalPermission));
            }
            // find highest permission for actor
            // check that permission is valid for actor to assign
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
            ResourceCollection resourceCollection, HasAuthorizedUsers source, RevisionLogType type) {
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
            if (PersistableUtils.isNotNullOrTransient(source) && RevisionLogType.EDIT == type) {
                checkSelfEscalation(actor, user, source, incomingUser.getGeneralPermission());
            }
            currentUsers.add(incomingUser);
            if (shouldSaveResource) {
                incomingUser.setCreatedBy(actor);
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
            removeResourceCollectionFromResource(resource, current, authenticatedUser, collection);
        }

        for (C collection : helper.getToAdd()) {
            logger.debug("adding: {} ", collection);
            addResourceCollectionToResource(resource, current, authenticatedUser, shouldSave, errorHandling, collection, cls);
        }
        logger.debug("after save: {} ({})", current, current.size());

    }

    private <C extends ResourceCollection> void removeResourceCollectionFromResource(Resource resource, Set<C> current, TdarUser authenticatedUser,
            C collection) {
        if (!authorizationService.canRemoveFromCollection(collection, authenticatedUser)) {
            String name = "Collection";
            if (collection instanceof VisibleCollection) {
                name = ((VisibleCollection) collection).getName();
            }
            throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_remmove_error", Arrays.asList(name));

        }
        current.remove(collection);
        if (collection instanceof RightsBasedResourceCollection) {
            ((RightsBasedResourceCollection) collection).getResources().remove(resource);
            if (collection instanceof SharedCollection) {
                resource.getSharedCollections().remove(collection);
//            } else {
//                resource.getInternalCollections().remove(collection);
            }
        } else {
            ((ListCollection) collection).getUnmanagedResources().remove(resource);
            resource.getUnmanagedResourceCollections().remove((ListCollection) collection);
        }
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
//        if (collection instanceof InternalCollection) {
//            collectionToAdd = collection;
//        } else {
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
//        }
        logger.trace("{}, {}", collectionToAdd, collectionToAdd.isValid());
        String name = getName(collectionToAdd);
        if (collectionToAdd != null && collectionToAdd.isValid()) {
            if (PersistableUtils.isNotNullOrTransient(collectionToAdd) && !current.contains(collectionToAdd)
                    && !authorizationService.canAddToCollection(authenticatedUser, collectionToAdd)) {
                throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_error",
                        Arrays.asList(name));
            }
            collectionToAdd.markUpdated(authenticatedUser);
            if (collectionToAdd.isTransient()) {
                collectionToAdd.setChangesNeedToBeLogged(true);
                collectionToAdd.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, authenticatedUser, GeneralPermissions.ADMINISTER_SHARE));
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

//        if (collectionToAdd instanceof InternalCollection) {
//            resource.getInternalCollections().add((InternalCollection) collectionToAdd);
//        }
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
        allChildren.addAll(addAlternateChildrenTrees(allChildren, cls));
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
            if (child.getAlternateParent() != null) {
                child.getAlternateParent().getTransientChildren().add(child);
            }
        };

        // second pass - sort all children lists (we add root into "allchildren" so we can sort the top level)
        allChildren.add(collection);
        allChildren.forEach(child -> {
            if (child != null && CollectionUtils.isNotEmpty(child.getTransientChildren())) {
                child.getTransientChildren().sort(new TitleSortComparator());
                logger.trace("new list: {}", child.getTransientChildren());
            }
        });
        return allChildren;
    }

    private <C extends HierarchicalCollection<C>> Collection<C> addAlternateChildrenTrees(List<C> allChildren, Class<C> cls) {
        Set<C> toReturn = new HashSet<>(getDao().getAlternateChildrenTrees(allChildren, cls)); 
        return toReturn;
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
        applyTransientEnabledPermission(authenticatedUser, users,
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
    public void addUserToInternalCollection(Resource resource, TdarUser authenticatedUser, TdarUser user, GeneralPermissions permission) {
        resource.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, user, permission));
    }

    @Transactional(readOnly = true)
    public Set<RightsBasedResourceCollection> getEffectiveSharesForResource(Resource resource) {
        Set<RightsBasedResourceCollection> tempSet = new HashSet<>();
        for (SharedCollection collection : resource.getSharedResourceCollections()) {
            if (collection != null) {
                tempSet.addAll(collection.getHierarchicalResourceCollections());
            }
        }
//        InternalCollection internal = resource.getInternalResourceCollection();
//        if ((internal != null) &&
//                CollectionUtils.isNotEmpty(internal.getAuthorizedUsers())) {
//            tempSet.add(internal);
//        }

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
            if (!authorizationService.canAddToCollection(authenticatedUser, (ResourceCollection) persistable)) {
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
    public <C extends HierarchicalCollection> void saveCollectionForController(CollectionSaveObject cso) {
        C persistable = (C) cso.getCollection();
        Class<C> cls = cso.getPersistableClass();
        TdarUser authenticatedUser = cso.getUser();
        if (persistable == null) {
            throw new TdarRecoverableRuntimeException();
        }
        logger.debug("{} - {}" , persistable, persistable.getId());
        if (PersistableUtils.isTransient(persistable)) {
            GeneralPermissions perm = GeneralPermissions.ADMINISTER_SHARE;
            if (persistable instanceof ListCollection) {
                cls = (Class<C>) ListCollection.class;
                perm = GeneralPermissions.ADMINISTER_GROUP;
            }
            persistable.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser,authenticatedUser, perm));
        }
        RevisionLogType type = RevisionLogType.CREATE;
        if (PersistableUtils.isNotTransient(persistable)) {
            type = RevisionLogType.EDIT;
        }

        List<Resource> resourcesToRemove = getDao().findAll(Resource.class, cso.getToRemove());
        List<Resource> resourcesToAdd = getDao().findAll(Resource.class, cso.getToAdd());
        getLogger().debug("toAdd: {}", resourcesToAdd);
        getLogger().debug("toRemove: {}", resourcesToRemove);

        if (persistable instanceof SharedCollection) {
            SharedCollection shared = (SharedCollection) persistable;
            reconcileIncomingResourcesForCollection(shared, authenticatedUser, resourcesToAdd, resourcesToRemove);
        }

        if (persistable instanceof ListCollection) {
            ListCollection list = (ListCollection) persistable;
            reconcileIncomingResourcesForCollectionWithoutRights(list, authenticatedUser, resourcesToAdd, resourcesToRemove);
        }
//        saveAuthorizedUsersForResourceCollection(persistable, persistable, cso.getAuthorizedUsers(), cso.isShouldSave(), authenticatedUser,type);
        simpleFileProcessingDao.processFileProxyForCreatorOrCollection(((CustomizableCollection<ListCollection>) persistable).getProperties(),
                cso.getFileProxy());

        if (!Objects.equals(cso.getParentId(), persistable.getParentId())) {
            updateCollectionParentTo(authenticatedUser, persistable, (C) cso.getParent(), cls);
        }

        if (!Objects.equals(cso.getAlternateParentId(), persistable.getAlternateParentId())) {
            logger.debug("updating alternate parent for {} from {} to {}", persistable.getId(), persistable.getAlternateParent(), cso.getAlternateParent());
            persistable.setAlternateParent(cso.getAlternateParent());
        }

        String msg = String.format("%s modified %s", authenticatedUser, persistable.getTitle());
        CollectionRevisionLog revision = new CollectionRevisionLog(msg, persistable, authenticatedUser, type);
        revision.setTimeBasedOnStart(cso.getStartTime());
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
        if (rc.getProperties() != null && rc.getProperties().getWhitelabel()) {
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
//        if (collection instanceof InternalCollection) {
//            resource.getInternalCollections().remove(collection);
//            // ((InternalCollection) collection).getResources().remove(resource);
//        }
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

    @Transactional(readOnly = false)
    public <C extends ResourceCollection> void saveCollectionForRightsController(C c, TdarUser authenticatedUser,
            List<UserRightsProxy> proxies,
            Class<C> class1, Long startTime) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<>();
        List<UserInvite> invites = new ArrayList<>();

        convertProxyToItems(proxies, authenticatedUser, authorizedUsers, invites);
        RevisionLogType edit = RevisionLogType.EDIT;
        saveAuthorizedUsersForResourceCollection(c, c, authorizedUsers, true, authenticatedUser, edit);

        if (c instanceof VisibleCollection) {
            String msg = String.format("%s modified rights on %s", authenticatedUser, ((VisibleCollection) c).getTitle());
            CollectionRevisionLog revision = new CollectionRevisionLog(msg, c, authenticatedUser, edit);
            revision.setTimeBasedOnStart(startTime);
            getDao().saveOrUpdate(revision);
        }
        handleInvites(authenticatedUser, invites, c);
        publisher.publishEvent(new TdarEvent(c, EventType.CREATE_OR_UPDATE));

    }

    @Transactional(readOnly = true)
    public List<Resource> findResourcesSharedWith(TdarUser authenticatedUser, List<SharedCollection> list, TdarUser user) {
        boolean admin = false;
        if (authorizationService.isEditor(authenticatedUser)) {
            admin = true;
        }
        return getDao().findResourcesSharedWith(authenticatedUser, list, user, admin);
    }

    @Transactional(readOnly = true)
    public <C extends ResourceCollection> List<SharedCollection> findCollectionsSharedWith(TdarUser authenticatedUser, TdarUser user, Class<C> cls) {
        boolean admin = false;
        if (authorizationService.isEditor(authenticatedUser)) {
            admin = true;
        }
        return getDao().findCollectionsSharedWith(authenticatedUser, user, cls, GeneralPermissions.MODIFY_RECORD, admin);
    }

    @Transactional(readOnly = true)
    public List<TdarUser> findUsersSharedWith(TdarUser authenticatedUser) {
        return getDao().findUsersSharedWith(authenticatedUser);
    }

    @Transactional(readOnly = true)
    public List<UserInvite> findUserInvites(Persistable resource) {
        if (resource instanceof Resource) {
            return getDao().findUserInvites((Resource) resource);
        }
        if (resource instanceof ResourceCollection) {
            return getDao().findUserInvites((ResourceCollection) resource);
        }
        if (resource instanceof TdarUser) {
            return getDao().findUserInvites((TdarUser) resource);
        }
        return null;

    }

    @Transactional(readOnly = true)
    public List<UserInvite> findUserInvites(Resource resource) {
        return getDao().findUserInvites(resource);
    }

    @Transactional(readOnly = true)
    public List<UserInvite> findUserInvites(ResourceCollection resourceCollection) {
        return getDao().findUserInvites(resourceCollection);
    }

    @Transactional(readOnly = true)
    public List<UserInvite> findUserInvites(TdarUser user) {
        return getDao().findUserInvites(user);
    }

    @Transactional(readOnly = false)
    public void saveResourceRights(List<UserRightsProxy> proxies, TdarUser authenticatedUser, Resource resource) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<>();
        List<UserInvite> invites = new ArrayList<>();
        convertProxyToItems(proxies, authenticatedUser, authorizedUsers, invites);
        saveAuthorizedUsersForResource(resource, authorizedUsers, true, authenticatedUser);
//        if (resource.getInternalResourceCollection() == null) {
//            getDao().createInternalResourceCollectionForResource(resource.getSubmitter(), resource, true);
//        }
//        resource.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, authenticatedUser, GeneralPermissions.ADMINISTER_SHARE));
        if (CollectionUtils.isNotEmpty(invites)) {
            throw new NotImplementedException();
        }
        //handleInvites(authenticatedUser, invites, resource.getInternalResourceCollection());
    }

    private <C extends ResourceCollection> void handleInvites(TdarUser authenticatedUser, List<UserInvite> invites, C c) {
        List<UserInvite> existing = getDao().findUserInvites(c);
        Map<Long, UserInvite> createIdMap = PersistableUtils.createIdMap(existing);
        
        if (CollectionUtils.isNotEmpty(invites)) {
            for (UserInvite invite : invites) {
                if (PersistableUtils.isNotTransient(invite) || invite == null || invite.getUser().hasNoPersistableValues()) {
                    continue;
                }

                // existing one
                if (PersistableUtils.isNotNullOrTransient(invite.getId() )) {
                    UserInvite inv = createIdMap.get(invite.getId());
                    inv.setDateExpires(invite.getDateExpires());
                    inv.setPermissions(inv.getPermissions());
                    getDao().saveOrUpdate(inv);
                    createIdMap.remove(invite.getId());
                    continue;
                }

                // new invite
                invite.setResourceCollection(c);
                getDao().saveOrUpdate(invite);
                emailService.sendUserInviteEmail(invite, authenticatedUser);
            }
        }
        
        getDao().delete(createIdMap.values());
    }

    private void convertProxyToItems(List<UserRightsProxy> proxies, TdarUser authenticatedUser, List<AuthorizedUser> authorizedUsers, List<UserInvite> invites) {
        for (UserRightsProxy proxy : proxies) {
            if (proxy == null || proxy.isEmpty()) {
                return;
            } 

            if (proxy.getEmail() != null || proxy.getInviteId() != null) {
                UserInvite invite = toInvite(proxy, authenticatedUser);
                if (invite != null) {
                    if (invite.getUser().isValidForController()) {
                        invites.add(invite);
                    } else {
                        throw new TdarValidationException("resourceCollectionService.invalid", Arrays.asList(invite.getUser()));
                    }
                }
            } else if (PersistableUtils.isNotNullOrTransient(proxy.getId())) {
                authorizedUsers.add(toAuthorizedUser(proxy));
            }
        }
    }

    private UserInvite toInvite(UserRightsProxy proxy, TdarUser user) {
        UserInvite invite = new UserInvite();
        invite.setDateExpires(proxy.getUntilDate());
        invite.setId(proxy.getInviteId());
        invite.setPermissions(proxy.getPermission());
        if (PersistableUtils.isNotNullOrTransient(proxy.getInviteId() )) {
            invite = getDao().find(UserInvite.class, proxy.getInviteId());
        }
        invite.setAuthorizer(user);
        Person person = new Person(proxy.getFirstName(), proxy.getLastName(), proxy.getEmail());
        if (person.hasNoPersistableValues()) {
            return null;
        }
        person = entityService.findOrSaveCreator(person);
        invite.setPerson(person);
        return invite;
    }

    private AuthorizedUser toAuthorizedUser(UserRightsProxy proxy) {
        try {
            AuthorizedUser au = new AuthorizedUser();
            logger.debug("{} {} ", proxy, proxy.getId());
            TdarUser user = getDao().find(TdarUser.class, proxy.getId());
            if (user == null && PersistableUtils.isNotNullOrTransient(proxy.getId() )) {
                throw new TdarRecoverableRuntimeException("resourceCollectionService.user_does_not_exists", Arrays.asList(proxy.getDisplayName()));
            }
            logger.debug("{} {}", user.getClass() , user);
            au.setUser(user);
            au.setGeneralPermission(proxy.getPermission());
            au.setDateExpires(proxy.getUntilDate());
            getLogger().debug("{} ({})", au, proxy.getDisplayName());
        return au;
        } catch (WrongClassException e) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.user_does_not_exists", Arrays.asList(proxy.getDisplayName()));
        }
    }

    @Transactional(readOnly = true)
    public <C extends HierarchicalCollection> List<C> findAlternateChildren(List<Long> ids, TdarUser authenticatedUser, Class<C> cls) {
        List<C> findAlternateChildren = getDao().findAlternateChildren(ids, cls);
        if (CollectionUtils.isNotEmpty(findAlternateChildren)) {
            findAlternateChildren.forEach(c -> {
                authorizationService.applyTransientViewableFlag(c, authenticatedUser);
            });
        }
        return findAlternateChildren;
    }

}
