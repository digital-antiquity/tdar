package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.VisibleCollection;
import org.tdar.core.bean.collection.InternalCollection;
import org.tdar.core.bean.collection.RightsBasedResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.utils.PersistableUtils;

/**
 * pull out the logic to get the rights info for a resource into a single spot for indexing.
 * 
 * @author abrin
 *
 */
public class ResourceRightsExtractor {
    
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private Resource resource;

    public ResourceRightsExtractor(Resource resource) {
        this.resource = resource;
        extract();
    }

    private HashSet<Long> directCollectionIds = new HashSet<>();;
    private HashSet<Long> collectionIds = new HashSet<>();;
    private HashSet<Long> allCollectionIds = new HashSet<>();;
    private HashSet<String> collectionNames = new HashSet<>();;
    private Set<String> directCollectionNames = new HashSet<>();

    

    /*
     * this function should introduce into the index all of the people who can
     * modify a record which is useful for limiting things on the project page
     */
    public List<Long> getUsersWhoCanModify() {
        List<Long> users = new ArrayList<Long>();
        HashSet<TdarUser> writable = new HashSet<>();
        writable.add(resource.getSubmitter());
        writable.add(resource.getUpdatedBy());
        for (RightsBasedResourceCollection collection : resource.getRightsBasedResourceCollections()) {
            writable.addAll(CollectionRightsExtractor.getUsersWhoCan(collection, GeneralPermissions.MODIFY_METADATA, true));
        }
        for (TdarUser p : writable) {
            if (PersistableUtils.isNullOrTransient(p)) {
                continue;
            }
            users.add(p.getId());
        }
        // FIXME: decide whether right should inherit from projects (1) of (2)
        // change see authorizedUserDao
        // sb.append(getAdditionalUsersWhoCanModify());
        logger.trace("effectiveUsers:" + users);
        return users;
    }

    /*
     * this function should introduce into the index all of the people who can
     * modify a record which is useful for limiting things on the project page
     */
    public List<Long> getUsersWhoCanView() {
        List<Long> users = new ArrayList<Long>();
        HashSet<TdarUser> writable = new HashSet<>();
        writable.add(resource.getSubmitter());
        writable.add(resource.getUpdatedBy());
        for (RightsBasedResourceCollection collection : resource.getRightsBasedResourceCollections()) {
            writable.addAll(CollectionRightsExtractor.getUsersWhoCan(collection, GeneralPermissions.VIEW_ALL, true));
        }
        for (TdarUser p : writable) {
            if (PersistableUtils.isNullOrTransient(p)) {
                continue;
            }
            users.add(p.getId());
        }
        // FIXME: decide whether right should inherit from projects (1) of (2)
        // change see authorizedUserDao
        // sb.append(getAdditionalUsersWhoCanModify());
        logger.trace("effectiveUsers:" + users);
        return users;
    }

    public void extract() {
        Set<RightsBasedResourceCollection> collections = new HashSet<>(resource.getRightsBasedResourceCollections());
        for (RightsBasedResourceCollection collection : collections) {
            if (collection instanceof SharedCollection) {
                directCollectionIds.add(collection.getId());
                if (collection instanceof VisibleCollection) {
                    directCollectionNames.add(((VisibleCollection) collection).getName());
                }
                if (collection instanceof SharedCollection) {
                SharedCollection shared = (SharedCollection)collection;
                collectionNames.addAll(shared.getParentNameList());
                collectionIds.addAll(shared.getParentIds());
                }
            } else if (collection instanceof InternalCollection) {
                allCollectionIds.add(collection.getId());
            }
        }
        collectionIds.addAll(directCollectionIds);
        allCollectionIds.addAll(collectionIds);
    }

    public HashSet<Long> getDirectCollectionIds() {
        return directCollectionIds;
    }

    public void setDirectCollectionIds(HashSet<Long> directCollectionIds) {
        this.directCollectionIds = directCollectionIds;
    }

    public HashSet<Long> getCollectionIds() {
        return collectionIds;
    }

    public void setCollectionIds(HashSet<Long> collectionIds) {
        this.collectionIds = collectionIds;
    }

    public HashSet<Long> getAllCollectionIds() {
        return allCollectionIds;
    }

    public void setAllCollectionIds(HashSet<Long> allCollectionIds) {
        this.allCollectionIds = allCollectionIds;
    }

    public HashSet<String> getCollectionNames() {
        return collectionNames;
    }

    public void setCollectionNames(HashSet<String> collectionNames) {
        this.collectionNames = collectionNames;
    }

    public Set<String> getDirectCollectionNames() {
        return directCollectionNames;
    }

    public void setDirectCollectionNames(Set<String> directCollectionNames) {
        this.directCollectionNames = directCollectionNames;
    }
    
}
