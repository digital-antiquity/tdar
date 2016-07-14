package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

public interface HierarchicalCollection<C extends ResourceCollection&HasDisplayProperties> extends HasDisplayProperties{

    public C getParent();

    public void setParent(C parent);

    public Set<C> getTransientChildren();

    public void setTransientChildren(Set<C> transientChildren);

    public List<C> getHierarchicalResourceCollections();

    public List<C> getVisibleParents();
    

    @XmlTransient
    @Transient
    public default boolean isTopCollection() {
        return getParent() == null;
    }

    @XmlTransient
    @Transient
    public default boolean isSubCollection() {
        return getParent() != null;
    }


    @SuppressWarnings({ "unchecked", "hiding" })
    public default <C extends HierarchicalCollection> List<C> getHierarchicalResourceCollections(Class<C> class1, C collection_) {
        ArrayList<C> parentTree = new ArrayList<>();
        parentTree.add(collection_);
        C collection = collection_;
        while (collection.getParent() != null) {
            collection = (C) collection.getParent();
            parentTree.add(0, collection);
        }
        return parentTree;
    }

    @XmlTransient
    @Transient
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



    @XmlTransient
    @Transient
    public default boolean isTopLevel() {
        if ((getParent() == null) || (getParent().isHidden() == true)) {
            return true;
        }
        return false;
    }

    @XmlTransient
    @Transient
    public default Long getParentId() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getId();
    }

    /*
     * Default to sorting by name, but grouping by parentId, used for sorting int he tree
     */
    public default <C extends HierarchicalCollection> int compareTo(C self, C o) {
        List<String> tree = self.getParentNameList();
        List<String> tree_ = o.getParentNameList();
        while (!tree.isEmpty() && !tree_.isEmpty() && (tree.get(0) == tree_.get(0))) {
            tree.remove(0);
            tree_.remove(0);
        }
        if (tree.isEmpty()) {
            return -1;
        } else if (tree_.isEmpty()) {
            return 1;
        } else {
            return tree.get(0).compareTo(tree_.get(0));
        }
    }
}
