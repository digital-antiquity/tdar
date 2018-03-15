package org.tdar.core.cache;

import java.util.HashSet;
import java.util.Set;

import org.tdar.core.bean.Editable;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.resource.Resource;

/**
 * Still a work in progress... but the goal of this would be to move all of the transient permission booleans out of resource and other beans and to move them here.  
 * This would be a ThreadLocal variable that would be used as a cache and exposed to freemarker to check the cache    
 * @author abrin
 *
 */
public class ThreadPermissionsCache {
    private boolean isAdmin = false;
    private Set<Long> editableResources = new HashSet<>();
    private Set<Long> viewableResources = new HashSet<>();
    private Set<Long> editableCollections = new HashSet<>();
    private Set<Long> viewableCollections = new HashSet<>();
    private Set<Long> managedResources = new HashSet<>();

    public ThreadPermissionsCache() {}
    
    public ThreadPermissionsCache(boolean editor) {
        setAdmin(true);
    }

    public boolean isEditable(Editable e) {
        if (isAdmin()) {
            return true;
        }

        if (e instanceof Resource) {
            return editableResources.contains(e.getId());
        }
        if (e instanceof ResourceCollection) {
            return editableCollections.contains(e.getId());
        }
        return false;
    }

    public boolean isManaged(Resource r) {
        return managedResources.contains(r.getId());
    }

    public boolean isManaged(Number r) {
        return managedResources.contains(r.longValue());
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Set<Long> getManagedResources() {
        return managedResources;
    }

    public void setManagedResources(Set<Long> managedResources) {
        this.managedResources = managedResources;
    }
}
