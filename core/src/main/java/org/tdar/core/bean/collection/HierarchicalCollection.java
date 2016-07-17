package org.tdar.core.bean.collection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

@Entity
public abstract class HierarchicalCollection<C extends VisibleCollection> extends VisibleCollection implements Comparable<C> {

    private static final long serialVersionUID = -4518328095223743894L;


    @ElementCollection()
    @CollectionTable(name = "collection_parents", joinColumns = @JoinColumn(name = "collection_id"))
    @Column(name = "parent_id")
    private Set<Long> parentIds = new HashSet<>();

    /**
     * Get ordered list of parents (ids) of this resources ... great grandfather, grandfather, father.
     * 
     * Note: in earlier implementations this contained the currentId as well, I've removed this, but am unsure
     * whether it should be there
     */
    @Transient
    @ElementCollection
    public Set<Long> getParentIds() {
        return parentIds;
    }

    public void setParentIds(Set<Long> parentIds) {
        this.parentIds = parentIds;
    }

    private transient Set<C> transientChildren = new LinkedHashSet<>();

    public abstract C getParent();

    public abstract void setParent(C c);

    @XmlTransient
    @Transient
    public Set<C> getTransientChildren() {
        return transientChildren;
    }

    public void setTransientChildren(Set<C> transientChildren) {
        this.transientChildren = transientChildren;
    }

    @XmlTransient
    @Transient
    public boolean isTopCollection() {
        return getParent() == null;
    }

    @XmlTransient
    @Transient
    public boolean isSubCollection() {
        return getParent() != null;
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    public <C extends HierarchicalCollection> List<C> getHierarchicalResourceCollections(Class<C> class1, C collection_) {
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
    public List<String> getParentNameList() {
        ArrayList<String> parentNameTree = new ArrayList<String>();
        for (C collection : getHierarchicalResourceCollections()) {
            parentNameTree.add(collection.getName());
        }
        return parentNameTree;
    }

    public abstract List<C> getHierarchicalResourceCollections();

    public List<C> getVisibleParents(Class<C> type) {
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

    @XmlTransient
    @Transient
    public boolean isTopLevel() {
        if ((getParent() == null) || (getParent().isHidden() == true)) {
            return true;
        }
        return false;
    }

    @XmlTransient
    @Transient
    public Long getParentId() {
        if (getParent() == null) {
            return null;
        }
        return getParent().getId();
    }

    /*
     * Default to sorting by name, but grouping by parentId, used for sorting int he tree
     */
    public <C extends HierarchicalCollection> int compareTo(C self, C o) {
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
