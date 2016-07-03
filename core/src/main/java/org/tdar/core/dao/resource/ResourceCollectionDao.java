/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.dao.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.query.Query;
import org.hibernate.query.NativeQuery;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.DownloadAuthorization;
import org.tdar.core.bean.collection.HomepageFeaturedCollections;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.WhiteLabelCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.Dao;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.utils.PersistableUtils;

/**
 * @author Adam Brin
 * 
 */
@Component
public class ResourceCollectionDao extends Dao.HibernateBase<ResourceCollection> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AuthorizedUserDao authorizedUserDao;
    
    public ResourceCollectionDao() {
        super(ResourceCollection.class);
    }

    /**
     * @return
     */
    public List<ResourceCollection> findCollectionsOfParent(Long parent, Boolean visible, CollectionType ... collectionTypes) {
        Query<ResourceCollection> namedQuery = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_BY_PARENT,ResourceCollection.class);
        namedQuery.setParameter("parent", parent);
        namedQuery.setParameter("collectionTypes", Arrays.asList(collectionTypes));
        namedQuery.setParameter("visible", visible);
        return namedQuery.getResultList();
    }

    public List<ResourceCollection> findPublicCollectionsWithHiddenParents() {
        Query<ResourceCollection> namedQuery = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT, ResourceCollection.class);
        return namedQuery.getResultList();
    }

    public List<ResourceCollection> findParentOwnerCollections(Person person, List<CollectionType> types) {
        Query<ResourceCollection> namedQuery = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_BY_AUTH_OWNER, ResourceCollection.class);
        namedQuery.setParameter("authOwnerId", person.getId());
        namedQuery.setParameter("collectionTypes", types);
        namedQuery.setParameter("equivPerm", GeneralPermissions.ADMINISTER_GROUP.getEffectivePermissions() - 1);
        try {
            List<ResourceCollection> list = namedQuery.getResultList();
            logger.trace("{}", list);
            return list;
        } catch (Exception e) {
            logger.debug("cannot find parent owner collection:", e);
        }
        return null;
    }

    /**
     * @return
     */
    public List<ResourceCollection> findAllSharedResourceCollections() {
        return findByCriteria(getDetachedCriteria().add(Restrictions.eq("type", CollectionType.SHARED)));
    }

    public ResourceCollection findCollectionWithName(TdarUser user, boolean isAdmin, ResourceCollection collection) {
        Query<ResourceCollection> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME, ResourceCollection.class);
        query.setParameter("name", collection.getName());
        List<ResourceCollection> list = query.getResultList();
        for  (ResourceCollection coll : list) {
        	if (isAdmin || authorizedUserDao.isAllowedTo(user, coll, GeneralPermissions.ADMINISTER_GROUP)) {
        		return coll;
        	}
        }
        return null;
    }

    public List<ResourceCollection> findCollectionsWithName(TdarUser user, boolean isAdmin, String name) {
        Query<ResourceCollection> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME, ResourceCollection.class);
        query.setParameter("name", name);
        List<ResourceCollection> list = new ArrayList<>(query.getResultList());
        list.removeIf( rc -> (
            !isAdmin && !authorizedUserDao.isAllowedTo(user, rc, GeneralPermissions.ADMINISTER_GROUP)   
        ));
        return list;

    }

    public List<Long> findAllPublicActiveCollectionIds() {
        Query<Long> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTIONS_PUBLIC_ACTIVE, Long.class);
        return query.getResultList();
    }

    public List<Resource> findAllResourcesWithStatus(ResourceCollection persistable, Status[] statuses) {
        Query<Resource> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_RESOURCES_WITH_STATUS , Resource.class);
        query.setParameter("ids", Arrays.asList(persistable.getId()));
        query.setParameter("statuses", Arrays.asList(statuses));
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findInheritedCollections(Person user, GeneralPermissions generalPermissions) {
        if (PersistableUtils.isTransient(user)) {
            return Collections.EMPTY_LIST;
        }
        int permission = generalPermissions.getEffectivePermissions() - 1;
        Query<ResourceCollection> query = getCurrentSession().createNamedQuery(TdarNamedQueries.COLLECTION_LIST_WITH_AUTHUSER, ResourceCollection.class);
        query.setParameter("effectivePermission", permission);
        query.setParameter("userId", user.getId());
        return query.getResultList();
    }

    public Set<ResourceCollection> findFlattendCollections(Person user, GeneralPermissions generalPermissions) {
        Set<ResourceCollection> allCollections = new HashSet<>();

        // get all collections that grant explicit edit permissions to person
        List<ResourceCollection> collections = findInheritedCollections(user, generalPermissions);

        for (ResourceCollection rc : collections) {
            allCollections.addAll(findAllChildCollectionsOnly(rc, CollectionType.SHARED));
            allCollections.add(rc);
        }

        return allCollections;
    }

    public List<ResourceCollection> findAllChildCollectionsOnly(ResourceCollection collection, CollectionType collectionType) {
        List<ResourceCollection> allChildren = getAllChildCollections(collection);
        Iterator<ResourceCollection> iter = allChildren.iterator();
        while (iter.hasNext()) {
            ResourceCollection rc = iter.next();
            if (!rc.getType().equals(collectionType)) {
                iter.remove();
            }
        }
        return allChildren;
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findCollectionSparseResources(Long collectionId) {
        if (PersistableUtils.isNullOrTransient(collectionId)) {
            return Collections.EMPTY_LIST;
        }
        Query<Resource> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_SPARSE_COLLECTION_RESOURCES, Resource.class);
        query.setParameter("id", collectionId);
        return query.getResultList();
    }

    public Long getCollectionViewCount(ResourceCollection persistable) {
        Query<Number> query = getCurrentSession().createNamedQuery(TdarNamedQueries.COLLECTION_VIEW, Number.class);
        query.setParameter("id", persistable.getId());
        Number result = (Number) query.getSingleResult();
        return result.longValue();
    }

    @SuppressWarnings({"unchecked","rawtypes"})
    public List<ResourceCollection> getAllChildCollections(ResourceCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return Collections.EMPTY_LIST;
        }
        Query query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_CHILDREN);
        query.setParameter("id", persistable.getId());
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    /**
     * Return download authorizations that have a host name that matches the referrer and the APIKey that matches the one in the DB.
     * Does Hierarchical Collection Query
     * 
     * @param informationResourceFileVersion
     * @param apiKey
     * @param referrer
     * @return
     */
    public List<DownloadAuthorization> getDownloadAuthorizations(InformationResourceFileVersion informationResourceFileVersion, String apiKey,
            String referrer) {
        InformationResource ir = informationResourceFileVersion.getInformationResourceFile().getInformationResource();
        Set<Long> sharedCollectionIds = new HashSet<>();
        for (ResourceCollection rc : ir.getSharedResourceCollections()) {
            sharedCollectionIds.add(rc.getId());
            sharedCollectionIds.addAll(rc.getParentIds());
        }
        if (CollectionUtils.isEmpty(sharedCollectionIds)) {
            return Collections.EMPTY_LIST;
        }
        Query<DownloadAuthorization> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_HOSTED_DOWNLOAD_AUTHORIZATION, DownloadAuthorization.class);
        query.setParameter("collectionids", sharedCollectionIds);
        query.setParameter("apiKey", apiKey);
        query.setParameter("hostname", referrer.toLowerCase());
        return query.getResultList();
    }

    public ScrollableResults findAllResourcesInCollectionAndSubCollectionScrollable(ResourceCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return null;
        }
        Query<Resource> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_CHILDREN_RESOURCES, Resource.class);
        query.setParameter("id", persistable.getId());
        return query.scroll();
    }

    public Long countAllResourcesInCollectionAndSubCollection(ResourceCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return null;
        }
        Query<Number> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_CHILDREN_RESOURCES_COUNT, Number.class);
        query.setParameter("id", persistable.getId());
        return query.getSingleResult().longValue();
    }

    public void makeResourceInCollectionActive(ResourceCollection col, TdarUser person) {
        Query<Resource> query = getCurrentSession().createNamedQuery(TdarNamedQueries.UPDATE_RESOURCE_IN_COLLECTION_TO_ACTIVE, Resource.class);
        query.setParameter("id", col.getId());
        for (Resource resource : query.getResultList()) {
            resource.markUpdated(person);
            resource.setStatus(Status.ACTIVE);
            ResourceRevisionLog rrl = new ResourceRevisionLog("Resource made Active", resource, person, RevisionLogType.EDIT);
            saveOrUpdate(rrl);
            saveOrUpdate(resource);
        }
    }

    public ResourceCollection findRandomFeaturedCollection() {
        // use projection to just get the ID of the resource back -- less crazy binding in database queries
        Criteria criteria = getCurrentSession().createCriteria(HomepageFeaturedCollections.class, "h");
        criteria.createAlias("h.featured", "c");
        // criteria.add(Restrictions.eq("c.status", Status.ACTIVE));
        criteria.add(Restrictions.sqlRestriction("1=1 order by random()"));
        criteria.setMaxResults(1);
        try {
            return ((HomepageFeaturedCollections) criteria.uniqueResult()).getFeatured();
        } catch (NullPointerException npe) {
            return null;
        }
    }

    public WhiteLabelCollection getWhiteLabelCollectionForResource(Resource resource) {
        Set<ResourceCollection> resourceCollections = resource.getSharedResourceCollections();

        List<WhiteLabelCollection> whiteLabelCollections = new ArrayList<>();
        for (ResourceCollection rc : resourceCollections) {
            if (rc.isWhiteLabelCollection()) {
                whiteLabelCollections.add((WhiteLabelCollection) rc);
            }
        }
        if (whiteLabelCollections.size() > 1) {
            getLogger().warn("resource #{} belongs to more than one whitelabel collection: {}", resource.getId(), whiteLabelCollections);
        }

        if (CollectionUtils.isNotEmpty(whiteLabelCollections)) {
            return whiteLabelCollections.get(0);
        }
        return null;
    }

    public Set<AuthorizedUser> getUsersFromDb(ResourceCollection collection) {
        Query<AuthorizedUser> query = getNamedQuery(TdarNamedQueries.USERS_IN_COLLECTION, AuthorizedUser.class);
        query.setParameter("id", collection.getId());
        query.setReadOnly(true);
        query.getResultList();

        HashSet<AuthorizedUser> set = new HashSet<AuthorizedUser>(query.getResultList());
        detachFromSession(set);
        return set;
    }

    public List<Long> findCollectionIdsWithTimeLimitedAccess() {
        Query<Long> query = getNamedQuery(TdarNamedQueries.COLLECTION_TIME_LIMITED_IDS, Long.class);
        query.setReadOnly(true);
        return query.getResultList();
    }

    public void addToInternalCollection(Resource resource, TdarUser user, GeneralPermissions permission) {
        ResourceCollection internal = resource.getInternalResourceCollection();
        if (internal == null) {
            internal = createInternalResourceCollectionWithResource(resource.getSubmitter(), resource, true);
        }
        internal.getAuthorizedUsers().add(new AuthorizedUser(user, permission));
        saveOrUpdate(internal);
    }

    public ResourceCollection createInternalResourceCollectionWithResource(TdarUser owner, Resource resource, boolean shouldSave) {
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
            saveOrUpdate(internalCollection);
            refresh(internalCollection);
        }
        return internalCollection;
    }

    /**
     * Convert a resource collection into a white-label collection.
     * @param rc
     * @return
     */
    public WhiteLabelCollection convertToWhitelabelCollection(ResourceCollection rc) {
        Long id = rc.getId();
        detachFromSession(rc);
        NativeQuery query = getCurrentSession().createNativeQuery(TdarNamedQueries.QUERY_SQL_CONVERT_COLLECTION_TO_WHITELABEL);
        query.setParameter("id", id);
        query.executeUpdate();
        WhiteLabelCollection wlc = find(WhiteLabelCollection.class, id);
        return wlc;
    }

    /**
     * Detach the provided white-label collection and return a persisted resource collection object.
     *
     * @param wlc
     * @return
     */
    public ResourceCollection convertToResourceCollection(WhiteLabelCollection wlc) {
        Long id = wlc.getId();
        detachFromSession(wlc);
        NativeQuery query = getCurrentSession().createNativeQuery(TdarNamedQueries.QUERY_SQL_CONVERT_WHITELABEL_TO_COLLECTION);
        query.setParameter("id", id);
        query.executeUpdate();
        ResourceCollection rc = find(ResourceCollection.class, id);
        return rc;
    }

    public void changeSubmitter(ResourceCollection collection, TdarUser submitter, TdarUser authenticatedUser) {
        Query<Resource> query = getCurrentSession().createNamedQuery(TdarNamedQueries.ALL_RESOURCES_IN_COLLECTION, Resource.class);
        query.setParameter("id", collection.getId());
        for (Resource resource : query.getResultList()) {
            resource.markUpdated(authenticatedUser);
            String msg = String.format("changed submitter from %s to %s ", resource.getSubmitter().toString(), submitter.toString());
            ResourceRevisionLog rrl = new ResourceRevisionLog(msg, resource, authenticatedUser, RevisionLogType.EDIT);
            resource.setSubmitter(submitter);
            saveOrUpdate(rrl);
            saveOrUpdate(resource);
        }
    }
}
