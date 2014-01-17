/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.dao.resource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.dao.Dao;

/**
 * @author Adam Brin
 * 
 */
@Component
public class ResourceCollectionDao extends Dao.HibernateBase<ResourceCollection> {

    public ResourceCollectionDao() {
        super(ResourceCollection.class);
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findCollectionsOfParent(Long parent, Boolean visible, CollectionType... collectionTypes) {
        // FIXME: nesting...
        Query namedQuery = getCurrentSession().getNamedQuery(QUERY_COLLECTION_BY_PARENT);
        namedQuery.setParameter("parent", parent);
        namedQuery.setParameterList("collectionTypes", collectionTypes);
        namedQuery.setParameter("visible", visible);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findPublicCollectionsWithHiddenParents() {
        Query namedQuery = getCurrentSession().getNamedQuery(QUERY_COLLECTION_PUBLIC_WITH_HIDDEN_PARENT);
        return namedQuery.list();
    }

    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findParentOwnerCollections(Person person, List<CollectionType> types) {
        Query namedQuery = getCurrentSession().getNamedQuery(QUERY_COLLECTION_BY_AUTH_OWNER);
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

    public ResourceCollection findCollectionWithName(Person user, ResourceCollection collection, GeneralPermissions permission) {
        Query query = getCurrentSession().getNamedQuery(QUERY_COLLECTIONS_YOU_HAVE_ACCESS_TO_WITH_NAME);// QUERY_PROJECT_EDITABLE
        query.setLong("userId", user.getId());
        query.setString("name", collection.getName());
        //FIXME: move all this 'permission-1' hoo-ha from the caller to the query
        query.setLong("effectivePermission", GeneralPermissions.ADMINISTER_GROUP.getEffectivePermissions() -1);
        @SuppressWarnings("unchecked")
        List<ResourceCollection> list = (List<ResourceCollection>) query.list();
        if(list.size() > 1 ) {
            logger.error("query found more than one resource collection: user:{}, coll:{}", user, collection);
        }
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Long> findAllPublicActiveCollectionIds() {
        Query query = getCurrentSession().getNamedQuery(QUERY_COLLECTIONS_PUBLIC_ACTIVE);
        return query.list();
    }

    @SuppressWarnings("unchecked")
    public List<Resource> findAllResourcesWithStatus(ResourceCollection persistable, Status[] statuses) {
        Query query = getCurrentSession().getNamedQuery(QUERY_COLLECTION_RESOURCES_WITH_STATUS);
        query.setParameterList("ids", Arrays.asList(persistable.getId()));
        query.setParameterList("statuses", Arrays.asList(statuses));
        return (List<Resource>) query.list();
    }

    public List<ResourceCollection> findInheritedCollections(Person user, GeneralPermissions generalPermissions) {
        int permission = generalPermissions.getEffectivePermissions() - 1;
        Query query = getCurrentSession().getNamedQuery(COLLECTION_LIST_WITH_AUTHUSER);
        query.setInteger("effectivePermission", permission);
        query.setLong("userId", user.getId());
        return query.list();
    }

    public Set<ResourceCollection> findFlattendCollections(Person user, GeneralPermissions generalPermissions) {
        Set<ResourceCollection>allCollections = new HashSet<>();

        //get all collections that grant explicit edit permissions to person
        List<ResourceCollection> collections = findInheritedCollections(user, generalPermissions);

        for(ResourceCollection rc : collections) {
            allCollections.addAll(findAllChildCollectionsOnly(rc, ResourceCollection.CollectionType.SHARED));
            allCollections.add(rc);
        }

        return allCollections;
    }

    public List<ResourceCollection> findAllChildCollectionsOnly(ResourceCollection collection, CollectionType collectionType) {
        List<ResourceCollection> collections = new LinkedList<>();
        List<ResourceCollection> toEvaluate = new LinkedList<>();
        toEvaluate.add(collection);
        while (!toEvaluate.isEmpty()) {
            ResourceCollection child = toEvaluate.get(0);
            collections.add(child);
            toEvaluate.remove(0);
            toEvaluate.addAll(findCollectionsOfParent(child.getId(), null, collectionType));
        }
        return collections;
    }

}
