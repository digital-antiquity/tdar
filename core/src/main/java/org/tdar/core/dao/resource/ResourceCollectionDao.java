/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.dao.resource;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.CollectionDisplayProperties;
import org.tdar.core.bean.collection.DownloadAuthorization;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.HomepageFeaturedCollections;
import org.tdar.core.bean.collection.RequestCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.HasAuthorizedUsers;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceRevisionLog;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.RevisionLogType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.dao.base.HibernateBase;
import org.tdar.core.dao.entity.AuthorizedUserDao;
import org.tdar.utils.PersistableUtils;

/**
 * @author Adam Brin
 * 
 */
@Component
public class ResourceCollectionDao extends HibernateBase<ResourceCollection> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AuthorizedUserDao authorizedUserDao;

    public ResourceCollectionDao() {
        super(ResourceCollection.class);
    }

    /**
     * @return
     */
    public  List<SharedCollection> findCollectionsOfParent(Long parent, Boolean visible) {
        String q = TdarNamedQueries.QUERY_SHARED_COLLECTION_BY_PARENT;
        Query<SharedCollection> namedQuery = getCurrentSession().createNamedQuery(q, SharedCollection.class);
        namedQuery.setParameter("parent", parent);
        namedQuery.setParameter("visible", visible);
        return namedQuery.getResultList();
    }

    public List<ResourceCollection> findPublicCollectionsWithHiddenParents() {
        Query<ResourceCollection> namedQuery = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT,
                ResourceCollection.class);
        return namedQuery.getResultList();
    }

    public List<SharedCollection> findParentOwnerCollections(Person person) {
        String q = TdarNamedQueries.QUERY_SHARED_COLLECTION_BY_AUTH_OWNER;
        GeneralPermissions base = GeneralPermissions.ADMINISTER_SHARE;

        Query<SharedCollection> namedQuery = getCurrentSession().createNamedQuery(q, SharedCollection.class);
        Long id = -1L;
        if (PersistableUtils.isNotNullOrTransient(person)) {
            id = person.getId();
        }
        namedQuery.setParameter("authOwnerId", id);
        namedQuery.setParameter("equivPerm", base.getEffectivePermissions() - 1);
        try {
            List<SharedCollection> list = namedQuery.getResultList();
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
    public List<SharedCollection> findAllSharedResourceCollections() {
        return findAll(SharedCollection.class);
    }

    public SharedCollection findCollectionWithName(TdarUser user, boolean isAdmin, String name) {
        String q = TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME;
        GeneralPermissions base = GeneralPermissions.ADMINISTER_SHARE;
        Query<SharedCollection> query = getCurrentSession().createNamedQuery(q, SharedCollection.class);
        query.setParameter("name", name);
        List<SharedCollection> list = query.getResultList();
        logger.trace("found: {} ", list);
        for (SharedCollection coll : list) {
            if (isAdmin || authorizedUserDao.isAllowedTo(user, coll, base)) {
                return coll;
            }
        }
        return null;
    }

    public List<Long> findAllPublicActiveCollectionIds() {
        Query<Long> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTIONS_PUBLIC_ACTIVE, Long.class);
        return query.getResultList();
    }

    public List<Resource> findAllResourcesWithStatus(ResourceCollection persistable, Status[] statuses) {
        Query<Resource> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_RESOURCES_WITH_STATUS, Resource.class);
        query.setParameter("ids", Arrays.asList(persistable.getId()));
        query.setParameter("statuses", Arrays.asList(statuses));
        return query.getResultList();
    }

    public List<SharedCollection> findInheritedCollections(Person user, GeneralPermissions generalPermissions) {
        if (PersistableUtils.isTransient(user)) {
            return Collections.<SharedCollection> emptyList();
        }
        int permission = generalPermissions.getEffectivePermissions() - 1;
        String q = TdarNamedQueries.SHARED_COLLECTION_LIST_WITH_AUTHUSER;
        Query<SharedCollection> query = getCurrentSession().createNamedQuery(q, SharedCollection.class);
        query.setParameter("effectivePermission", permission);
        query.setParameter("userId", user.getId());
        return query.getResultList();
    }

    // @SuppressWarnings("unchecked")
    public Set<SharedCollection> findFlattendCollections(Person user, GeneralPermissions generalPermissions) {
        Set<SharedCollection> allCollections = new HashSet<>();

        // get all collections that grant explicit edit permissions to person
        List<SharedCollection> collections = findInheritedCollections(user, generalPermissions);

        for (SharedCollection rc : collections) {
            if (rc instanceof SharedCollection) {
                List<SharedCollection> find = findAllChildCollectionsOnly((SharedCollection) rc);
                allCollections.addAll(find);
            }
            allCollections.add(rc);
        }

        return allCollections;
    }

    public List<SharedCollection> findAllChildCollectionsOnly(SharedCollection collection) {
        List<SharedCollection> allChildren = getAllChildCollections(collection);
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

    public List<SharedCollection> getAllChildCollections(SharedCollection persistable) {
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
        for (SharedCollection rc : ir.getSharedResourceCollections()) {
            sharedCollectionIds.add(rc.getId());
            sharedCollectionIds.addAll(rc.getParentIds());
        }
        if (CollectionUtils.isEmpty(sharedCollectionIds)) {
            return Collections.EMPTY_LIST;
        }
        Query<DownloadAuthorization> query = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_HOSTED_DOWNLOAD_AUTHORIZATION,
                DownloadAuthorization.class);
        query.setParameter("collectionids", sharedCollectionIds);
        query.setParameter("apiKey", apiKey);
        query.setParameter("hostname", referrer.toLowerCase());
        return query.getResultList();
    }

    public ScrollableResults findAllResourcesInCollectionAndSubCollectionScrollable(SharedCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return null;
        }
        String q = TdarNamedQueries.QUERY_SHARED_COLLECTION_CHILDREN_RESOURCES;
        Query<Resource> query = getCurrentSession().createNamedQuery(q, Resource.class);
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
        List<Resource> resources = new ArrayList<>();
        for (Resource resource : query.getResultList()) {
            resource.markUpdated(person);
            resource.setStatus(Status.ACTIVE);
            ResourceRevisionLog rrl = new ResourceRevisionLog("Resource made Active", resource, person, RevisionLogType.EDIT);
            saveOrUpdate(rrl);
            resources.add(resource);
        }
        saveOrUpdate(resources);
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

    public ResourceCollection getWhiteLabelCollectionForResource(Resource resource) {
        Set<ResourceCollection> resourceCollections = new HashSet<>();
        resourceCollections.addAll(resource.getSharedCollections());

        List<ResourceCollection> whiteLabelCollections = new ArrayList<>();
        for (ResourceCollection rc : resourceCollections) {
            if (rc.getProperties() != null && rc.getProperties().getWhitelabel()) {
                whiteLabelCollections.add(rc);
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

    /**
     * Convert a resource collection into a white-label collection.
     * 
     * @param rc
     * @return
     */
    public SharedCollection convertToWhitelabelCollection(SharedCollection rc) {
        if (rc.getProperties() == null) {
            rc.setProperties(new CollectionDisplayProperties(false, false, false, false, false, false));
        }
        rc.getProperties().setWhitelabel(true);
        saveOrUpdate(rc);
        return rc;
    }

    /**
     * Detach the provided white-label collection and return a persisted resource collection object.
     *
     * @param wlc
     * @return
     */
    public SharedCollection convertToResourceCollection(SharedCollection wlc) {
        if (wlc.getProperties() == null) {
            return wlc;
        }
        wlc.getProperties().setWhitelabel(false);
        return wlc;
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

    public void deleteDownloadAuthorizations(ResourceCollection collection) {
        Query<DownloadAuthorization> query = getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_DOWNLOAD_AUTHORIZATION, DownloadAuthorization.class);
        query.setParameter("collectionId", collection.getId());
        for (DownloadAuthorization da : query.list()) {
            da.getRefererHostnames().clear();
            delete(da);
        }
    }

    public List<Resource> findResourcesSharedWith(TdarUser authenticatedUser, TdarUser user, boolean admin) {
        Query query = getCurrentSession().createSQLQuery(TdarNamedQueries.QUERY_RESOURCES_SHARED_WITH);
        query.setParameter("userId", user.getId());
        query.setParameter("ownerId", authenticatedUser.getId());
        List<Resource> resources = new ArrayList<>();
        for (Object obj_ : query.getResultList()) {
            Object[] obj = (Object[]) obj_;
            // id, title, status, resource_type
            Long id = ((Number) obj[0]).longValue();
            String title = (String) obj[1];
            Status status = Status.valueOf((String) obj[2]);
            ResourceType type = ResourceType.valueOf((String) obj[3]);
            try {
                Resource newInstance = type.getResourceClass().newInstance();
                newInstance.setId(id);
                newInstance.setTitle(title);
                newInstance.setStatus(status);
                resources.add(newInstance);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.debug("{}", e);
            }
        }
        return resources;

    }

    public List<SharedCollection> findCollectionsSharedWith(TdarUser authenticatedUser, TdarUser user, GeneralPermissions perm, boolean admin) {
        Query<SharedCollection> shared = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO, SharedCollection.class);
        shared.setParameter("userId", authenticatedUser.getId());
        shared.setParameter("perm", perm.getEffectivePermissions() - 1);
        List<SharedCollection> resultList = shared.getResultList();
        List<Long> ids = PersistableUtils.extractIds(resultList);
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }

        Query<SharedCollection> query = getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_COLLECTIONS_SHARED_WITH, SharedCollection.class);
        query.setParameter("user", user);
        query.setParameter("admin", admin);
        query.setParameter("collectionIds", ids);
        return query.getResultList();
    }

    /**
     * Return a list of users that are assigned rights to collections created by the specified user.
     *
     * @param authenticatedUser
     * @return
     */
    public List<TdarUser> findUsersSharedWith(TdarUser authenticatedUser) {
        logger.trace("user: {}", authenticatedUser);
        Query<BigInteger> shared = getCurrentSession().createNativeQuery(TdarNamedQueries.QUERY_USERS_SHARED_WITH);
        shared.setParameter("userId", authenticatedUser.getId());
        logger.debug(TdarNamedQueries.QUERY_USERS_SHARED_WITH);
        List<BigInteger> resultList = shared.getResultList();
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        logger.trace("# userIds: {}", resultList.size());
        Set<Long> lst = new HashSet<>();
        resultList.forEach(i -> {
            lst.add(i.longValue());
        });

        List<TdarUser> users = new ArrayList<>(findAll(TdarUser.class, lst));

        return users;
    }

    public List<UserInvite> findUserInvites(HasAuthorizedUsers c) {
        if (c instanceof ResourceCollection) {
            return findUserInvites((ResourceCollection) c);
        }
        if (c instanceof Resource) {
            return findUserInvites((Resource) c);
        }
        return null;
    }

    public List<UserInvite> findUserInvites(ResourceCollection resourceCollection) {
        return getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_USERINVITES_BY_COLLECTION, UserInvite.class)
                .setParameter("collection", resourceCollection)
                .list();

    }

    public List<UserInvite> findUserInvites(TdarUser user) {
        return getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_USERINVITES_BY_USER, UserInvite.class)
                .setParameter("user", user)
                .list();
    }

    public List<UserInvite> findUserInvites(Resource resource) {
        return getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_USERINVITES_BY_RESOURCE, UserInvite.class)
                .setParameter("resource", resource)
                .list();
    }

    public RequestCollection findCustomRequest(Resource resource) {
        List<Long> ids = PersistableUtils.extractIds(resource.getSharedCollections());
        for (RequestCollection rc : findAll(RequestCollection.class)) {
            if (CollectionUtils.containsAny(rc.getCollections(), ids)) {
                return rc;
            }
        }
        return null;
    }

    public List<SharedCollection> findAlternateChildren(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        Query<SharedCollection> query = getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_ALTERNATE_CHILDRENS,SharedCollection.class);
        query.setParameterList("collectionIds", ids);
        return query.list();
    }

    public  List<SharedCollection> getAlternateChildrenTrees(Collection<SharedCollection> allChildren, SharedCollection collection) {
        List<Long> ids = PersistableUtils.extractIds(allChildren);
        String qs = TdarNamedQueries.FIND_ALTERNATE_CHILDRENS_TREE;

        ids.add(collection.getId());
        Query<SharedCollection> query = getCurrentSession().createNamedQuery(qs,SharedCollection.class);
        query.setParameterList("collectionIds", ids);
        return query.list();
    }

    public Collection<HasAuthorizedUsers> findExpiringUsers(Date date) {
        ArrayList<HasAuthorizedUsers> toReturn = new ArrayList<>();
        Query<ResourceCollection> query = getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_EXPIRING_AUTH_USERS_FOR_COLLECTION,ResourceCollection.class);
        query.setParameter("date", date);
        toReturn.addAll(query.list());
        Query<Resource> query2 = getCurrentSession().createNamedQuery(TdarNamedQueries.FIND_EXPIRING_AUTH_USERS_FOR_RESOURCE,Resource.class);
        query2.setParameter("date", date);
        toReturn.addAll(query2.list());

        return toReturn;
    }

}
