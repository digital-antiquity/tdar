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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.AuthenticationAndAuthorizationService;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.search.query.SortOption;

/**
 * @author Adam Brin
 * 
 */

@Service
public class ResourceCollectionService extends ServiceInterface.TypedDaoBase<ResourceCollection, ResourceCollectionDao> {

    @Autowired
    AuthenticationAndAuthorizationService authenticationAndAuthorizationService;

    @Transactional
    public List<Resource> reconcileIncomingResourcesForCollection(ResourceCollection persistable, Person authenticatedUser, List<Resource> resources) {
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
        // toEvaluate should retain the "new" resources to the collection

        // set the deleted resources aside first
        List<Resource> deletedResources = findAllResourcesWithStatus(persistable, Status.DELETED, Status.FLAGGED, Status.DUPLICATE,
                Status.FLAGGED_ACCOUNT_BALANCE);

        persistable.getResources().removeAll(toRemove);
        saveOrUpdate(persistable);
        List<Resource> rehydratedIncomingResources = getDao().loadFromSparseEntities(toEvaluate, Resource.class);
        logger.info("{} ", authenticatedUser);
        for (Resource resource : rehydratedIncomingResources) {
            if (!authenticationAndAuthorizationService.canEditResource(authenticatedUser, resource)) {
                ineligibleResources.add(resource);
            } else {
                resource.getResourceCollections().add(persistable);
            }
        }
        // remove all of the undesirable resources that that the user just tried to add
        rehydratedIncomingResources.removeAll(ineligibleResources);
        // getResourceCollectionService().findAllChildCollectionsRecursive(persistable, CollectionType.SHARED);
        persistable.getResources().addAll(rehydratedIncomingResources);

        // add all the deleted resources that were already in the colleciton
        persistable.getResources().addAll(deletedResources);
        saveOrUpdate(persistable);
        if (ineligibleResources.size() > 0) {
            throw new TdarRecoverableRuntimeException(
                    "the following resources could not be added to the collection because you do not have the rights to add them: " + ineligibleResources);
        }
        return rehydratedIncomingResources;
    }

