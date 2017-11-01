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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.CollectionResourceSection;
import org.tdar.core.bean.collection.CollectionRevisionLog;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.Permissions;
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
import org.tdar.core.service.CollectionSaveObject;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.RightsResolver;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.UserRightsProxyService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.resource.ErrorHandling;
import org.tdar.transform.jsonld.SchemaOrgCollectionTransformer;
import org.tdar.utils.PersistableUtils;
import org.tdar.utils.TitleSortComparator;

import com.opensymphony.xwork2.TextProvider;

/**
 * @author Adam Brin
 * 
 */

@Service
public class ResourceCollectionServiceImpl extends ServiceInterface.TypedDaoBase<ResourceCollection, ResourceCollectionDao>
        implements ResourceCollectionService {

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
    private UserRightsProxyService userRightsProxyService;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#saveAuthorizedUsersForResource(org.tdar.core.bean.resource.Resource, java.util.List,
     * boolean, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional
    public void saveAuthorizedUsersForResource(Resource resource, List<AuthorizedUser> authorizedUsers, boolean shouldSave, TdarUser actor) {
        logger.info("saving authorized users...");

        logger.trace("------------------------------------------------------");
        logger.debug("current users (start): {}", resource.getAuthorizedUsers());
        logger.debug("incoming authorized users (start): {}", authorizedUsers);

        CollectionRightsComparator comparator = new CollectionRightsComparator(resource.getAuthorizedUsers(), authorizedUsers);
        if (comparator.rightsDifferent()) {
            RightsResolver rco = authorizationService.getRightsResolverFor(resource, actor, InternalTdarRights.EDIT_ANYTHING);
            comparator.makeChanges(rco, resource, actor);
        }
        comparator = null;

        logger.debug("users after save: {}", resource.getAuthorizedUsers());
        if (shouldSave) {
            getDao().saveOrUpdate(resource);
        }
        logger.trace("------------------------------------------------------");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getAuthorizedUsersForResource(org.tdar.core.bean.resource.Resource,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuthorizedUser> getAuthorizedUsersForResource(Resource resource, TdarUser authenticatedUser) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<>(resource.getAuthorizedUsers());
        boolean canModify = authorizationService.canUploadFiles(authenticatedUser, resource);
        applyTransientEnabledPermission(authenticatedUser, authorizedUsers, canModify);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findAllTopLevelCollections()
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAllTopLevelCollections() {
        Set<ResourceCollection> resultSet = new HashSet<>();
        resultSet.addAll((List<ResourceCollection>) getDao().findCollectionsOfParent(null, false));
        List<ResourceCollection> toReturn = new ArrayList<>(resultSet);
        Collections.sort(toReturn, new Comparator<ResourceCollection>() {
            @Override
            public int compare(ResourceCollection o1, ResourceCollection o2) {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findDirectChildCollections(java.lang.Long, java.lang.Boolean, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findDirectChildCollections(Long id, Boolean hidden) {
        return getDao().findCollectionsOfParent(id, hidden);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#delete(org.tdar.core.bean.collection.ResourceCollection)
     */
    @Override
    @Transactional
    public void delete(ResourceCollection resourceCollection) {
        getDao().delete(resourceCollection.getAuthorizedUsers());
        for (Resource resource : (resourceCollection).getManagedResources()) {
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }
        getDao().delete(resourceCollection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#saveAuthorizedUsersForResourceCollection(org.tdar.core.bean.resource.HasAuthorizedUsers,
     * org.tdar.core.bean.collection.ResourceCollection, java.util.Collection, boolean, org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.resource.RevisionLogType)
     */
    @Override
    @Transactional(readOnly = false)
    public void saveAuthorizedUsersForResourceCollection(HasAuthorizedUsers source, ResourceCollection resourceCollection,
            Collection<AuthorizedUser> incomingUsers,
            boolean shouldSaveResource, TdarUser actor, RevisionLogType type) {
        if (resourceCollection == null) {
            throw new TdarRecoverableRuntimeException("resourceCollectionService.could_not_save");
        }
        logger.trace("------------------------------------------------------");
        logger.debug("current users (start): {}", resourceCollection.getAuthorizedUsers());
        logger.debug("incoming authorized users (start): {}", incomingUsers);

        RightsResolver rightsResolver = authorizationService.getRightsResolverFor(resourceCollection, actor, InternalTdarRights.EDIT_ANYTHING);
        CollectionRightsComparator comparator = new CollectionRightsComparator(getDao().getUsersFromDb(resourceCollection), incomingUsers);
        if (comparator.rightsDifferent()) {
            logger.debug("{}", actor);

            if (!authorizationService.canAdminiserUsersOn( actor, source)) {
                throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
            }

            for (AuthorizedUser user : comparator.getAdditions()) {
                addUserToCollection(shouldSaveResource, resourceCollection.getAuthorizedUsers(), user, actor, resourceCollection, source, type, rightsResolver);
            }

            resourceCollection.getAuthorizedUsers().removeAll(comparator.getDeletions());

            comparator.handleDifferences(resourceCollection, actor, rightsResolver);
        }
        comparator = null;

        logger.debug("users after save: {}", resourceCollection.getAuthorizedUsers());
        if (shouldSaveResource) {
            getDao().saveOrUpdate(resourceCollection);
        }
        logger.trace("------------------------------------------------------");
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
            ResourceCollection resourceCollection, HasAuthorizedUsers source, RevisionLogType type, RightsResolver rightsResolver) {
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
                rightsResolver.checkEscalation(actor, incomingUser);
            }
            currentUsers.add(incomingUser);
            if (shouldSaveResource) {
                incomingUser.setCreatedBy(actor);
                getDao().saveOrUpdate(incomingUser);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findParentOwnerCollections(org.tdar.core.bean.entity.Person, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findParentOwnerCollections(Person person) {
        return getDao().findParentOwnerCollections(person);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findPotentialParentCollections(org.tdar.core.bean.entity.Person, C, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findPotentialParentCollections(Person person, ResourceCollection collection) {
        List<ResourceCollection> potentialCollections = getDao().findParentOwnerCollections(person);
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
                parent = (parent).getParent();
            }
        }
        return potentialCollections;
    }

    @Override
    @Transactional(readOnly = false)
    public void saveResourceCollections(Resource resource, Collection<ResourceCollection> incoming, Set<ResourceCollection> current,
            TdarUser authenticatedUser, boolean shouldSave, ErrorHandling errorHandling, CollectionResourceSection type) {

        // FIXME.... BROKEN???
        logger.debug("incoming {}: {} ({})", type, incoming, incoming.size());
        logger.debug(" current {}: {} ({})", type, current, current.size());

        ResourceCollectionSaveHelper helper = new ResourceCollectionSaveHelper(incoming, current);
        logger.info("collections to remove: {}", helper.getToDelete());
        for (ResourceCollection collection : helper.getToDelete()) {
            removeResourceCollectionFromResource(resource, current, authenticatedUser, collection, type);
        }

        for (ResourceCollection collection : helper.getToAdd()) {
            if (collection.isValidForController()) {
                logger.debug("adding: {} ", collection);
                addResourceCollectionToResource(resource, current, authenticatedUser, shouldSave, errorHandling, collection, type);
            } else {
                logger.warn("skipping invalid collection: {}", collection);
            }
        }
        logger.debug("after save: {} ({})", current, current.size());

    }

    private void removeResourceCollectionFromResource(Resource resource, Set<ResourceCollection> current, TdarUser authenticatedUser,
            ResourceCollection collection, CollectionResourceSection type) {
        if (!authorizationService.canRemoveFromCollection(authenticatedUser, collection)) {
            String name = collection.getName();
            throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_remmove_error", Arrays.asList(name));

        }
        current.remove(collection);
        if (type == CollectionResourceSection.MANAGED) {
            (collection).getManagedResources().remove(resource);
            resource.getManagedResourceCollections().remove(collection);
        } else {
            collection.getUnmanagedResources().remove(resource);
            resource.getUnmanagedResourceCollections().remove(collection);
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
    @Override
    @Transactional(readOnly = false)
    public void addResourceCollectionToResource(Resource resource, Set<ResourceCollection> current, TdarUser authenticatedUser,
            boolean shouldSave,
            ErrorHandling errorHandling, ResourceCollection collection, CollectionResourceSection type) {
        logger.trace("addResourceCollectionToResource({}) {} - {}", type, collection, resource);
        if (!collection.isValidForController()) {
            logger.debug("skipping invalid: {}", collection);
            return;
        }
        ResourceCollection collectionToAdd = null;
        if (collection.isTransient()) {
            collectionToAdd = findOrCreateCollection(resource, authenticatedUser, collection);
        } else {
            collectionToAdd = getDao().find(ResourceCollection.class, collection.getId());
        }

        logger.trace("{}, {}", collectionToAdd, collectionToAdd.isValid());
        String name = collectionToAdd.getName();
        if (collectionToAdd != null && collectionToAdd.isValid()) {
            if (PersistableUtils.isNotNullOrTransient(collectionToAdd) && !current.contains(collectionToAdd)
                    && !authorizationService.canAddToCollection(authenticatedUser, collectionToAdd)) {
                throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_error",
                        Arrays.asList(name));
            }
            collectionToAdd.markUpdated(authenticatedUser);
            if (collectionToAdd.isTransient()) {
                collectionToAdd.setChangesNeedToBeLogged(true);
                collectionToAdd.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, authenticatedUser, Permissions.ADMINISTER_COLLECTION));
            }

            // jtd the following line changes collectionToAdd's hashcode. all sets it belongs to are now corrupt.
            if (type == CollectionResourceSection.MANAGED) {
                addToCollection(resource, collectionToAdd, CollectionResourceSection.MANAGED);
            } else {
                collectionToAdd.getUnmanagedResources().add(resource);
                resource.getUnmanagedResourceCollections().add(collectionToAdd);
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

    private void addToCollection(Resource resource, ResourceCollection collectionToAdd, CollectionResourceSection type) {
        if (type == CollectionResourceSection.MANAGED) {
            collectionToAdd.getManagedResources().add(resource);
            resource.getManagedResourceCollections().add(collectionToAdd);
        } else {
            collectionToAdd.getUnmanagedResources().add(resource);
            resource.getUnmanagedResourceCollections().add(collectionToAdd);
        }
    }

    private ResourceCollection findOrCreateCollection(Resource resource, TdarUser authenticatedUser, ResourceCollection collection) {
        boolean isAdmin = authorizationService.can(InternalTdarRights.EDIT_RESOURCE_COLLECTIONS, authenticatedUser);
        ResourceCollection potential = getDao().findCollectionWithName(authenticatedUser, isAdmin, collection.getName());
        if (potential != null) {
            return potential;
        } else {
            collection.setOwner(authenticatedUser);
            collection.markUpdated(resource.getSubmitter());
            if (collection.getSortBy() == null) {
                collection.setSortBy(ResourceCollection.DEFAULT_SORT_OPTION);
            }
            publisher.publishEvent(new TdarEvent(collection, EventType.CREATE_OR_UPDATE));
            return collection;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findAllResourceCollections()
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAllResourceCollections() {
        return getDao().findAllSharedResourceCollections();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#buildCollectionTreeForController(C, org.tdar.core.bean.entity.TdarUser, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public TreeSet<ResourceCollection> buildCollectionTreeForController(ResourceCollection collection, TdarUser authenticatedUser) {
        TreeSet<ResourceCollection> allChildren = new TreeSet<ResourceCollection>(new TitleSortComparator());
        allChildren.addAll(getAllChildCollections(collection));
        allChildren.addAll(addAlternateChildrenTrees(allChildren, collection));
        // FIXME: iterate over all children to reconcile tree
        Iterator<ResourceCollection> iter = allChildren.iterator();
        while (iter.hasNext()) {
            ResourceCollection child = iter.next();
            authorizationService.applyTransientViewableFlag(child, authenticatedUser);
            ResourceCollection parent = child.getParent();
            if (parent != null) {
                parent.getTransientChildren().add(child);
                iter.remove();
            }
            if (child.getAlternateParent() != null) {
                child.getAlternateParent().getTransientChildren().add(child);
            }
        }
        ;

        // second pass - sort all children lists (we add root into "allchildren" so we can sort the top level)
        allChildren.add(collection);
        return allChildren;
    }

    private Collection<ResourceCollection> addAlternateChildrenTrees(Collection<ResourceCollection> allChildren, ResourceCollection child) {
        Set<ResourceCollection> toReturn = new HashSet<>(getDao().getAlternateChildrenTrees(allChildren, child));
        return toReturn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findAllChildCollectionsOnly(E, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAllChildCollectionsOnly(ResourceCollection collection) {
        return getDao().findAllChildCollectionsOnly(collection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findFlattenedCollections(org.tdar.core.bean.entity.Person,
     * org.tdar.core.bean.entity.permissions.GeneralPermissions, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public Set<ResourceCollection> findFlattenedCollections(Person user, Permissions generalPermissions) {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getFullyInitializedRootResourceCollection(org.tdar.core.bean.collection.SharedCollection,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceCollection getFullyInitializedRootResourceCollection(ResourceCollection anyNode, TdarUser authenticatedUser) {
        ResourceCollection root = getRootResourceCollection(anyNode);
        buildCollectionTreeForController(getRootResourceCollection(anyNode), authenticatedUser);
        return root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findAllPublicActiveCollectionIds()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Long> findAllPublicActiveCollectionIds() {
        return getDao().findAllPublicActiveCollectionIds();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findAllResourcesWithStatus(org.tdar.core.bean.collection.ResourceCollection,
     * org.tdar.core.bean.resource.Status)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findAllResourcesWithStatus(ResourceCollection persistable, Status... statuses) {
        return getDao().findAllResourcesWithStatus(persistable, statuses);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getAuthorizedUsersForCollection(org.tdar.core.bean.collection.ResourceCollection,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<AuthorizedUser> getAuthorizedUsersForCollection(ResourceCollection persistable, TdarUser authenticatedUser) {
        List<AuthorizedUser> users = new ArrayList<>(persistable.getAuthorizedUsers());
        applyTransientEnabledPermission(authenticatedUser, users,
                authorizationService.canEditCollection(authenticatedUser, persistable));
        return users;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#reconcileCollectionTree(java.util.Collection, org.tdar.core.bean.entity.TdarUser,
     * java.util.List, java.lang.Class)
     */
    @Override
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
            buildCollectionTreeForController(rc, authenticatedUser);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findCollectionSparseResources(java.lang.Long)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findCollectionSparseResources(Long collectionId) {
        return getDao().findCollectionSparseResources(collectionId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getCollectionViewCount(org.tdar.core.bean.collection.ResourceCollection)
     */
    @Override
    @Transactional(readOnly = true)
    public Long getCollectionViewCount(ResourceCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable))
            return 0L;
        return getDao().getCollectionViewCount(persistable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#updateCollectionParentTo(org.tdar.core.bean.entity.TdarUser, C, C, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateCollectionParentTo(TdarUser authorizedUser, ResourceCollection persistable, ResourceCollection parent) {
        // find all children with me as a parent
        if (!authorizationService.canEditCollection(authorizedUser, persistable) ||
                parent != null && !authorizationService.canEditCollection(authorizedUser, parent)) {
            throw new TdarAuthorizationException("resourceCollectionService.user_does_not_have_permisssions");
        }

        List<ResourceCollection> children = getAllChildCollections(persistable);
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
        for (ResourceCollection child : children) {
            child.getParentIds().removeAll(oldParentIds);
            child.getParentIds().addAll(parentIds);
            saveOrUpdate(child);
        }
        saveOrUpdate(persistable);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#updateAlternateCollectionParentTo(org.tdar.core.bean.entity.TdarUser, C,
     * org.tdar.core.bean.collection.HierarchicalCollection, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = false)
    public void updateAlternateCollectionParentTo(TdarUser authorizedUser, ResourceCollection persistable, ResourceCollection hierarchicalCollection) {

        List<ResourceCollection> children = getAllChildCollections(persistable);
        List<Long> oldParentIds = new ArrayList<>(persistable.getAlternateParentIds());
        logger.debug("updating parent for {} from {} to {}", persistable.getId(), persistable.getAlternateParent(), hierarchicalCollection);
        persistable.setAlternateParent(hierarchicalCollection);
        List<Long> parentIds = new ArrayList<>();
        if (PersistableUtils.isNotNullOrTransient(hierarchicalCollection)) {
            if (CollectionUtils.isNotEmpty(hierarchicalCollection.getAlternateParentIds())) {
                parentIds.addAll(hierarchicalCollection.getAlternateParentIds());
                parentIds.addAll(hierarchicalCollection.getParentIds());
            }
            parentIds.add(hierarchicalCollection.getId());
        }
        persistable.getAlternateParentIds().clear();
        persistable.getAlternateParentIds().addAll(parentIds);
        for (ResourceCollection child : children) {
            child.getAlternateParentIds().removeAll(oldParentIds);
            child.getAlternateParentIds().addAll(parentIds);
            saveOrUpdate(child);
        }
        saveOrUpdate(persistable);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getAllChildCollections(C, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> getAllChildCollections(ResourceCollection persistable) {
        return getDao().getAllChildCollections(persistable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#addUserToInternalCollection(org.tdar.core.bean.resource.Resource,
     * org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.entity.permissions.GeneralPermissions)
     */
    @Override
    @Transactional
    public void addUserToInternalCollection(Resource resource, TdarUser authenticatedUser, TdarUser user, Permissions permission) {
        resource.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, user, permission));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getEffectiveSharesForResource(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public Set<ResourceCollection> getEffectiveSharesForResource(Resource resource) {
        Set<ResourceCollection> tempSet = new HashSet<>();
        for (ResourceCollection collection : resource.getManagedResourceCollections()) {
            if (collection != null) {
                tempSet.addAll(collection.getHierarchicalResourceCollections());
            }
        }

        return tempSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getEffectiveResourceCollectionsForResource(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public Set<ResourceCollection> getEffectiveResourceCollectionsForResource(Resource resource) {
        Set<ResourceCollection> tempSet = new HashSet<>();
        for (ResourceCollection collection : resource.getUnmanagedResourceCollections()) {
            if (collection != null) {
                tempSet.addAll(collection.getHierarchicalResourceCollections());
            }
        }

        Iterator<ResourceCollection> iter = tempSet.iterator();
        while (iter.hasNext()) {
            ResourceCollection next = iter.next();
            if (CollectionUtils.isEmpty((next).getAuthorizedUsers())) {
                iter.remove();
            }
        }

        return tempSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#reconcileIncomingResourcesForCollection(org.tdar.core.bean.collection.
     * RightsBasedResourceCollection, org.tdar.core.bean.entity.TdarUser, java.util.List, java.util.List)
     */
    @Override
    @Transactional(readOnly = false)
    public void reconcileIncomingResourcesForCollection(ResourceCollection persistable, TdarUser authenticatedUser, List<Resource> resourcesToAdd,
            List<Resource> resourcesToRemove, CollectionResourceSection type) {
        getLogger().debug(" {}:: toAdd: {}", type, resourcesToAdd);
        getLogger().debug(" {}::toRemove: {}", type, resourcesToRemove);

        Set<Resource> resources = persistable.getManagedResources();
        List<Resource> ineligibleToAdd = new ArrayList<Resource>(); // existing resources the user doesn't have the rights to add
        List<Resource> ineligibleToRemove = new ArrayList<Resource>(); // existing resources the user doesn't have the rights to add
        Permissions modifyRecord = Permissions.MODIFY_RECORD;

        if (CollectionUtils.isNotEmpty(resourcesToAdd)) {
            if (!authorizationService.canAddToCollection(authenticatedUser, persistable)) {
                throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_error",
                        Arrays.asList(persistable.getName()));
            }
        }

        if (CollectionUtils.isNotEmpty(resourcesToRemove)) {
            if (!authorizationService.canRemoveFromCollection(authenticatedUser, persistable)) {
                throw new TdarAuthorizationException("resourceCollectionSerice.resource_collection_rights_remmove_error",
                        Arrays.asList(persistable.getName()));
            }
        }

        for (Resource resource : resourcesToAdd) {
            if (type == CollectionResourceSection.MANAGED && !authorizationService.canEditResource(authenticatedUser, resource, modifyRecord)
                    || type == CollectionResourceSection.UNMANAGED && !authorizationService.canViewResource(authenticatedUser, resource)) {
                ineligibleToAdd.add(resource);
            } else {
                addToCollection(resource, persistable, type);
                publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
            }
        }

        for (Resource resource : resourcesToRemove) {
            if (type == CollectionResourceSection.MANAGED && !authorizationService.canEditResource(authenticatedUser, resource, modifyRecord)
                    || type == CollectionResourceSection.UNMANAGED && !authorizationService.canViewResource(authenticatedUser, resource)) {
                ineligibleToAdd.add(resource);
            } else {
                removeFromCollection(resource, persistable, type);
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
        if (type == CollectionResourceSection.MANAGED) {
            getLogger().debug(" result: {}:: {}", type, persistable.getManagedResources());
        } else {
            getLogger().debug(" result: {}:: {}", type, persistable.getUnmanagedResources());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#removeResourceFromCollection(org.tdar.core.bean.resource.Resource,
     * org.tdar.core.bean.collection.VisibleCollection, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void removeResourceFromCollection(Resource resource, ResourceCollection collection, TdarUser authenticatedUser, CollectionResourceSection type) {
        if (type == CollectionResourceSection.MANAGED && 
        	(!authorizationService.canEditResource(authenticatedUser, resource, Permissions.MODIFY_RECORD) ||
            !authorizationService.canRemoveFromCollection(authenticatedUser, collection))) {
            throw new TdarAuthorizationException("resourceCollectionService.could_not_remove");
        } 
        else if (type == CollectionResourceSection.UNMANAGED && !authorizationService.canRemoveFromCollection(authenticatedUser, collection)) {
        	throw new TdarAuthorizationException("resourceCollectionService.could_not_remove");
        } 
        else {
            removeFromCollection(resource, collection, type);
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#deleteForController(org.tdar.core.bean.collection.VisibleCollection, java.lang.String,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void deleteForController(ResourceCollection persistable, String deletionReason, TdarUser authenticatedUser) {
        // should I do something special?
        for (Resource resource : persistable.getManagedResources()) {
            removeFromCollection(resource, persistable, CollectionResourceSection.MANAGED);
            getDao().saveOrUpdate(resource);
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
        }
        for (Resource resource : persistable.getUnmanagedResources()) {
            removeFromCollection(resource, persistable, CollectionResourceSection.UNMANAGED);
            getDao().saveOrUpdate(resource);
            publisher.publishEvent(new TdarEvent(resource, EventType.CREATE_OR_UPDATE));
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getDeletionIssues(com.opensymphony.xwork2.TextProvider,
     * org.tdar.core.bean.collection.ResourceCollection)
     */
    @Override
    @Transactional(readOnly = true)
    public DeleteIssue getDeletionIssues(TextProvider provider, ResourceCollection persistable) {
        List<ResourceCollection> findAllChildCollections = findDirectChildCollections(persistable.getId(), null);
        if (CollectionUtils.isNotEmpty(findAllChildCollections)) {
            getLogger().info("we still have children: {}", findAllChildCollections);
            DeleteIssue issue = new DeleteIssue();
            issue.getRelatedItems().addAll(findAllChildCollections);
            issue.setIssue(provider.getText("resourceCollectionService.cannot_delete_collection"));
            return issue;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#saveCollectionForController(org.tdar.core.service.CollectionSaveObject)
     */
    @Override
    @Transactional(readOnly = false)
    public void saveCollectionForController(CollectionSaveObject cso) {
        ResourceCollection persistable = cso.getCollection();
        TdarUser authenticatedUser = cso.getUser();
        if (persistable == null) {
            throw new TdarRecoverableRuntimeException();
        }
        logger.debug("{} - {}", persistable, persistable.getId());
        if (PersistableUtils.isTransient(persistable)) {
            Permissions perm = Permissions.ADMINISTER_COLLECTION;

            persistable.getAuthorizedUsers().add(new AuthorizedUser(authenticatedUser, authenticatedUser, perm));
        }
        RevisionLogType type = RevisionLogType.CREATE;
        if (PersistableUtils.isNotTransient(persistable)) {
            type = RevisionLogType.EDIT;
        }
        saveOrUpdate(persistable);
        List<Resource> resourcesToRemove = getDao().findAll(Resource.class, cso.getToRemove());
        List<Resource> resourcesToAdd = getDao().findAll(Resource.class, cso.getToAdd());
        List<Resource> publicToRemove = getDao().findAll(Resource.class, cso.getPublicToRemove());
        List<Resource> publicToAdd = getDao().findAll(Resource.class, cso.getPublicToAdd());

        reconcileIncomingResourcesForCollection(persistable, authenticatedUser, resourcesToAdd, resourcesToRemove, CollectionResourceSection.MANAGED);
        reconcileIncomingResourcesForCollection(persistable, authenticatedUser, publicToAdd, publicToRemove, CollectionResourceSection.UNMANAGED);

        // saveAuthorizedUsersForResourceCollection(persistable, persistable, cso.getAuthorizedUsers(), cso.isShouldSave(), authenticatedUser,type);
        simpleFileProcessingDao.processFileProxyForCreatorOrCollection(persistable.getProperties(),
                cso.getFileProxy());

        if (!Objects.equals(cso.getParentId(), persistable.getParentId())) {
            updateCollectionParentTo(authenticatedUser, persistable, cso.getParent());
        }

        if (!Objects.equals(cso.getAlternateParentId(), persistable.getAlternateParentId())) {
            logger.debug("updating alternate parent for {} from {} to {}", persistable.getId(), persistable.getAlternateParent(), cso.getAlternateParent());
            updateAlternateCollectionParentTo(authenticatedUser, persistable, cso.getAlternateParent());
        }

        String msg = String.format("%s modified %s", authenticatedUser, persistable.getTitle());
        CollectionRevisionLog revision = new CollectionRevisionLog(msg, persistable, authenticatedUser, type);
        revision.setTimeBasedOnStart(cso.getStartTime());
        getDao().saveOrUpdate(revision);
        publisher.publishEvent(new TdarEvent(persistable, EventType.CREATE_OR_UPDATE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#makeResourcesInCollectionActive(org.tdar.core.bean.collection.ResourceCollection,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void makeResourcesInCollectionActive(ResourceCollection col, TdarUser person) {
        if (!authorizationService.canEditCollection(person, col)) {
            throw new TdarAuthorizationException("resourceCollectionService.make_active_permissions");
        }
        getDao().makeResourceInCollectionActive(col, person);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getRandomFeaturedCollection()
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceCollection getRandomFeaturedCollection() {
        return getDao().findRandomFeaturedCollection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getWhiteLabelCollectionForResource(org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = true)
    public ResourceCollection getWhiteLabelCollectionForResource(Resource resource) {
        return getDao().getWhiteLabelCollectionForResource(resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findCollectionsWithName(org.tdar.core.bean.entity.TdarUser, java.lang.String,
     * java.lang.Class)
     */
    @Override
    public ResourceCollection findCollectionsWithName(TdarUser user, String name) {
        boolean isAdmin = authorizationService.isEditor(user);
        return getDao().findCollectionWithName(user, isAdmin, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#convertToWhitelabelCollection(org.tdar.core.bean.collection.CustomizableCollection)
     */
    @Override
    @Transactional
    /**
     * Convert a resource collection into a persisted white-label collection with all default values.
     * Note that this has the effect of detaching the input collection from the session.
     * 
     * @param rc
     * @return
     */
    public ResourceCollection convertToWhitelabelCollection(ResourceCollection rc) {
        if (rc.getProperties() != null && rc.getProperties().getWhitelabel()) {
            return rc;
        }
        return getDao().convertToWhitelabelCollection(rc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#convertToResourceCollection(org.tdar.core.bean.collection.CustomizableCollection)
     */
    @Override
    @Transactional
    /**
     * Detach the provided white-label collection and return a persisted resource collection object.
     *
     * @param wlc
     * @return
     */
    public ResourceCollection convertToResourceCollection(ResourceCollection wlc) {
        return getDao().convertToResourceCollection(wlc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#changeSubmitter(org.tdar.core.bean.collection.ResourceCollection,
     * org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void changeSubmitter(ResourceCollection collection, TdarUser submitter, TdarUser authenticatedUser) {
        getDao().changeSubmitter(collection, submitter, authenticatedUser);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#moveResource(org.tdar.core.bean.resource.Resource, C, C,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void moveResource(Resource resource, ResourceCollection fromCollection, ResourceCollection toCollection, TdarUser tdarUser) {
        if (!authorizationService.canEdit(tdarUser, resource) || !authorizationService.canEdit(tdarUser, fromCollection)
                || !authorizationService.canEdit(tdarUser, toCollection)) {
            throw new TdarAuthorizationException("resourceCollectionService.insufficient_rights");
        }
        resource.getManagedResourceCollections().remove(fromCollection);
        (fromCollection).getManagedResources().remove(resource);
        resource.getManagedResourceCollections().add(toCollection);
        (toCollection).getManagedResources().add(resource);

        getDao().saveOrUpdate(resource);
        saveOrUpdate(fromCollection);
        saveOrUpdate(toCollection);

    }

    private void removeFromCollection(Resource resource, ResourceCollection collection, CollectionResourceSection type) {
        if (type == CollectionResourceSection.MANAGED) {
            resource.getManagedResourceCollections().remove(collection);
            // ((SharedCollection)collection).getResources().remove(resource);
        } else {
            resource.getUnmanagedResourceCollections().remove(collection);
            // ((ListCollection)collection).getUnmanagedResources().remove(resource);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#getSchemaOrgJsonLD(org.tdar.core.bean.collection.VisibleCollection)
     */
    @Override
    @Transactional(readOnly = true)
    public String getSchemaOrgJsonLD(ResourceCollection resource) throws IOException {
        SchemaOrgCollectionTransformer transformer = new SchemaOrgCollectionTransformer();
        return transformer.convert(serializationService, resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#saveCollectionForRightsController(C, org.tdar.core.bean.entity.TdarUser, java.util.List,
     * java.lang.Class, java.lang.Long)
     */
    @Override
    @Transactional(readOnly = false)
    public void saveCollectionForRightsController(ResourceCollection c, TdarUser authenticatedUser, List<UserRightsProxy> proxies, Long startTime) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<>();
        List<UserInvite> invites = new ArrayList<>();
        userRightsProxyService.convertProxyToItems(proxies, authenticatedUser, authorizedUsers, invites);
        RevisionLogType edit = RevisionLogType.EDIT;
        saveAuthorizedUsersForResourceCollection(c, c, authorizedUsers, true, authenticatedUser, edit);

        String msg = String.format("%s modified rights on %s", authenticatedUser, (c).getTitle());
        CollectionRevisionLog revision = new CollectionRevisionLog(msg, c, authenticatedUser, edit);
        revision.setTimeBasedOnStart(startTime);
        getDao().saveOrUpdate(revision);
        c.markUpdated(authenticatedUser);
        userRightsProxyService.handleInvites(authenticatedUser, invites, c);
        saveOrUpdate(c);
        publisher.publishEvent(new TdarEvent(c, EventType.CREATE_OR_UPDATE));

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findResourcesSharedWith(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Resource> findResourcesSharedWith(TdarUser authenticatedUser, TdarUser user) {
        boolean admin = false;
        if (authorizationService.isEditor(authenticatedUser)) {
            admin = true;
        }
        return getDao().findResourcesSharedWith(authenticatedUser, user, admin);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findCollectionsSharedWith(org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.entity.TdarUser, java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findCollectionsSharedWith(TdarUser authenticatedUser, TdarUser user) {
        boolean admin = false;
        if (authorizationService.isEditor(authenticatedUser)) {
            admin = true;
        }
        return getDao().findCollectionsSharedWith(authenticatedUser, user, Permissions.MODIFY_RECORD, admin);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findUsersSharedWith(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public List<TdarUser> findUsersSharedWith(TdarUser authenticatedUser) {
        return getDao().findUsersSharedWith(authenticatedUser);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#saveResourceRights(java.util.List, org.tdar.core.bean.entity.TdarUser,
     * org.tdar.core.bean.resource.Resource)
     */
    @Override
    @Transactional(readOnly = false)
    public void saveResourceRights(List<UserRightsProxy> proxies, TdarUser authenticatedUser, Resource resource) {
        List<UserInvite> invites = new ArrayList<>();
        List<AuthorizedUser> authorizedUsers = new ArrayList<>();
        userRightsProxyService.convertProxyToItems(proxies, authenticatedUser, authorizedUsers, invites);
        saveAuthorizedUsersForResource(resource, authorizedUsers, true, authenticatedUser);

        userRightsProxyService.handleInvites(authenticatedUser, invites, resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.collection.ResourceCollectionService#findAlternateChildren(java.util.List, org.tdar.core.bean.entity.TdarUser,
     * java.lang.Class)
     */
    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAlternateChildren(List<Long> ids, TdarUser authenticatedUser) {
        List<ResourceCollection> findAlternateChildren = getDao().findAlternateChildren(ids);
        if (CollectionUtils.isNotEmpty(findAlternateChildren)) {
            findAlternateChildren.forEach(c -> {
                authorizationService.applyTransientViewableFlag(c, authenticatedUser);
            });
        }
        return findAlternateChildren;
    }

    @Override
    public ResourceCollection find(long l) {
        return super.find(l);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceCollection> getCollectionsForResourceAndUser(Resource resource, TdarUser user) {
        logger.debug("getCollectionsForResourceAndUser: Getting collections for  {}  {}", resource, user);

        List<ResourceCollection> resourceCollections = new ArrayList<ResourceCollection>();
        getLogger().trace("parent/ owner collections");

        // There's No javadoc for on getManagedResourceCollections.
        for (ResourceCollection rc : resource.getManagedResourceCollections()) {
            // Please verify the permission.
            if (authorizationService.canEdit(user, rc)) {
                resourceCollections.add(rc);
            }
        }

        logger.debug("resourceCollections: {}", resourceCollections);

        return resourceCollections;
    }

    @Override
    @Transactional(readOnly = false)
    public ResourceCollection createNewResourceCollection(String collectionName, TdarUser user) {
        logger.debug("createNewResourceCollection: Creating new collection: {} for {}", collectionName, user);
        ResourceCollection rc = new ResourceCollection(collectionName, "", user);
        rc.markUpdated(user);
        rc.getAuthorizedUsers().add(new AuthorizedUser(user, user, Permissions.ADMINISTER_COLLECTION));
        getDao().saveOrUpdate(rc);
        logger.debug("New collection id:{}", rc.getId());
        return rc;
    }
}
