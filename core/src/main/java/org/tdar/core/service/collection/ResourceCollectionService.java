package org.tdar.core.service.collection;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.UserRightsProxy;
import org.tdar.core.service.CollectionSaveObject;
import org.tdar.core.service.DeleteIssue;
import org.tdar.core.service.resource.ErrorHandling;

import com.opensymphony.xwork2.TextProvider;

public interface ResourceCollectionService {

    /**
     * Reconcile @link AuthorizedUser entries on a @link ResourceCollection, save if told to.
     * 
     * @param resource
     * @param authorizedUsers
     * @param shouldSave
     */
    void saveAuthorizedUsersForResource(Resource resource, List<AuthorizedUser> authorizedUsers, boolean shouldSave, TdarUser actor);

    /**
     * Get All @link AuthorizedUser entries for a @Link Resource based on all @link ResourceCollection entries.
     * 
     * @param resource
     * @param authenticatedUser
     * @return
     */
    List<AuthorizedUser> getAuthorizedUsersForResource(Resource resource, TdarUser authenticatedUser);

    /**
     * Find all collections that have no parent or have a parent that's hidden
     * 
     * @return
     */
    List<SharedCollection> findAllTopLevelCollections();

    /**
     * Find all direct child @link ResourceCollection entries of a @link ResourceCollection
     * 
     */
    List<SharedCollection> findDirectChildCollections(Long id, Boolean hidden);

    /**
     * Delete the @link ResourceCollection and it's @link AuthorizedUser entries.
     * 
     * @param resourceCollection
     */
    void delete(ResourceCollection resourceCollection);

    /**
     * Save the incoming @link AuthorizedUser entries for the @link ResourceCollection resolving as needed, avoiding duplicates
     * 
     * @param resourceCollection
     * @param incomingUsers
     * @param shouldSaveResource
     * @param type
     */
    void saveAuthorizedUsersForResourceCollection(HasAuthorizedUsers source, ResourceCollection resourceCollection,
            Collection<AuthorizedUser> incomingUsers,
            boolean shouldSaveResource, TdarUser actor, RevisionLogType type);

    /**
     * Find root @link ResourceCollection entries for a @link Person (shown on the dashboard).
     * 
     * @param person
     * @return
     */
    List<SharedCollection> findParentOwnerCollections(Person person);

    /**
     * Find all @link ResourceCollection entries that are potential parents of the specified @link ResourceCollection
     * 
     * @param person
     * @param collection
     * @return
     */
    List<SharedCollection> findPotentialParentCollections(Person person, SharedCollection collection);

    void saveResourceCollections(Resource resource, Collection<SharedCollection> incoming, Set<SharedCollection> current,
            TdarUser authenticatedUser, boolean shouldSave, ErrorHandling errorHandling, CollectionType type);

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
    void addResourceCollectionToResource(Resource resource, Set<SharedCollection> current, TdarUser authenticatedUser,
            boolean shouldSave,
            ErrorHandling errorHandling, SharedCollection collection, CollectionType type);

    /**
     * Find all @link ResourceCollection entries (shared only)
     * 
     * @return
     */
    List<SharedCollection> findAllResourceCollections();

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
    TreeSet<SharedCollection> buildCollectionTreeForController(SharedCollection collection, TdarUser authenticatedUser);

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
    List<SharedCollection> findAllChildCollectionsOnly(SharedCollection collection);

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
     Set<SharedCollection> findFlattenedCollections(Person user, GeneralPermissions generalPermissions);

    /**
     * Return the root resource collection of the provided resource collection. This method also populates the
     * transient children resource collection for every node in the tree.
     * 
     * @param anyNode
     * @return
     */
    SharedCollection getFullyInitializedRootResourceCollection(SharedCollection anyNode, TdarUser authenticatedUser);

    /**
     * Find all @link ResourceCollection entries that were both public and shared.
     * 
     * @return
     */
    List<Long> findAllPublicActiveCollectionIds();

    /**
     * Find all @link ResourceCollection entries that have the specified @link Status.
     * 
     * @param persistable
     * @param statuses
     * @return
     */
    List<Resource> findAllResourcesWithStatus(ResourceCollection persistable, Status... statuses);

