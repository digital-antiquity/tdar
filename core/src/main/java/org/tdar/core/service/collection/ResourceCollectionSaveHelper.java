package org.tdar.core.service.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.utils.PersistableUtils;

public class ResourceCollectionSaveHelper<C extends ResourceCollection> {

    private Set<C> toDelete = new HashSet<>();
    private Set<C> toAdd = new HashSet<>();

    @SuppressWarnings("unchecked")
    public ResourceCollectionSaveHelper(Collection<C> incoming, Collection<? extends ResourceCollection> existing_, Class<C> cls) {

        Set<C> existing = new HashSet<>();
        Iterator<? extends ResourceCollection> iterator = existing_.iterator();
        while (iterator.hasNext()) {
            ResourceCollection c = iterator.next();
            if (c.getClass().isAssignableFrom(cls)) {
                existing.add((C)c);
            }
        }

        Map<Long, C> idMap = PersistableUtils.createIdMap(existing);
        for (C in : incoming) {
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

    public Set<C> getToAdd() {
        return toAdd;
    }

    public void setToAdd(Set<C> toAdd) {
        this.toAdd = toAdd;
    }

    public Set<C> getToDelete() {
        return toDelete;
    }

    public void setToDelete(Set<C> toDelete) {
        this.toDelete = toDelete;
    }
}