    @Transactional
    public void saveAuthorizedUsersForResource(Resource resource, List<AuthorizedUser> authorizedUsers, boolean shouldSave) {
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
            internalCollection = new ResourceCollection();
            internalCollection.setType(CollectionType.INTERNAL);
            internalCollection.setOwner(resource.getSubmitter());
            resource.getResourceCollections().add(internalCollection);
            // internalCollection.getResources().add(resource); // WATCH -- may cause failure, if so, remove
            if (shouldSave) {
                getDao().saveOrUpdate(internalCollection);
                getDao().refresh(internalCollection);
            }
        }
        // note: we assume here that the authorizedUser validation will happen in saveAuthorizedUsersForResourceCollection
        saveAuthorizedUsersForResourceCollection(internalCollection, authorizedUsers, shouldSave);
        // if (CollectionUtils.isNotEmpty(internalCollection.getAuthorizedUsers())) {
        // resource.getResourceCollections().remove(internalCollection);
        // getDao().delete(internalCollection);
        // }
    }

    public List<AuthorizedUser> getAuthorizedUsersForResource(Resource resource) {
        List<AuthorizedUser> authorizedUsers = new ArrayList<AuthorizedUser>();

        for (ResourceCollection collection : resource.getResourceCollections()) {
            if (collection.getType() == CollectionType.INTERNAL) {
                authorizedUsers.addAll(collection.getAuthorizedUsers());
            }
        }

        return authorizedUsers;
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
     * @param id
     * @param public
     * @return
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAllDirectChildCollections(Long id, Boolean visible, CollectionType... type) {
        return getDao().findCollectionsOfParent(id, visible, type);
    }

    @Transactional
    public void delete(ResourceCollection resourceCollection) {
        getDao().delete(resourceCollection.getAuthorizedUsers());
        getDao().delete(resourceCollection);
    }

    @Transactional
    public void saveAuthorizedUsersForResourceCollection(ResourceCollection resourceCollection, List<AuthorizedUser> authorizedUsers, boolean shouldSaveResource) {
        if (resourceCollection == null) {
            throw new TdarRecoverableRuntimeException("could not save resource collection ... null");
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
                addUserToCollection(shouldSaveResource, currentUsers, incomingUser);
            }
        }
        // CollectionUtils.removeAll(currentUsers, Collections.);
        logger.debug("users after save: {}", currentUsers);
        if (shouldSaveResource)
            getDao().saveOrUpdate(resourceCollection);
    }

    private void addUserToCollection(boolean shouldSaveResource, Set<AuthorizedUser> currentUsers, AuthorizedUser incomingUser) {
        if (Persistable.Base.isNotNullOrTransient(incomingUser.getUser())) {
            Person user = getDao().find(Person.class, incomingUser.getUser().getId());
            if (user != null) {
                // it's important to ensure that we replace the proxy user w/ the persistent user prior to calling isValid(), because isValid()
                // may evaluate fields that aren't set in the proxy object.
                incomingUser.setUser(user);
                if (!incomingUser.isValid()) {
                    return;
                }

                currentUsers.add(incomingUser);
                if (shouldSaveResource)
                    getDao().saveOrUpdate(incomingUser);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ResourceCollection> findParentOwnerCollections(Person person) {
        return getDao().findParentOwnerCollections(person, Arrays.asList(CollectionType.SHARED));
    }

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

    @Transactional
    public void saveSharedResourceCollections(Resource resource, Collection<ResourceCollection> incoming, Set<ResourceCollection> current,
            Person authenticatedUser, boolean shouldSave, ErrorHandling errorHandling) {
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
            }
        }

        for (ResourceCollection collection : toRemove) {
            current.remove(collection);
            resource.getResourceCollections().remove(current);
        }

        for (ResourceCollection collection : incoming_) {
            ResourceCollection collectionToAdd = null;
            if (collection.isTransient()) {
                ResourceCollection potential = getDao().findCollectionsWithName(authenticatedUser, collection);
                if (potential != null) {
                    collectionToAdd = potential;
                } else {
                    collection.setOwner(authenticatedUser);
                    collection.setType(CollectionType.SHARED);
                    if (collection.getSortBy() == null) {
                        collection.setSortBy(SortOption.RESOURCE_TYPE);
                    }
                    collection.setVisible(true);
                    collectionToAdd = collection;
                }
            } else {
                collectionToAdd = find(collection.getId());
            }

            if (collectionToAdd != null && collectionToAdd.isValid()) {
                if (collectionToAdd.isTransient() && shouldSave) {
                    save(collectionToAdd);
                }
                // I think something in the next two lines is duplicitive
                current.add(collectionToAdd);
                collectionToAdd.getResources().add(resource);

                resource.getResourceCollections().add(collectionToAdd);
            } else {
                if (errorHandling == ErrorHandling.VALIDATE_WITH_EXCEPTION) {
                    throw new TdarRecoverableRuntimeException(collectionToAdd.getName() + " is not valid");
                }
            }
        }
        logger.debug("after save: {} ({})", current, current.size());

    }

    /**
     * @return
     */
    @Transactional(readOnly = true)
    public List<ResourceCollection> findAllResourceCollections() {
        return getDao().findAllSharedResourceCollections();
    }

    public List<ResourceCollection> findAllChildCollectionsRecursive(ResourceCollection collection, CollectionType shared) {
        List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
        List<ResourceCollection> toEvaluate = new ArrayList<ResourceCollection>();
        toEvaluate.add(collection);
        while (!toEvaluate.isEmpty()) {
            ResourceCollection child = toEvaluate.get(0);
            collections.add(child);
            toEvaluate.remove(0);
            toEvaluate.addAll(findAllDirectChildCollections(child.getId(), null, CollectionType.SHARED));
        }
        return collections;
    }

    public List<Long> findAllPublicActiveCollectionIds() {
        return getDao().findAllPublicActiveCollectionIds();
    }

    public List<Resource> findAllResourcesWithStatus(ResourceCollection persistable, Status... statuses) {
        return getDao().findAllResourcesWithStatus(persistable, statuses);
    }

}