    /**
     * Get all @link AuthorizedUser entries for a collection (could be recursive up through parents)
     * 
     * @param persistable
     * @param authenticatedUser
     * @return
     */
    List<AuthorizedUser> getAuthorizedUsersForCollection(ResourceCollection persistable, TdarUser authenticatedUser);

    /**
     * For each @link ResourceCollection in the collection if it's in the list of id's, remove it. For a set of id's try and build a single tree and avoid
     * duplicate trees. This is used by the two tree's in the Dashboard (your's and shared).
     * 
     * @param collection
     * @param authenticatedUser
     * @param collectionIds
     */
    void reconcileCollectionTree(Collection<SharedCollection> collection, TdarUser authenticatedUser, List<Long> collectionIds);

    /**
     * Return a read-only list of sparse Resource objects that belong to a ResourceCollection with the specified ID
     * The only populated fields are Resource.id, Resource.title, and Resource.resourceType.
     * 
     * @param collectionId
     *            id of the ResourceCollection that contains the resources returned by this method
     * @return
     */
    List<Resource> findCollectionSparseResources(Long collectionId);

    /**
     * Find aggregate view count for collection
     * 
     * @param persistable
     * @return
     */
    Long getCollectionViewCount(ResourceCollection persistable);

    void updateCollectionParentTo(TdarUser authorizedUser, SharedCollection persistable, SharedCollection parent);

    void updateAlternateCollectionParentTo(TdarUser authorizedUser, SharedCollection persistable,
            SharedCollection hierarchicalCollection);

    List<SharedCollection> getAllChildCollections(SharedCollection persistable);

    void addUserToInternalCollection(Resource resource, TdarUser authenticatedUser, TdarUser user, GeneralPermissions permission);

    Set<SharedCollection> getEffectiveSharesForResource(Resource resource);

    Set<SharedCollection> getEffectiveResourceCollectionsForResource(Resource resource);

    void reconcileIncomingResourcesForCollection(SharedCollection persistable, TdarUser authenticatedUser, List<Resource> resourcesToAdd,
            List<Resource> resourcesToRemove);

    void removeResourceFromCollection(Resource resource, ResourceCollection collection, TdarUser authenticatedUser, CollectionType type);

    void reconcileIncomingResourcesForCollectionWithoutRights(SharedCollection persistable, TdarUser authenticatedUser, List<Resource> resourcesToAdd,
            List<Resource> resourcesToRemove);

    void deleteForController(SharedCollection persistable, String deletionReason, TdarUser authenticatedUser);

    DeleteIssue getDeletionIssues(TextProvider provider, ResourceCollection persistable);

    void saveCollectionForController(CollectionSaveObject cso);

    void makeResourcesInCollectionActive(ResourceCollection col, TdarUser person);

    ResourceCollection getRandomFeaturedCollection();

    ResourceCollection getWhiteLabelCollectionForResource(Resource resource);

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
    SharedCollection findCollectionsWithName(TdarUser user, String name);

    /**
     * Convert a resource collection into a persisted white-label collection with all default values.
     * Note that this has the effect of detaching the input collection from the session.
     * 
     * @param rc
     * @return
     */
    ResourceCollection convertToWhitelabelCollection(ResourceCollection rc);

    /**
     * Detach the provided white-label collection and return a persisted resource collection object.
     *
     * @param wlc
     * @return
     */
    ResourceCollection convertToResourceCollection(ResourceCollection wlc);

    void changeSubmitter(ResourceCollection collection, TdarUser submitter, TdarUser authenticatedUser);

    void moveResource(Resource resource, SharedCollection fromCollection, SharedCollection toCollection, TdarUser tdarUser);

    String getSchemaOrgJsonLD(ResourceCollection resource) throws IOException;

    void saveCollectionForRightsController(SharedCollection c, TdarUser authenticatedUser,
            List<UserRightsProxy> proxies, Long startTime);

    List<Resource> findResourcesSharedWith(TdarUser authenticatedUser, TdarUser user);

    List<SharedCollection> findCollectionsSharedWith(TdarUser authenticatedUser, TdarUser user);

    List<TdarUser> findUsersSharedWith(TdarUser authenticatedUser);

    void saveResourceRights(List<UserRightsProxy> proxies, TdarUser authenticatedUser, Resource resource);

    List<SharedCollection> findAlternateChildren(List<Long> ids, TdarUser authenticatedUser);

    ResourceCollection find(long l);

}