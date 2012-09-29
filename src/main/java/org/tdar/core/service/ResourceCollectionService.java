package org.tdar.core.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.NotNullPredicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.dao.resource.ResourceCollectionDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.ResourceService.ErrorHandling;
import org.tdar.search.query.SortOption;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
@Service
public class ResourceCollectionService extends ServiceInterface.TypedDaoBase<ResourceCollection, ResourceCollectionDao> {

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
    public void saveAuthorizedUsersForResourceCollection(ResourceCollection resourceCollection, List<AuthorizedUser> authorizedUsers) {
        saveAuthorizedUsersForResourceCollection(resourceCollection, authorizedUsers, true);
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

        // internalCollection.getResources().add(resource);
        if (CollectionUtils.isNotEmpty(authorizedUsers)) {
            for (AuthorizedUser incomingUser : authorizedUsers) {
                if (incomingUser == null) {
                    continue;
                }

                incomingUser.setResourceCollection(resourceCollection);

                if (!Persistable.Base.isNullOrTransient(incomingUser.getUser())) {
                    Person user = getDao().find(Person.class, incomingUser.getUser().getId());
                    if (user != null) {
                        // it's important to ensure that we replace the proxy user w/ the persistent user prior to calling isValid(), because isValid()
                        // may evaluate fields that aren't set in the proxy object.
                        incomingUser.setUser(user);
                        if (!incomingUser.isValid()) {
                            continue;
                        }
                        // FIXME: not sure this is needed, but because hashCode doesn't include generalPermissions
                        // best to be safe
                        if (currentUsers.contains(incomingUser)) {
                            currentUsers.remove(incomingUser);
                        }
                        currentUsers.add(incomingUser);
                        if (shouldSaveResource)
                            getDao().saveOrUpdate(incomingUser);
                    }
                }
            }
        }
        // CollectionUtils.removeAll(currentUsers, Collections.);
        logger.debug("users after save: {}", currentUsers);
        if (shouldSaveResource)
            getDao().saveOrUpdate(resourceCollection);
    }
    
    @Transactional
    public Set<ResourceCollection> findAllEditableCollections(Person person) {
        Set<ResourceCollection> allEditableCollections = new HashSet<ResourceCollection>(getDao().findExplicitlyAuthorizedCollections(person, CollectionType.SHARED));
        // problem here is that all of the potential children may have other collections as children, and these may be "valid" collections for our tree
        Set<ResourceCollection> childCollections = new HashSet<ResourceCollection>();
        for (ResourceCollection potentialCollection: allEditableCollections) {
            childCollections.addAll(findAllChildCollectionsRecursive(potentialCollection));
        }
        allEditableCollections.addAll(childCollections);
        return allEditableCollections;
    }

    @Transactional(readOnly = true)
    public List<ResourceCollection> findExplicitlyAuthorizedCollections(Person person) {
        return getDao().findExplicitlyAuthorizedCollections(person, CollectionType.SHARED);
    }

    /**
     * Returns a set of ResourceCollections that are viable parents for the given ResourceCollection which are the 
     * set of all editable ResourceCollections, excluding any descendants of the given ResourceCollection to avoid cycles.
     * 
     * @param person
     * @param collection
     * @return
     */
    @Transactional(readOnly = true)
    public Set<ResourceCollection> findPotentialParentCollections(Person person, ResourceCollection collection) {
        Set<ResourceCollection> allEditableCollections = findAllEditableCollections(person);
        // remove any potential collections that may be descendants of the parameterized ResourceCollection
        Iterator<ResourceCollection> iterator = allEditableCollections.iterator();
        while (iterator.hasNext()) {
            ResourceCollection parent = iterator.next();
            while (parent != null) {
                // checking for recursive loops both direct children or grandchildren...
                if (parent.equals(collection)) {
                    logger.trace("removing {} from parent list to prevent infinite loops", collection);
                    iterator.remove();
                    break;
                }
                parent = parent.getParent();
            }
        }
        return allEditableCollections;
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

    public List<ResourceCollection> findAllChildCollectionsRecursive(ResourceCollection collection) {
        return findAllChildCollectionsRecursive(collection, CollectionType.SHARED);
    }

    public List<ResourceCollection> findAllChildCollectionsRecursive(ResourceCollection collection, CollectionType collectionType) {
        List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
        LinkedList<ResourceCollection> toEvaluate = new LinkedList<ResourceCollection>();
        toEvaluate.add(collection);
        while (!toEvaluate.isEmpty()) {
            ResourceCollection child = toEvaluate.removeFirst();
            collections.add(child);
            toEvaluate.addAll(findAllDirectChildCollections(child.getId(), null, collectionType));
        }
        return collections;
    }
    
    /**
     * Returns all of the authorized users for a collection, including the explicit permissions on the specified collection along with any permisssions
     * inherited from the collection's ancestors.  
     * 
     * @param collection
     * @return
     */
    public List<AuthorizedUser>getEffectiveAuthorizedUsers(ResourceCollection collection) {
       List<AuthorizedUser> authUsers = new ArrayList<AuthorizedUser>();
       for(ResourceCollection ancestor : collection.getHierarchicalResourceCollections()) {
           authUsers.addAll(ancestor.getAuthorizedUsers());
       }
       
       return authUsers;
    }
    
    /**
     * return copy of inherited authusers
     * @return
     */
    public List<AuthorizedUser> getTransientInheritedAuthorizedUsers(ResourceCollection collection) {
        List<AuthorizedUser> authusers = new ArrayList<AuthorizedUser>();
        if(collection.isRoot()) return authusers;
        
        List<AuthorizedUser> effectiveAuthusers = getEffectiveAuthorizedUsers(collection.getParent());
        for(AuthorizedUser authuser: effectiveAuthusers) {
            AuthorizedUser transientAuthuser = new AuthorizedUser(authuser.getUser(), authuser.getGeneralPermission());
            authusers.add(transientAuthuser);
        }
        return authusers;
    }

    
}
