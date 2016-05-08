package org.tdar.search.converter;

import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;

public class ResourceRightsExtractor {

    private HashSet<Long> directCollectionIds = new HashSet<>();;
    private HashSet<Long> collectionIds = new HashSet<>();;
    private HashSet<Long> allCollectionIds = new HashSet<>();;
    private HashSet<String> collectionNames = new HashSet<>();;
    private Set<String> directCollectionNames = new HashSet<>();

    public void extract(Resource resource) {
        Set<ResourceCollection> collections = new HashSet<>(resource.getResourceCollections());
        collections.addAll(resource.getUnmanagedResourceCollections());
        for (ResourceCollection collection : collections) {
            if (collection.isShared()) {
                directCollectionIds.add(collection.getId());
                directCollectionNames.add(collection.getName());
                collectionIds.addAll(collection.getParentIds());
                collectionNames.addAll(collection.getParentNameList());
            } else if (collection.isInternal()) {
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
