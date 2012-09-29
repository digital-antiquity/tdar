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
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.search.query.SortOption;

/**
 * @author Adam Brin
 * 
 */
@Transactional
@Service
public class ResourceCollectionService extends ServiceInterface.TypedDaoBase<ResourceCollection, ResourceCollectionDao> {

    Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    GenericDao genericDao;

    @Autowired
    ResourceCollectionDao resourceCollectionDao;

    @Autowired
    EntityService entityService;

    @Autowired
    public void setDao(ResourceCollectionDao dao) {
        super.setDao(dao);
    }

    @Transactional(readOnly = false)
    public void saveAuthorizedUsersForResource(Resource resource, List<AuthorizedUser> authorizedUsers, boolean shouldSave) {
        logger.info("saving authorized users...");

        Iterator<AuthorizedUser> iterator = authorizedUsers.iterator();
        while (iterator.hasNext()) {
            AuthorizedUser user = iterator.next();
            if (user == null || !user.isValidWithoutCollection()) {
                iterator.remove();
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
        for (ResourceCollection collection : resource.getResourceCollections()) {
            if (collection.getType() == CollectionType.INTERNAL) {
                internalCollection = collection;
                if (shouldSave) {
                    internalCollection = genericDao.merge(internalCollection);
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
                genericDao.saveOrUpdate(internalCollection);
                genericDao.refresh(internalCollection);
            }
        }

        saveAuthorizedUsersForResourceCollection(internalCollection, authorizedUsers, shouldSave);
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
    public List<ResourceCollection> findAllChildCollections(Long id, Boolean visible, CollectionType... type) {
        return getDao().findCollectionsOfParent(id, visible, type);
    }

    public void delete(ResourceCollection resourceCollection) {
        getDao().delete(resourceCollection.getAuthorizedUsers());
        getDao().delete(resourceCollection);
    }

    public void saveAuthorizedUsersForResourceCollection(ResourceCollection resourceCollection, List<AuthorizedUser> authorizedUsers, boolean shouldSaveResource) {
        if (resourceCollection == null) {
            throw new TdarRecoverableRuntimeException("could not save resource collection ... null");
        }
        Set<AuthorizedUser> currentUsers = resourceCollection.getAuthorizedUsers();
        logger.debug("current users (start): {}", currentUsers);
        logger.debug("incoming authorized users (start): {}", authorizedUsers);

        // the request may have edited the an existing authUser's permissions, so clear out the old set and go w/ most recent set.
        currentUsers.clear();

        // internalCollection.getResources().add(resource);
        if (CollectionUtils.isNotEmpty(authorizedUsers)) {
            for (AuthorizedUser incomingUser : authorizedUsers) {
                if (incomingUser == null) {
                    continue;
                }

                incomingUser.setResourceCollection(resourceCollection);
                if (!incomingUser.isValid()) {
                    continue;
                }

                if (incomingUser.getUser().getId() != null && incomingUser.getUser().getId() != -1) {
                    Person user = entityService.find(incomingUser.getUser().getId());
                    if (user != null) {
                        incomingUser.setUser(user);
                        // FIXME: not sure this is needed, but because hashCode doesn't include generalPermissions
                        // best to be safe
                        if (currentUsers.contains(incomingUser)) {
                            currentUsers.remove(incomingUser);
                        }
                        currentUsers.add(incomingUser);
                        if (shouldSaveResource)
                            genericDao.saveOrUpdate(incomingUser);
                    }
                }
            }
        }
        // CollectionUtils.removeAll(currentUsers, Collections.);
        logger.debug("users after save: {}", currentUsers);
        if (shouldSaveResource)
            genericDao.saveOrUpdate(resourceCollection);
    }

    public List<ResourceCollection> findParentOwnerCollections(Person person) {
        return resourceCollectionDao.findParentOwnerCollections(person, Arrays.asList(CollectionType.SHARED));
    }

    public List<ResourceCollection> findPotentialParentCollections(Person person, ResourceCollection collection) {
        List<ResourceCollection> potentialCollections = resourceCollectionDao.findParentOwnerCollections(person, Arrays.asList(CollectionType.SHARED));
        if (collection == null) {
            return potentialCollections;
        }
        Iterator<ResourceCollection> iterator = potentialCollections.iterator();
        while (iterator.hasNext()) {
            ResourceCollection parent = iterator.next();
            while (parent != null) {
                if (parent == collection) {
                    logger.trace("removing {} from parent list to prevent infinite loops", collection);
                    iterator.remove();
                    break;
                }
                parent = parent.getParent();
            }
        }
        return potentialCollections;
    }

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
                collection.setOwner(authenticatedUser);
                collection.setType(CollectionType.SHARED);
                if (collection.getSortBy() == null) {
                    collection.setSortBy(SortOption.RESOURCE_TYPE);
                }
                collection.setVisible(true);
                collectionToAdd = collection;
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
    public List<ResourceCollection> findAllResourceCollections() {
        return getDao().findAllSharedResourceCollections();
    }

}
