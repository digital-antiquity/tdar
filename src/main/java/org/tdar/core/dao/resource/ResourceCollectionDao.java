/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.core.dao.resource;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.dao.Dao;

/**
 * @author Adam Brin
 * 
 */
@Component
public class ResourceCollectionDao extends Dao.HibernateBase<ResourceCollection> {

    /**
     * @param persistentClass
     */
    public ResourceCollectionDao() {
        super(ResourceCollection.class);
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<ResourceCollection> findCollectionsOfParent(Long parent, Boolean visible, CollectionType... collectionTypes) {
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
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return
     */
    public List<ResourceCollection> findAllSharedResourceCollections() {
        return findByCriteria(getDetachedCriteria().add(Restrictions.eq("type", CollectionType.SHARED)));
    }

}
