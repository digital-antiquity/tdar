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
import org.hibernate.Query;
import org.hibernate.SQLQuery;
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
    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findCollectionsOfParent(Long parent, Boolean visible, CollectionType... collectionTypes) {
        Query namedQuery = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTION_BY_PARENT);
        namedQuery.setParameter("parent", parent);
        namedQuery.setParameterList("collectionTypes", collectionTypes);
        namedQuery.setParameter("visible", visible);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findPublicCollectionsWithHiddenParents() {
        Query namedQuery = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findParentOwnerCollections(Person person, List<CollectionType> types) {
        Query namedQuery = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTION_BY_AUTH_OWNER);
        namedQuery.setParameter("authOwnerId", person.getId());
        namedQuery.setParameterList("collectionTypes", types);
        namedQuery.setParameter("equivPerm", GeneralPermissions.ADMINISTER_GROUP.getEffectivePermissions() - 1);
        try {
            List<ResourceCollection> list = namedQuery.list();
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

    @SuppressWarnings("unchecked")
    public ResourceCollection findCollectionWithName(TdarUser user, boolean isAdmin, ResourceCollection collection) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME);
        query.setString("name", collection.getName());
        List<ResourceCollection> list = query.list();
        for  (ResourceCollection coll : list) {
        	if (isAdmin || authorizedUserDao.isAllowedTo(user, coll, GeneralPermissions.ADMINISTER_GROUP)) {
        		return coll;
        	}
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findCollectionsWithName(TdarUser user, boolean isAdmin, String name) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME);
        query.setString("name", name);
        List<ResourceCollection> list = new ArrayList<>(query.list());
        list.removeIf( rc -> (
            !isAdmin && !authorizedUserDao.isAllowedTo(user, rc, GeneralPermissions.ADMINISTER_GROUP)   
        ));
        return list;

    }

    @SuppressWarnings("unchecked")
    public List<Long> findAllPublicActiveCollectionIds() {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTIONS_PUBLIC_ACTIVE);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findAllResourcesWithStatus(ResourceCollection persistable, Status[] statuses) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTION_RESOURCES_WITH_STATUS);
        query.setParameterList("ids", Arrays.asList(persistable.getId()));
        query.setParameterList("statuses", Arrays.asList(statuses));
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findInheritedCollections(Person user, GeneralPermissions generalPermissions) {
        if (PersistableUtils.isTransient(user)) {
            return Collections.EMPTY_LIST;
        }
        int permission = generalPermissions.getEffectivePermissions() - 1;
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.COLLECTION_LIST_WITH_AUTHUSER);
        query.setInteger("effectivePermission", permission);
        query.setLong("userId", user.getId());
        return query.list();
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
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_SPARSE_COLLECTION_RESOURCES);
        query.setLong("id", collectionId);
        return query.list();
    }

    public Long getCollectionViewCount(ResourceCollection persistable) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.COLLECTION_VIEW);
        query.setParameter("id", persistable.getId());
        Number result = (Number) query.uniqueResult();
        return result.longValue();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceCollection> getAllChildCollections(ResourceCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return Collections.EMPTY_LIST;
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTION_CHILDREN);
        query.setLong("id", persistable.getId());
        return query.list();
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
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_HOSTED_DOWNLOAD_AUTHORIZATION);
        query.setParameterList("collectionids", sharedCollectionIds);
        query.setParameter("apiKey", apiKey);
        query.setParameter("hostname", referrer.toLowerCase());
        return query.list();
    }

    public ScrollableResults findAllResourcesInCollectionAndSubCollectionScrollable(ResourceCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return null;
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTION_CHILDREN_RESOURCES);
        query.setLong("id", persistable.getId());
        return query.scroll();
    }

    public Long countAllResourcesInCollectionAndSubCollection(ResourceCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return null;
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.QUERY_COLLECTION_CHILDREN_RESOURCES_COUNT);
        query.setLong("id", persistable.getId());
        return (Long) query.uniqueResult();
    }

    public void makeResourceInCollectionActive(ResourceCollection col, TdarUser person) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.UPDATE_RESOURCE_IN_COLLECTION_TO_ACTIVE);
        query.setParameter("id", col.getId());
        List<?> resources = query.list();
        for (Object resource_ : resources) {
            Resource resource = (Resource)resource_;
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

    @SuppressWarnings("unchecked")
    public Set<AuthorizedUser> getUsersFromDb(ResourceCollection collection) {
        Query query = getNamedQuery(TdarNamedQueries.USERS_IN_COLLECTION);
        query.setLong("id", collection.getId());
        query.setReadOnly(true);
        query.list();

        HashSet<AuthorizedUser> set = new HashSet<AuthorizedUser>(query.list());
        detachFromSession(set);
        return set;
    }

    @SuppressWarnings("unchecked")
    public List<Long> findCollectionIdsWithTimeLimitedAccess() {
        Query query = getNamedQuery(TdarNamedQueries.COLLECTION_TIME_LIMITED_IDS);
        query.setReadOnly(true);
        return query.list();
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
        SQLQuery query = getCurrentSession().createSQLQuery(TdarNamedQueries.QUERY_SQL_CONVERT_COLLECTION_TO_WHITELABEL);
        query.setLong("id", id);
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
        SQLQuery query = getCurrentSession().createSQLQuery(TdarNamedQueries.QUERY_SQL_CONVERT_WHITELABEL_TO_COLLECTION);
        query.setLong("id", id);
        query.executeUpdate();
        ResourceCollection rc = find(ResourceCollection.class, id);
        return rc;
    }

    public void changeSubmitter(ResourceCollection collection, TdarUser submitter, TdarUser authenticatedUser) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.ALL_RESOURCES_IN_COLLECTION);
        query.setParameter("id", collection.getId());
        List<?> resources = query.list();
        for (Object resource_ : resources) {
            Resource resource = (Resource)resource_;
            resource.markUpdated(authenticatedUser);
            String msg = String.format("changed submitter from %s to %s ", resource.getSubmitter().toString(), submitter.toString());
            ResourceRevisionLog rrl = new ResourceRevisionLog(msg, resource, authenticatedUser, RevisionLogType.EDIT);
            resource.setSubmitter(submitter);
            saveOrUpdate(rrl);
            saveOrUpdate(resource);
        }
    }

    public void deleteDownloadAuthorizations(ResourceCollection collection) {
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_DOWNLOAD_AUTHORIZATION);
        query.setParameter("collectionId", collection.getId());
        for (DownloadAuthorization da : (List<DownloadAuthorization>)query.list()) {
            da.getRefererHostnames().clear();
            delete(da);
        }
    }
}
