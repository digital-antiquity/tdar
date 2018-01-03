package org.tdar.core.service.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.utils.PersistableUtils;

public class ResourceCollectionSaveHelper {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private Set<ResourceCollection> toDelete = new HashSet<>();
    private Set<ResourceCollection> toAdd = new HashSet<>();

    @SuppressWarnings("unchecked")
    public ResourceCollectionSaveHelper(Collection<ResourceCollection> incoming, Collection<ResourceCollection> existing_) {

        Set<ResourceCollection> existing = new HashSet<>();
        Iterator<ResourceCollection> iterator = existing_.iterator();
        while (iterator.hasNext()) {
            ResourceCollection c = iterator.next();
            existing.add(c);
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
