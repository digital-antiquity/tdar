package org.tdar.core.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.utils.PersistableUtils;


public class ResourceCollectionSaveHelper {

    private Set<ResourceCollection> toDelete = new HashSet<>();
    private Set<ResourceCollection> toAdd = new HashSet<>();
    
    public ResourceCollectionSaveHelper(Collection<ResourceCollection> incoming,Collection<ResourceCollection> existing_, CollectionType type) {
        
        Set<ResourceCollection> existing = new HashSet<>(existing_);
        if (type != null) {
            Iterator<ResourceCollection> iterator = existing.iterator();
            while (iterator.hasNext()) {
                ResourceCollection c = iterator.next();
                if (c.getType() != type) {
                    iterator.remove();
                }
            }
        }
        
        Map<Long, ResourceCollection> idMap = PersistableUtils.createIdMap(existing);
        for (ResourceCollection in : incoming) {
            if (in == null) {
                continue;
            }

            if (!idMap.containsKey(in.getId())) {
                getToAdd().add(in);
            } else {
                idMap.remove(in.getId());
            }
        }
        for (Long id : idMap.keySet()) {
            getToDelete().add(idMap.get(id));
        }
    }

    public Set<ResourceCollection> getToAdd() {
        return toAdd;
    }

    public void setToAdd(Set<ResourceCollection> toAdd) {
        this.toAdd = toAdd;
    }

    public Set<ResourceCollection> getToDelete() {
        return toDelete;
    }

    public void setToDelete(Set<ResourceCollection> toDelete) {
        this.toDelete = toDelete;
    }
}