package org.tdar.core.service.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.SharedCollection;
import org.tdar.utils.PersistableUtils;

public class ResourceCollectionSaveHelper {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private Set<SharedCollection> toDelete = new HashSet<>();
    private Set<SharedCollection> toAdd = new HashSet<>();

    @SuppressWarnings("unchecked")
    public ResourceCollectionSaveHelper(Collection<SharedCollection> incoming, Collection<SharedCollection> existing_) {

        
        Set<SharedCollection> existing = new HashSet<>();
        Iterator<SharedCollection> iterator = existing_.iterator();
        while (iterator.hasNext()) {
            SharedCollection c = iterator.next();
            existing.add(c);
        }

        Map<Long, SharedCollection> idMap = PersistableUtils.createIdMap(existing);
        for (SharedCollection in : incoming) {
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

    public Set<SharedCollection> getToAdd() {
        return toAdd;
    }

    public void setToAdd(Set<SharedCollection> toAdd) {
        this.toAdd = toAdd;
    }

    public Set<SharedCollection> getToDelete() {
        return toDelete;
    }

    public void setToDelete(Set<SharedCollection> toDelete) {
        this.toDelete = toDelete;
    }
}
