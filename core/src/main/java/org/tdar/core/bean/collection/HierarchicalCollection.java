package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface HierarchicalCollection<C extends ResourceCollection> {

    public C getParent();

    public void setParent(C parent);

    public Set<C> getTransientChildren();

    public void setTransientChildren(Set<C> transientChildren);

    public boolean isTopLevel();

    public Long getParentId();

    public List<C> getHierarchicalResourceCollections();

    public List<C> getVisibleParents();
    
    public boolean isTopCollection();

    public boolean isSubCollection();

    public default List<String> getParentNameList() {
        ArrayList<String> parentNameTree = new ArrayList<String>();
        for (C collection : getHierarchicalResourceCollections()) {
            parentNameTree.add(collection.getName());
        }
        return parentNameTree;
    }

    public default List<C> getVisibleParents(Class<C> type) {
        List<C> hierarchicalResourceCollections = getHierarchicalResourceCollections();
        Iterator<C> iterator = hierarchicalResourceCollections.iterator();
        while (iterator.hasNext()) {
            C collection = iterator.next();
            if (!(type.isAssignableFrom(collection.getClass())) || !collection.isHidden()) {
                iterator.remove();
            }
        }
        return hierarchicalResourceCollections;
    }

    public Set<Long> getParentIds();
}
