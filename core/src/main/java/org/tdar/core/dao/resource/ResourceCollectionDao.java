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
import org.tdar.core.bean.collection.CollectionType;
import org.tdar.core.bean.collection.CustomizableCollection;
import org.tdar.core.bean.collection.DownloadAuthorization;
import org.tdar.core.bean.collection.HierarchicalCollection;
import org.tdar.core.bean.collection.HomepageFeaturedCollections;
import org.tdar.core.bean.collection.ListCollection;
import org.tdar.core.bean.collection.RequestCollection;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.collection.VisibleCollection;
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
import org.tdar.core.configuration.TdarConfiguration;
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
    public <C extends HierarchicalCollection> List<C> findCollectionsOfParent(Long parent, Boolean visible, Class<C> cls) {
        String q = TdarNamedQueries.QUERY_SHARED_COLLECTION_BY_PARENT;
        if (CollectionType.getTypeForClass(cls) == CollectionType.LIST) {
            q = TdarNamedQueries.QUERY_LIST_COLLECTION_BY_PARENT;
        }
        Query<C> namedQuery = getCurrentSession().createNamedQuery(q, cls);
        namedQuery.setParameter("parent", parent);
        namedQuery.setParameter("visible", visible);
        return namedQuery.getResultList();
    }

    public List<ResourceCollection> findPublicCollectionsWithHiddenParents() {
        Query<ResourceCollection> namedQuery = getCurrentSession().createNamedQuery(TdarNamedQueries.QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT,
                ResourceCollection.class);
        return namedQuery.getResultList();
    }

    public <C extends HierarchicalCollection> List<C> findParentOwnerCollections(Person person, Class<C> cls) {
        String q = TdarNamedQueries.QUERY_SHARED_COLLECTION_BY_AUTH_OWNER;
        GeneralPermissions base = GeneralPermissions.ADMINISTER_SHARE;
        if (CollectionType.getTypeForClass(cls) == CollectionType.LIST) {
            q = TdarNamedQueries.QUERY_LIST_COLLECTION_BY_AUTH_OWNER;
            base = GeneralPermissions.ADMINISTER_GROUP;
        }
        Query<C> namedQuery = getCurrentSession().createNamedQuery(q, cls);
        Long id = -1L;
        if (PersistableUtils.isNotNullOrTransient(person)) {
            id = person.getId();
        }
        namedQuery.setParameter("authOwnerId", id);
        namedQuery.setParameter("collectionTypes", Arrays.asList(CollectionType.getTypeForClass(cls)));
        namedQuery.setParameter("equivPerm", base.getEffectivePermissions() - 1);
        try {
            List<C> list = namedQuery.getResultList();
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

    public <C extends VisibleCollection> C findCollectionWithName(TdarUser user, boolean isAdmin, String name, Class<C> cls) {
        String q = TdarNamedQueries.QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME;
        GeneralPermissions base = GeneralPermissions.ADMINISTER_SHARE;
        if (cls.isAssignableFrom(ListCollection.class)) {
            q = TdarNamedQueries.QUERY_LIST_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME;
            base = GeneralPermissions.ADMINISTER_GROUP;
        }
        Query<C> query = getCurrentSession().createNamedQuery(q, cls);
        query.setParameter("name", name);
        List<C> list = query.getResultList();
        logger.trace("found: {} ", list);
        for (C coll : list) {
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

    public <C extends ResourceCollection> List<C> findInheritedCollections(Person user, GeneralPermissions generalPermissions, Class<C> cls) {
        if (PersistableUtils.isTransient(user)) {
            return Collections.<C>emptyList();
        }
        int permission = generalPermissions.getEffectivePermissions() - 1;
        String q = TdarNamedQueries.SHARED_COLLECTION_LIST_WITH_AUTHUSER;
        if (cls.isAssignableFrom(ListCollection.class)) {
            q = TdarNamedQueries.LIST_COLLECTION_LIST_WITH_AUTHUSER;
        }
        Query<C> query = getCurrentSession().createNamedQuery(q, cls);
        query.setParameter("effectivePermission", permission);
        query.setParameter("userId", user.getId());
        return query.getResultList();
    }

//    @SuppressWarnings("unchecked")
    public <C extends ResourceCollection> Set<C> findFlattendCollections(Person user, GeneralPermissions generalPermissions, Class<C> cls) {
        Set<C> allCollections = new HashSet<>();

        // get all collections that grant explicit edit permissions to person
        List<C> collections = findInheritedCollections(user, generalPermissions, cls);

        for (C rc : collections) {
            if (rc instanceof SharedCollection) {
                List<SharedCollection> find = findAllChildCollectionsOnly((SharedCollection) rc, SharedCollection.class);
                allCollections.addAll((Collection<? extends C>) find);
            }
            if (rc instanceof ListCollection) {
                List<ListCollection> find = findAllChildCollectionsOnly((ListCollection) rc, ListCollection.class);
                allCollections.addAll((Collection<? extends C>) find);
            }
            allCollections.add(rc);
        }

        return allCollections;
    }

    public <E extends HierarchicalCollection<?>> List<E> findAllChildCollectionsOnly(E collection, Class<E> cls) {
        List<E> allChildren = getAllChildCollections(collection, cls);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <E extends HierarchicalCollection> List<E> getAllChildCollections(E persistable, Class<E> cls) {
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

    public ScrollableResults findAllResourcesInCollectionAndSubCollectionScrollable(HierarchicalCollection persistable) {
        if (PersistableUtils.isNullOrTransient(persistable)) {
            return null;
        }
        String q = TdarNamedQueries.QUERY_SHARED_COLLECTION_CHILDREN_RESOURCES;
        if (persistable instanceof ListCollection) {
            q = TdarNamedQueries.QUERY_LIST_COLLECTION_CHILDREN_RESOURCES;
        }
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

    public CustomizableCollection<?> getWhiteLabelCollectionForResource(Resource resource) {
        Set<CustomizableCollection<?>> resourceCollections = new HashSet<>();
        if (TdarConfiguration.getInstance().isListCollectionsEnabled()) {
            resourceCollections.addAll(resource.getUnmanagedResourceCollections());
        } else {
            resourceCollections.addAll(resource.getSharedCollections());
        }

        List<CustomizableCollection<?>> whiteLabelCollections = new ArrayList<>();
        for (CustomizableCollection<?> rc : resourceCollections) {
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
    public <C extends CustomizableCollection> C convertToWhitelabelCollection(C rc) {
        if (rc.getProperties() == null) {
            rc.setProperties(new CollectionDisplayProperties(false,false,false,false,false,false));
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
    public <C extends CustomizableCollection> C convertToResourceCollection(C wlc) {
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
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_DOWNLOAD_AUTHORIZATION);
        query.setParameter("collectionId", collection.getId());
        for (DownloadAuthorization da : (List<DownloadAuthorization>)query.list()) {
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
            Object[] obj = (Object[])obj_;
            //id, title, status, resource_type
            Long id = ((Number)obj[0]).longValue();
            String title = (String)obj[1];
            Status status = Status.valueOf((String)obj[2]);
            ResourceType type = ResourceType.valueOf((String)obj[3]);
            try {
                Resource newInstance = type.getResourceClass().newInstance();
                newInstance.setId(id);
                newInstance.setTitle(title);
                newInstance.setStatus(status);
                resources.add(newInstance);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.debug("{}",e);
            }
        }
        return resources;
        
    }

    public <C extends ResourceCollection> List<SharedCollection> findCollectionsSharedWith(TdarUser authenticatedUser, TdarUser user, Class<C> cls, GeneralPermissions perm , boolean admin) {
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
        logger.trace("user: {}",authenticatedUser);
        Query<BigInteger> shared = getCurrentSession().createNativeQuery(TdarNamedQueries.QUERY_USERS_SHARED_WITH);
        shared.setParameter("userId", authenticatedUser.getId());
//        shared.setParameter("permission", GeneralPermissions.MODIFY_RECORD.getEffectivePermissions() - 1);
        logger.trace(TdarNamedQueries.QUERY_USERS_SHARED_WITH);
        List<BigInteger> resultList = shared.getResultList();
        if (CollectionUtils.isEmpty(resultList)) {
            return new ArrayList<>();
        }
        logger.trace("# userIds: {}",resultList.size());
        List<Long> lst = new ArrayList<>();
        resultList.forEach(i -> {lst.add(i.longValue());});
        List<TdarUser> users = new ArrayList<>(findAll(TdarUser.class, lst));
//        logger.debug("users: {}",users);
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

    public <C extends HierarchicalCollection> List<C> findAlternateChildren(List<Long> ids, Class<C> cls) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.EMPTY_LIST;
        }
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_ALTERNATE_CHILDRENS);
        query.setParameterList("collectionIds", ids);
        return query.list();
    }

    public <C extends HierarchicalCollection> List<C> getAlternateChildrenTrees(Collection<C> allChildren, C collection, Class<C> cls) {
        List<Long> ids = PersistableUtils.extractIds(allChildren);
        String qs = TdarNamedQueries.FIND_ALTERNATE_CHILDRENS_TREE;
        if (collection instanceof ListCollection) {
            qs = TdarNamedQueries.FIND_ALTERNATE_LIST_CHILDRENS_TREE;
        }
        ids.add(collection.getId());
        Query query = getCurrentSession().getNamedQuery(qs);
        query.setParameterList("collectionIds", ids);
        return query.list();
    }

    public Collection<HasAuthorizedUsers> findExpiringUsers(Date date) {
        ArrayList<HasAuthorizedUsers> toReturn = new ArrayList<>();
        Query query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_EXPIRING_AUTH_USERS_FOR_COLLECTION);
        query.setParameter("date", date);
        toReturn.addAll(query.list());
        query = getCurrentSession().getNamedQuery(TdarNamedQueries.FIND_EXPIRING_AUTH_USERS_FOR_RESOURCE);
        query.setParameter("date", date);
        toReturn.addAll(query.list());
        
        return toReturn;
    }
    
}
